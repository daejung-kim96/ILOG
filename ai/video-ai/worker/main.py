import os
import sys
import cv2
import time
import json
import anyio
import numpy as np
import threading
import subprocess
import copy
from typing import List, Optional, Dict, Any
from datetime import datetime, timezone

from fastapi import FastAPI, Response, Request
from fastapi.responses import StreamingResponse, JSONResponse
import uvicorn


def log(*a):
    ts = datetime.now().strftime('%H:%M:%S')
    print(f"[{ts}]", *a, flush=True)


def env(name, default=None, cast=str):
    val = os.environ.get(name, None if default is None else str(default))
    if val is None:
        log(f"ENV {name} is required"); sys.exit(1)
    try:
        return cast(val)
    except Exception:
        return val


USER_CFG_KEYS = [
    'ai_fps',
    'sensitivity',
    'min_active_frames',
    'post_sec',
    'roi',
    'face_every_n',
    'draw_debug',
    'person_every_n',
    'yolo_conf_thresh',
    'yolo_iou_thresh',
    'clip_name_fmt',
    'clip_ts_fmt',
    'clip_cooldown_ms',
]


def _push_history(history: List[tuple[float, float, float]], item: tuple[float, float, float], keep_ms: float) -> None:
    """
    Append (ts_ms, nx, ny) to history and drop entries older than keep_ms.
    """
    history.append(item)
    if keep_ms <= 0 or not history:
        return
    cutoff = history[-1][0] - keep_ms
    # drop from front while too old
    idx = 0
    n = len(history)
    while idx < n and history[idx][0] < cutoff:
        idx += 1
    if idx > 0:
        del history[:idx]


def _displacement_norm(history: List[tuple[float, float, float]], window_ms: float) -> float:
    """
    Approximate 2D displacement in normalized coords over the last window_ms.
    Returns sqrt(dx^2 + dy^2).
    """
    if len(history) < 2 or window_ms <= 0:
        return 0.0
    now = history[-1][0]
    cutoff = now - window_ms
    xs = []
    ys = []
    for ts, nx, ny in history:
        if ts >= cutoff:
            xs.append(nx)
            ys.append(ny)
    if len(xs) < 2:
        return 0.0
    dx = max(xs) - min(xs)
    dy = max(ys) - min(ys)
    return float((dx * dx + dy * dy) ** 0.5)


def _vertical_rise(history: List[tuple[float, float, float]], window_ms: float) -> float:
    """
    Positive value means center moved upwards (ny decreased) over window.
    Return in normalized units (0..1).
    """
    if len(history) < 2 or window_ms <= 0:
        return 0.0
    now = history[-1][0]
    cutoff = now - window_ms
    ys = []
    for ts, nx, ny in history:
        if ts >= cutoff:
            ys.append(ny)
    if len(ys) < 2:
        return 0.0
    y_now = ys[-1]
    y_prev_max = max(ys[:-1]) if len(ys) > 1 else ys[0]
    # y decreases when moving upwards (top=0, bottom=1)
    return float(y_prev_max - y_now)


class State:
    def __init__(self):
        self.cfg = {
            'camera_id': env('CAMERA_ID', 'c200'),
            'rtsp_input': env('RTSP_INPUT', 'rtsp://hls-gateway:8554/c200'),
            'ai_fps': env('AI_FPS', 8, int),
            'sensitivity': env('SENSITIVITY', 0.015, float),
            'min_active_frames': env('MIN_ACTIVE_FRAMES', 6, int),
            'post_sec': env('POST_SEC', 5, int),
            'face_model': '/app/models/yunet.onnx',
            'face_every_n': 3,  # run face detection every N frames
            'draw_debug': True,
            'yolo_model': '/app/models/yolov8n.onnx',
            'person_every_n': 2,
            'yolo_conf_thresh': 0.25,
            'yolo_iou_thresh': 0.45,
            'audio': {
                'enabled': True,
                'input': 'rtsp',  # 'rtsp' -> use rtsp_input, or override with a URL
                # optional explicit audio RTSP; if not set, falls back to rtsp_input
                'rtsp_url': os.environ.get('AUDIO_RTSP_URL'),
                'sr': 16000,
                'channels': 1,
                'ffmpeg': 'ffmpeg',
                'frame_ms': 200,   # analysis frame size
            },
            # 이벤트 규칙(진입 임계/지속/쿨다운)
            'rules': {
                'NO_PERSON': {
                    'person_conf_th': 0.30,
                    'duration_ms': 5000,
                    'exit_ms': 800,
                    'cooldown_ms': 0,
                },
                'WAKE_UP': {
                    'motion_th': 0.08,
                    'duration_ms': 1500,
                    'exit_ms': 1200,
                    'cooldown_ms': 0,
                    'require_person': True,
                    'person_gate_th': 0.30,
                },
                'SUDDEN_MOVE': {
                    'delta_th': 0.06,       # |Δmotion| threshold (0..1)
                    'duration_ms': 1200,
                    'exit_ms': 800,
                    'cooldown_ms': 0,
                    'require_person': True,
                    'person_gate_th': 0.30,
                    # impulse/localized detectors (for small fast objects)
                    'impulse_th': 25,           # per-pixel diff threshold on prev-frame diff (0..255)
                    'impulse_ratio_th': 0.01,   # fraction of pixels over impulse_th
                    'tile_size': 32,            # tile side in pixels (on downscaled frame)
                    'tile_ratio_th': 0.25,      # within-tile changed pixel ratio
                },
                'PRONE': {
                    # person 보이는데 얼굴이 일정 시간 이상 안 보이고, 움직임이 낮으면 엎드림으로 판단
                    'person_gate_th': 0.30,
                    'face_conf_th': 0.30,
                    'min_no_face_ms': 3000,
                    'motion_max': 0.03,
                    'duration_ms': 1000,
                    'exit_ms': 1000,
                    'cooldown_ms': 0,
                },
                'FACE_COVERED': {
                    # require a person in ROI and low motion, but face confidence low
                    'person_gate_th': 0.30,
                    'face_conf_th': 0.30,
                    'motion_max': 0.02,
                    'duration_ms': 3000,
                    'exit_ms': 1000,
                    'cooldown_ms': 0,
                },
                'LOW_ACTIVITY': {
                    'motion_max': 0.01,
                    'duration_ms': 30000,
                    'exit_ms': 2000,
                    'cooldown_ms': 0,
                },
                'LOUD': {
                    'rms_th': 0.06,           # normalized 0..1
                    'band_low_hz': 100,
                    'band_high_hz': 600,
                    'band_energy_th': 0.25,   # ratio 0..1
                    'duration_ms': 200,
                    'exit_ms': 300,
                    'cooldown_ms': 0,
                },
                'CRY': {
                    'cry_th': 0.5,
                    'duration_ms': 800,
                    'exit_ms': 600,
                    'cooldown_ms': 0,
                },
                'OBJECT_NEAR_CHILD': {
                    'person_gate_th': 0.30,
                    # tracker-based rule: object speed (px/frame) and
                    # how much its distance to baby decreases between frames
                    'speed_thresh': 8.0,
                    'dist_dec_thresh': 20.0,
                    # fallback: when no person present, large ROI motion can also
                    # trigger OBJECT_NEAR_CHILD if motion >= motion_th
                    'motion_th': 0.08,
                    # blob-based fallback: when a single blob inside ROI is
                    # large enough (fraction of ROI area), treat as object near
                    # child even if global motion_ratio is small.
                    'blob_area_th': 0.0005,
                    'duration_ms': 0,
                    'exit_ms': 300,
                    'cooldown_ms': 0,
                },
            },
            'record_on': ['MOTION', 'NO_PERSON', 'FACE_COVERED', 'WAKE_UP', 'PRONE', 'OBJECT_NEAR_CHILD', 'LOUD'],
            # 클립 파일명 포맷
            'clip_name_fmt': '{cameraId}_{cause}_{ts}.mp4',
            'clip_ts_fmt': '%Y%m%d_%H%M%S',
            'clip_cooldown_ms': 0,
            'roi': None,  # e.g., [[x,y], ...] in 0..1 normalized
        }
        self.lock = threading.Lock()
        self.last_frame = None  # np.ndarray (BGR)
        self.last_frame_t = 0.0
        self.avg_fps = 0.0
        self.last_event_at: Optional[str] = None
        self.running = True
        self.events_q = []  # list of queue.Queue() for SSE fans
        self.face_det = None  # lazy init
        self.last_faces = []
        self.last_face_conf = None
        self.yolo_det = None
        self.last_persons = []
        self.last_person_conf = None
        self.tracker = None  # lazy init for multi-object tracker
        self.baby_track_id: Optional[int] = None
        self.baby_cx: Optional[float] = None
        self.baby_cy: Optional[float] = None
        self.last_near_cy: Optional[float] = None
        self.last_motion: Optional[float] = None
        self.rule_state: Dict[str, Dict[str, Any]] = {}
        self.loop_dt_ms: float = 0.0
        self.last_clip_ms: float = 0.0
        # audio signals
        self.audio_ok = False
        # instantaneous RMS of latest audio frame
        self.last_audio_rms_raw: Optional[float] = None
        # smoothed RMS (EMA) for debug/monitoring
        self.last_audio_rms: Optional[float] = None
        self.last_band_energy: Optional[float] = None
        self.last_cry_prob: Optional[float] = None
        # baby movement / posture history (for WAKE_UP, SUDDEN_MOVE)
        # entries: (ts_ms, nx, ny) with nx,ny in 0..1 normalized coordinates
        self.baby_history: List[tuple[float, float, float]] = []


S = State()
APP = FastAPI(title='AI Worker', version='0.1')


def _clip_name(camera_id: str, cause: Optional[str]) -> str:
    try:
        fmt = S.cfg.get('clip_name_fmt', '{cameraId}_{cause}_{ts}.mp4')
        tsfmt = S.cfg.get('clip_ts_fmt', '%Y%m%d_%H%M%S')
    except Exception:
        fmt = '{cameraId}_{cause}_{ts}.mp4'; tsfmt = '%Y%m%d_%H%M%S'
    ts = datetime.now().strftime(tsfmt)
    c = (cause or 'event').lower().replace(' ', '_')
    return fmt.format(cameraId=camera_id, cause=c, ts=ts)


def record_clip(rtsp_url: str, seconds: int, out_dir: str, camera_id: str, cause: Optional[str] = None) -> Optional[str]:
    os.makedirs(out_dir, exist_ok=True)
    out = os.path.join(out_dir, _clip_name(camera_id, cause))
    cmd_copy = [
        'ffmpeg','-y','-rtsp_transport','tcp','-i',rtsp_url,
        '-t',str(seconds),'-c','copy','-movflags','+faststart', out
    ]
    try:
        subprocess.run(cmd_copy, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
        return out
    except subprocess.CalledProcessError:
        cmd_re = [
            'ffmpeg','-y','-rtsp_transport','tcp','-i',rtsp_url,
            '-t',str(seconds),'-an','-c:v','libx264','-preset','ultrafast','-crf','28', out
        ]
        try:
            subprocess.run(cmd_re, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
            return out
        except Exception as e:
            log('clip record failed:', e)
            return None


def record_clip_and_emit(rtsp_url: str, seconds: int, out_dir: str, camera_id: str, cause: str):
    # global clip cooldown to avoid bursts
    try:
        cd = float(S.cfg.get('clip_cooldown_ms', 0))
    except Exception:
        cd = 0.0
    now_ms = time.time() * 1000.0
    if cd > 0 and (now_ms - getattr(S, 'last_clip_ms', 0.0)) < cd:
        return
    path = record_clip(rtsp_url, seconds, out_dir, camera_id, cause)
    if path:
        log('clip saved:', path)
        push_event({'type': 'CLIP', 'phase': 'done', 'cameraId': camera_id, 'at': ts_iso(),
                    'path': path, 'cause': cause})
        S.last_clip_ms = now_ms


def audio_loop():
    try:
        aud = S.cfg.get('audio', {})
        if not aud or not aud.get('enabled', True):
            return
        ff = str(aud.get('ffmpeg', 'ffmpeg'))
        sr = int(aud.get('sr', 16000))
        ch = int(aud.get('channels', 1))
        frame_ms = int(aud.get('frame_ms', 200))
        url = aud.get('rtsp_url') or (S.cfg.get('rtsp_input') if aud.get('input', 'rtsp') == 'rtsp' else None)
        if not url:
            log('audio: no input, disabling')
            return
        cmd = [ff, '-nostdin', '-loglevel', 'error',
               '-rtsp_transport', 'tcp', '-i', url,
               '-vn', '-ac', str(ch), '-ar', str(sr), '-f', 's16le', 'pipe:1']
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=0)
        bytes_per_sample = 2 * ch
        samples_per_frame = int(sr * (frame_ms/1000.0))
        buf = b''
        ema_rms = None
        while S.running:
            need = samples_per_frame * bytes_per_sample
            chunk = p.stdout.read(need)
            if not chunk or len(chunk) < need:
                # try to read more; if still not enough, restart ffmpeg
                time.sleep(0.05)
                continue
            buf = chunk
            x = np.frombuffer(buf, dtype=np.int16).astype(np.float32) / 32768.0
            # mono only; if stereo, take mean
            if ch > 1:
                x = x.reshape(-1, ch).mean(axis=1)
            rms = float(np.sqrt(np.mean(np.square(x)) + 1e-9))
            # band energy ratio
            fft = np.fft.rfft(x * np.hanning(x.shape[0]))
            mag2 = (np.abs(fft)**2)
            freqs = np.fft.rfftfreq(x.shape[0], d=1.0/sr)
            low = float(S.cfg['rules']['LOUD'].get('band_low_hz', 100))
            high = float(S.cfg['rules']['LOUD'].get('band_high_hz', 600))
            m = (freqs >= low) & (freqs <= high)
            band_energy = float(mag2[m].sum() / (mag2.sum() + 1e-9))
            # smooth
            if ema_rms is None:
                ema_rms = rms
            else:
                ema_rms = 0.8*ema_rms + 0.2*rms
            with S.lock:
                S.audio_ok = True
                S.last_audio_rms_raw = rms
                S.last_audio_rms = ema_rms
                S.last_band_energy = band_energy
                # YAMNet (cry_prob) hook left as None unless added later
    except Exception as e:
        log('audio_loop error:', e)


def ts_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def push_event(ev: Dict[str, Any]):
    ev_json = json.dumps(ev)
    # fan-out to blocking queues; readers will be polled via anyio.to_thread
    for q in list(S.events_q):
        try:
            q.put_nowait(ev_json)
        except Exception:
            pass


def make_roi_mask(shape, roi):
    if not roi:
        return None
    h, w = shape[:2]
    pts = np.array([[int(x*w), int(y*h)] for x,y in roi], dtype=np.int32)
    mask = np.zeros((h,w), dtype=np.uint8)
    cv2.fillPoly(mask, [pts], 255)
    return mask


def capture_loop():
    log('AI Worker starting...')
    avg = None
    state = 'IDLE'
    active_count = 0
    idle_count = 0
    last_tick = time.time()
    frame_idx = 0
    prev_motion_ratio = 0.0
    no_face_ms_acc = 0.0
    prev_gray = None

    prev_loop_t = time.time()
    while S.running:
        with S.lock:
            src = S.cfg['rtsp_input']
            ai_fps = max(1, int(S.cfg['ai_fps']))
            sens = float(S.cfg['sensitivity'])
            min_act = int(S.cfg['min_active_frames'])
            post_sec = int(S.cfg['post_sec'])
            roi = S.cfg.get('roi')
            face_model = S.cfg.get('face_model')
            face_every_n = int(S.cfg.get('face_every_n', 3))
            draw_debug = bool(S.cfg.get('draw_debug', True))
            yolo_model = S.cfg.get('yolo_model')
            person_every_n = int(S.cfg.get('person_every_n', 2))
            yolo_conf = float(S.cfg.get('yolo_conf_thresh', 0.25))
            yolo_iou = float(S.cfg.get('yolo_iou_thresh', 0.45))
        frame_interval = 1.0 / ai_fps

        cap = cv2.VideoCapture(src, cv2.CAP_FFMPEG)
        if not cap.isOpened():
            log('failed to open RTSP source, retry in 2s')
            time.sleep(2)
            continue

        roi_mask = None
        avg = None
        try:
            while S.running:
                t0 = time.time()
                ok, frame = cap.read()
                if not ok or frame is None:
                    log('frame read failed, reopen in 1s')
                    time.sleep(1)
                    break

                if (t0 - last_tick) > 0:
                    inst = 1.0 / max(1e-6, t0 - last_tick)
                    S.avg_fps = 0.9*S.avg_fps + 0.1*inst if S.avg_fps else inst
                    last_tick = t0

                # store last frame
                with S.lock:
                    S.last_frame = frame.copy()
                    S.last_frame_t = t0

                # FPS throttle
                dt = time.time() - t0
                if dt < frame_interval:
                    time.sleep(frame_interval - dt)
                # 루프 dt(ms) 갱신
                now_loop = time.time()
                S.loop_dt_ms = (now_loop - prev_loop_t) * 1000.0
                prev_loop_t = now_loop

                small = cv2.resize(frame, (0,0), fx=0.5, fy=0.5)
                gray = cv2.cvtColor(small, cv2.COLOR_BGR2GRAY)
                gray = cv2.GaussianBlur(gray, (5,5), 0)

                if roi and roi_mask is None:
                    roi_mask = make_roi_mask(gray.shape, roi)
                if roi_mask is not None:
                    gray = cv2.bitwise_and(gray, roi_mask)

                if avg is None:
                    avg = gray.astype('float')
                    continue

                cv2.accumulateWeighted(gray, avg, 0.05)
                delta = cv2.absdiff(gray, cv2.convertScaleAbs(avg))
                _, th = cv2.threshold(delta, 15, 255, cv2.THRESH_BINARY)
                motion_ratio = (th > 0).mean()
                # blob-based motion summary (for thrown objects etc.)
                blob_area_max = 0.0
                try:
                    contours, _ = cv2.findContours(th, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
                    fh, fw = th.shape
                    frame_area = float(fh * fw) if fh > 0 and fw > 0 else 1.0
                    for c in contours:
                        area = cv2.contourArea(c)
                        if area <= 0:
                            continue
                        ar = float(area / frame_area)
                        if ar > blob_area_max:
                            blob_area_max = ar
                except Exception:
                    blob_area_max = 0.0
                # prev-frame impulse diff (captures sudden small object changes)
                impulse_ratio = 0.0
                tile_max_ratio = 0.0
                ih = gray.shape[0]
                iw = gray.shape[1]
                if prev_gray is not None and prev_gray.shape == gray.shape:
                    fdiff = cv2.absdiff(gray, prev_gray)
                    try:
                        scfg = S.cfg.get('rules', {}).get('SUDDEN_MOVE', {})
                        imp_th = int(float(scfg.get('impulse_th', 25)))
                        _, imp_mask = cv2.threshold(fdiff, imp_th, 255, cv2.THRESH_BINARY)
                        impulse_ratio = (imp_mask > 0).mean()
                        ts = int(float(scfg.get('tile_size', 32)))
                        tr_th = float(scfg.get('tile_ratio_th', 0.25))
                        if ts > 4:
                            for y in range(0, ih, ts):
                                for x in range(0, iw, ts):
                                    tile = imp_mask[y:min(y+ts, ih), x:min(x+ts, iw)]
                                    if tile.size == 0:
                                        continue
                                    r = (tile > 0).mean()
                                    if r > tile_max_ratio:
                                        tile_max_ratio = r
                    except Exception:
                        pass
                prev_gray = gray.copy()
                delta_motion = abs(motion_ratio - prev_motion_ratio)
                prev_motion_ratio = motion_ratio
                # 카메라 블록 감지용 라플라시안 분산
                lap_var = cv2.Laplacian(gray, cv2.CV_64F).var()

                # Face detection every N frames (optional)
                frame_idx += 1
                face_conf_max = None
                faces_list = []
                if face_model and (frame_idx % max(1, face_every_n) == 0):
                    try:
                        if S.face_det is None and os.path.exists(face_model):
                            from models.yunet import YuNetDetector
                            S.face_det = YuNetDetector(model_path=face_model, input_size=(320, 320), score_threshold=0.3)
                            log('YuNet loaded:', face_model)
                        if S.face_det is not None:
                            faces_list = S.face_det.detect(frame)
                            if faces_list:
                                face_conf_max = max(f.get('score', 0.0) for f in faces_list)
                    except Exception as e:
                        # load/detect errors should not kill the loop
                        log('face detect error:', e)
                        S.face_det = None
                        faces_list = []
                        face_conf_max = None

                with S.lock:
                    S.last_faces = faces_list
                    S.last_face_conf = face_conf_max
                    S.last_motion = float(motion_ratio)

                # YOLO person/object detection every N frames (optional)
                tracks = []
                person_conf_max = None
                persons_list = []
                if yolo_model and (frame_idx % max(1, person_every_n) == 0):
                    try:
                        # lazy init: multiclass YOLO detector + tracker
                        if S.yolo_det is None:
                            from vision.detector_yolo import YoloDetector
                            S.yolo_det = YoloDetector(yolo_model, conf=yolo_conf, iou=yolo_iou)
                            log('YOLO(multiclass) loaded:', yolo_model)
                        if S.tracker is None:
                            from vision.tracker import SimpleTracker
                            S.tracker = SimpleTracker()
                        detections = S.yolo_det.detect(frame)
                        tracks = S.tracker.update(detections)
                        # for now, keep backwards-compatible persons_list for rules
                        # and also count non-person objects for debugging
                        non_persons_count = sum(
                            1 for det in detections
                            if int(det.get("cls", -1)) != 0
                        )
                        persons_list = [
                            {"bbox": list(det["bbox"]), "score": det["conf"]}
                            for det in detections
                            if int(det.get("cls", -1)) == 0  # COCO person class id
                        ]
                        if persons_list:
                            person_conf_max = max(p.get('score', 0.0) for p in persons_list)
                        # baby track heuristic: person track closest to ROI (if any)
                        crib_roi = None
                        try:
                            roi = S.cfg.get('roi')
                            if roi:
                                fh, fw = frame.shape[:2]
                                xs = [float(p[0]) * fw for p in roi]
                                ys = [float(p[1]) * fh for p in roi]
                                crib_roi = (min(xs), min(ys), max(xs), max(ys))
                        except Exception:
                            crib_roi = None
                        try:
                            from vision.tracker import pick_baby_track
                            baby = pick_baby_track(tracks, crib_roi, person_cls_id=0)
                        except Exception:
                            baby = None
                        # update baby track state + normalized movement history
                        fh, fw = frame.shape[:2]
                        with S.lock:
                            if baby is not None:
                                S.baby_track_id = baby.id
                                bx, by = baby.center
                                S.baby_cx = float(bx)
                                S.baby_cy = float(by)
                                try:
                                    # normalized center (0..1)
                                    nx = float(bx) / max(1.0, float(fw))
                                    ny = float(by) / max(1.0, float(fh))
                                    ts_ms = t0 * 1000.0
                                    _push_history(S.baby_history, (ts_ms, nx, ny), keep_ms=3000.0)
                                except Exception:
                                    pass
                            else:
                                S.baby_track_id = None
                                S.baby_cx = None
                                S.baby_cy = None
                                S.baby_history.clear()
                        # debug: log number of tracks and non-person detections
                        log('tracks:', len(tracks),
                            'persons:', len(persons_list),
                            'non_persons:', non_persons_count,
                            'baby_track:', S.baby_track_id)
                    except Exception as e:
                        log('yolo detect error:', e)
                        S.yolo_det = None
                        S.tracker = None
                        persons_list = []
                        person_conf_max = None

                with S.lock:
                    S.last_persons = persons_list
                    S.last_person_conf = person_conf_max

                # face 부재 누적시간(ms)
                face_present = (face_conf_max is not None) and (float(face_conf_max) >= float(S.cfg.get('rules', {}).get('PRONE', {}).get('face_conf_th', 0.30)))
                if face_present:
                    no_face_ms_acc = 0.0
                else:
                    no_face_ms_acc = min(60_000.0, no_face_ms_acc + S.loop_dt_ms)

                # MOTION: only when a person is present; large motion without
                # person will be handled by OBJECT_NEAR_CHILD fallback.
                person_present_for_motion = float(person_conf_max or 0.0) >= float(S.cfg.get('rules', {}).get('WAKE_UP', {}).get('person_gate_th', 0.30))
                if person_present_for_motion and motion_ratio > sens:
                    active_count += 1
                    idle_count = 0
                else:
                    idle_count += 1
                    if idle_count > min_act:
                        active_count = 0

                if state == 'IDLE' and active_count >= min_act:
                    state = 'ACTIVE'
                    S.last_event_at = ts_iso()
                    ev = {
                        'type': 'MOTION', 'phase': 'start', 'cameraId': S.cfg['camera_id'],
                        'at': S.last_event_at,
                        'score': float(motion_ratio),
                        'signals': {
                            'motion': float(motion_ratio),
                            'face_conf_max': float(face_conf_max) if face_conf_max is not None else None,
                            'person_conf_max': float(person_conf_max) if person_conf_max is not None else None,
                        }
                    }
                    log('[EVENT] start', ev)
                    push_event(ev)
                    if 'MOTION' in S.cfg.get('record_on', []):
                        threading.Thread(target=lambda: record_clip_and_emit(S.cfg['rtsp_input'], post_sec, '/app/clips', S.cfg['camera_id'], 'MOTION'), daemon=True).start()

                if state == 'ACTIVE' and idle_count >= min_act:
                    state = 'IDLE'
                    ev = {
                        'type': 'MOTION', 'phase': 'end', 'cameraId': S.cfg['camera_id'],
                        'at': ts_iso(),
                        'signals': {
                            'motion': float(motion_ratio),
                            'face_conf_max': float(S.last_face_conf) if S.last_face_conf is not None else None,
                            'person_conf_max': float(S.last_person_conf) if S.last_person_conf is not None else None,
                        }
                    }
                    log('[EVENT] end  ', ev)
                    push_event(ev)

                # ===== 상태머신 기반 이벤트 처리 =====
                def upd(name: str, cond: bool, signals: Dict[str, Any]):
                    cfg = S.cfg['rules'].get(name, {})
                    enter_ms = float(cfg.get('duration_ms', 1000))
                    exit_ms = float(cfg.get('exit_ms', 600.0))
                    # global default: no cooldown unless explicitly configured
                    cooldown_ms = float(cfg.get('cooldown_ms', 0.0))
                    st = S.rule_state.get(name, {'active': False, 'on_ms': 0.0, 'off_ms': 0.0, 'last_emit': 0.0})
                    # 누적 (히스테리시스)
                    if cond:
                        st['on_ms'] = min(enter_ms, st['on_ms'] + S.loop_dt_ms)
                        st['off_ms'] = 0.0
                    else:
                        st['off_ms'] = min(exit_ms, st['off_ms'] + S.loop_dt_ms)
                        st['on_ms'] = 0.0
                    # 종료: cond 거짓이 exit_ms 지속
                    if st['active'] and (not cond) and (st['off_ms'] >= exit_ms):
                        st['active'] = False
                        ev = {'type': name, 'phase': 'end', 'cameraId': S.cfg['camera_id'], 'at': ts_iso(), 'signals': signals}
                        push_event(ev)
                    # 시작: cond 참이 duration_ms 지속 && cooldown 이후
                    now_ms = time.time() * 1000.0
                    if (not st['active']) and cond and (st['on_ms'] >= enter_ms) and (now_ms - st.get('last_emit', 0.0) >= cooldown_ms):
                        st['active'] = True
                        st['last_emit'] = now_ms
                        ev = {'type': name, 'phase': 'start', 'cameraId': S.cfg['camera_id'], 'at': ts_iso(), 'signals': signals}
                        push_event(ev)
                        if name in S.cfg.get('record_on', []):
                            threading.Thread(target=lambda: record_clip_and_emit(S.cfg['rtsp_input'], post_sec, '/app/clips', S.cfg['camera_id'], name), daemon=True).start()
                    S.rule_state[name] = st

                rules = S.cfg.get('rules', {})
                # NO_PERSON
                if 'NO_PERSON' in rules:
                    pth = float(rules['NO_PERSON'].get('person_conf_th', 0.3))
                    cond = (person_conf_max or 0.0) < pth
                    upd('NO_PERSON', cond, {'person_conf_max': float(person_conf_max or 0.0)})
                # WAKE_UP (baby vertical rise or motion high)
                if 'WAKE_UP' in rules:
                    wcfg = rules['WAKE_UP']
                    mth = float(wcfg.get('motion_th', 0.08))
                    # legacy motion-based condition
                    cond = float(motion_ratio) >= mth
                    # baby vertical movement (normalized)
                    try:
                        wy_ms = float(wcfg.get('window_ms', 1800.0))
                        rise_th = float(wcfg.get('rise_norm_th', 0.12))
                    except Exception:
                        wy_ms = 1800.0
                        rise_th = 0.12
                    rise = _vertical_rise(S.baby_history, wy_ms) if S.baby_history else 0.0
                    cond = cond or (rise >= rise_th)
                    if bool(wcfg.get('require_person', False)):
                        gate = float(wcfg.get('person_gate_th', 0.3))
                        cond = cond and float(person_conf_max or 0.0) >= gate
                    upd('WAKE_UP', cond, {
                        'motion': float(motion_ratio),
                        'rise_norm': float(rise),
                    })
                # SUDDEN_MOVE (sudden baby displacement or delta motion) - disabled
                if False and 'SUDDEN_MOVE' in rules:
                    scfg = rules['SUDDEN_MOVE']
                    dth = float(scfg.get('delta_th', 0.06))
                    ir_th = float(scfg.get('impulse_ratio_th', 0.01))
                    tr_th = float(scfg.get('tile_ratio_th', 0.25))
                    cond = (float(delta_motion) >= dth) or (float(impulse_ratio) >= ir_th) or (float(tile_max_ratio) >= tr_th)
                    # baby-centered displacement (normalized)
                    try:
                        win_ms = float(scfg.get('window_ms', 1000.0))
                        disp_th = float(scfg.get('disp_norm_th', 0.10))
                    except Exception:
                        win_ms = 1000.0
                        disp_th = 0.10
                    disp = _displacement_norm(S.baby_history, win_ms) if S.baby_history else 0.0
                    cond = cond or (disp >= disp_th)
                    # if WAKE_UP is already active, do not double-fire SUDDEN_MOVE
                    wst = S.rule_state.get('WAKE_UP', {})
                    if wst.get('active'):
                        cond = False
                    if bool(scfg.get('require_person', False)):
                        gate = float(scfg.get('person_gate_th', 0.3))
                        cond = cond and float(person_conf_max or 0.0) >= gate
                    upd('SUDDEN_MOVE', cond, {
                        'motion': float(motion_ratio),
                        'delta_motion': float(delta_motion),
                        'impulse_ratio': float(impulse_ratio),
                        'tile_max_ratio': float(tile_max_ratio),
                        'person_conf_max': float(person_conf_max or 0.0),
                        'baby_disp_norm': float(disp),
                    })
                # FACE_COVERED (person present, low face conf, and low motion)
                if 'FACE_COVERED' in rules:
                    fcfg = rules['FACE_COVERED']
                    gate = float(fcfg.get('person_gate_th', 0.30))
                    fth = float(fcfg.get('face_conf_th', 0.30))
                    mmax = float(fcfg.get('motion_max', 0.02))
                    person_ok = float(person_conf_max or 0.0) >= gate
                    face_bad = float(face_conf_max or 0.0) < fth
                    cond = person_ok and face_bad and (float(motion_ratio) <= mmax)
                    upd('FACE_COVERED', cond, {
                        'face_conf_max': float(face_conf_max or 0.0),
                        'person_conf_max': float(person_conf_max or 0.0),
                        'motion': float(motion_ratio),
                    })
                # PRONE (person present, no face for N ms, and low motion)
                if 'PRONE' in rules:
                    pcfg = rules['PRONE']
                    gate = float(pcfg.get('person_gate_th', 0.30))
                    fth = float(pcfg.get('face_conf_th', 0.30))
                    mmax = float(pcfg.get('motion_max', 0.03))
                    min_nf = float(pcfg.get('min_no_face_ms', 3000.0))
                    person_ok = float(person_conf_max or 0.0) >= gate
                    face_ok = float(face_conf_max or 0.0) >= fth if face_conf_max is not None else False
                    cond = person_ok and (not face_ok) and (no_face_ms_acc >= min_nf) and (float(motion_ratio) <= mmax)
                    upd('PRONE', cond, {'no_face_ms': float(no_face_ms_acc), 'person_conf_max': float(person_conf_max or 0.0), 'motion': float(motion_ratio)})
                # LOW_ACTIVITY (sustained low motion)
                if 'LOW_ACTIVITY' in rules:
                    mmax = float(rules['LOW_ACTIVITY'].get('motion_max', 0.01))
                    cond = float(motion_ratio) <= mmax
                    upd('LOW_ACTIVITY', cond, {'motion': float(motion_ratio)})
                # OBJECT_NEAR_CHILD (tracked object rapidly approaching baby inside crib ROI,
                # or any fast object moving inside ROI)
                if 'OBJECT_NEAR_CHILD' in rules:
                    ocfg = rules['OBJECT_NEAR_CHILD']
                    gate = float(ocfg.get('person_gate_th', 0.30))
                    person_ok = float(person_conf_max or 0.0) >= gate
                    # derive crib ROI from cfg['roi'] polygon (same convention as above)
                    crib_roi = None
                    try:
                        roi_cfg = S.cfg.get('roi')
                        if roi_cfg:
                            fh, fw = frame.shape[:2]
                            xs = [float(p[0]) * fw for p in roi_cfg]
                            ys = [float(p[1]) * fh for p in roi_cfg]
                            crib_roi = (min(xs), min(ys), max(xs), max(ys))
                    except Exception:
                        crib_roi = None

                    # find current baby track from tracker output
                    baby_track = None
                    baby_id = None
                    try:
                        with S.lock:
                            baby_id = S.baby_track_id
                    except Exception:
                        baby_id = None
                    if baby_id is not None:
                        for t in tracks:
                            if t.id == baby_id:
                                baby_track = t
                                break

                    from rules.object_near_child import check_object_near_child
                    speed_th = float(ocfg.get('speed_thresh', 8.0))
                    dist_dec_th = float(ocfg.get('dist_dec_thresh', 20.0))
                    roi_speed_th = float(ocfg.get('roi_speed_thresh', speed_th))
                    motion_th = float(ocfg.get('motion_th', sens))
                    blob_area_th = float(ocfg.get('blob_area_th', 0.003))
                    max_near = ocfg.get('max_near_dist_px', None)
                    try:
                        max_near = float(max_near) if max_near is not None else None
                    except Exception:
                        max_near = None
                    require_down = bool(ocfg.get('require_downward', True))
                    near_list = []
                    if baby_track is not None and tracks:
                        near_list = check_object_near_child(
                            tracks,
                            baby_track,
                            crib_roi,
                            speed_th,
                            dist_dec_th,
                            max_near_dist=max_near,
                            require_downward=require_down,
                        )
                    # fast object purely based on ROI + speed, independent of baby/person gating
                    roi_fast = False
                    if crib_roi is not None and tracks:
                        x1, y1, x2, y2 = crib_roi
                        for t in tracks:
                            cx, cy = t.center
                            if x1 <= cx <= x2 and y1 <= cy <= y2 and t.speed >= roi_speed_th:
                                roi_fast = True
                                break

                    # person 있는 경우: tracker 기반 OBJECT_NEAR_CHILD
                    tracking_cond = person_ok and bool(near_list)
                    # person 없는 경우: ROI 안 빠른 객체 또는 큰 모션을 OBJECT_NEAR_CHILD로 처리
                    no_person = not person_ok
                    motion_fallback = no_person and (float(motion_ratio) >= motion_th)
                    roi_fast_cond = no_person and roi_fast
                    blob_cond = no_person and (float(blob_area_max) >= blob_area_th)
                    cond = tracking_cond or roi_fast_cond or motion_fallback or blob_cond
                    if near_list:
                        obj = max(near_list, key=lambda t: t.speed)
                        cx, cy = obj.center
                        # debug: log OBJECT_NEAR_CHILD candidate summary
                        log('OBJECT_NEAR_CHILD cand_cnt:', len(near_list),
                            'picked_id:', obj.id,
                            'cls:', obj.cls_id,
                            'speed:', obj.speed,
                            'cx:', cx, 'cy:', cy)
                        signals = {
                            'track_id': int(obj.id),
                            'cls_id': int(obj.cls_id),
                            'speed': float(obj.speed),
                            'cx': float(cx),
                            'cy': float(cy),
                            'person_conf_max': float(person_conf_max or 0.0),
                            'roi_fast': bool(roi_fast),
                        }
                    else:
                        signals = {
                            'track_id': None,
                            'cls_id': None,
                            'speed': 0.0,
                            'cx': None,
                            'cy': None,
                            'person_conf_max': float(person_conf_max or 0.0),
                            'roi_fast': bool(roi_fast),
                        }
                    upd('OBJECT_NEAR_CHILD', cond, signals)

                # LOUD (audio energy high)
                if 'LOUD' in rules:
                    rms_th = float(rules['LOUD'].get('rms_th', 0.08))
                    be_th = float(rules['LOUD'].get('band_energy_th', 0.35))
                    # use instantaneous RMS for impulsive sounds (claps, drops)
                    a_rms = float(S.last_audio_rms_raw) if getattr(S, 'last_audio_rms_raw', None) is not None else 0.0
                    b_en = float(S.last_band_energy) if S.last_band_energy is not None else 0.0
                    cond = (a_rms >= rms_th) or (b_en >= be_th)
                    upd('LOUD', cond, {'audio_rms': a_rms, 'band_energy': b_en})

                # CRY (requires cry_prob; left inactive if None)
                if 'CRY' in rules:
                    cprob = S.last_cry_prob
                    if cprob is None:
                        pass
                    else:
                        cth = float(rules['CRY'].get('cry_th', 0.5))
                        cond = float(cprob) >= cth
                        upd('CRY', cond, {'cry_prob': float(cprob)})

        finally:
            cap.release()


@APP.get('/healthz')
def healthz():
    with S.lock:
        data = {
            'ok': True,
            'cameraId': S.cfg['camera_id'],
            'rtsp_input': S.cfg['rtsp_input'],
            'ai_fps': S.cfg['ai_fps'],
            'avg_fps': round(S.avg_fps, 2),
            'lastEventAt': S.last_event_at,
            'face_conf_max': float(S.last_face_conf) if S.last_face_conf is not None else None,
            'person_conf_max': float(S.last_person_conf) if S.last_person_conf is not None else None,
            'rules': S.cfg.get('rules', {}),
            'audio_ok': S.audio_ok,
            'audio_rms': float(S.last_audio_rms) if S.last_audio_rms is not None else None,
            'audio_band_energy': float(S.last_band_energy) if S.last_band_energy is not None else None,
            'cry_prob': float(S.last_cry_prob) if S.last_cry_prob is not None else None,
        }
    return JSONResponse(data)


CONFIG_PATH = '/app/config.json'


def load_config_file():
    if os.path.exists(CONFIG_PATH):
        try:
            with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
                data = json.load(f)
            with S.lock:
                for k in USER_CFG_KEYS:
                    if k in data:
                        S.cfg[k] = data[k]
                if 'audio' in data and isinstance(data['audio'], dict):
                    S.cfg['audio'].update(data['audio'])
                if 'rules' in data and isinstance(data['rules'], dict):
                    for ev, params in data['rules'].items():
                        if ev not in S.cfg['rules'] or not isinstance(S.cfg['rules'][ev], dict):
                            S.cfg['rules'][ev] = params
                        else:
                            S.cfg['rules'][ev].update(params)
                if 'record_on' in data and isinstance(data['record_on'], list):
                    # merge persisted list with defaults so new events (e.g. LOUD)
                    # get added without breaking older configs
                    current = list(S.cfg.get('record_on', []))
                    for ev in data['record_on']:
                        if ev not in current:
                            current.append(ev)
                    S.cfg['record_on'] = current
        except Exception as e:
            log('config load failed:', e)


def save_config_file():
    try:
        with S.lock:
            data = {}
            for k in USER_CFG_KEYS:
                if k in S.cfg:
                    data[k] = S.cfg[k]
            audio_cfg = S.cfg.get('audio')
            if isinstance(audio_cfg, dict):
                data['audio'] = audio_cfg.copy()
            rules_cfg = S.cfg.get('rules')
            if isinstance(rules_cfg, dict):
                data['rules'] = copy.deepcopy(rules_cfg)
            record_on_cfg = S.cfg.get('record_on')
            if isinstance(record_on_cfg, list):
                data['record_on'] = list(record_on_cfg)
        with open(CONFIG_PATH, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
    except Exception as e:
        log('config save failed:', e)


@APP.get('/config')
def get_config():
    with S.lock:
        return JSONResponse(S.cfg)


@APP.get('/signals')
def get_signals():
    with S.lock:
        motion = getattr(S, 'last_motion', None)
        face_conf = getattr(S, 'last_face_conf', None)
        person_conf = getattr(S, 'last_person_conf', None)
        last_ts = getattr(S, 'last_frame_t', 0.0)
        baby_cx = getattr(S, 'baby_cx', None)
        baby_cy = getattr(S, 'baby_cy', None)
        baby_nx = None
        baby_ny = None
        if baby_cx is not None and baby_cy is not None and S.last_frame is not None:
            fh, fw = S.last_frame.shape[:2]
            baby_nx = float(baby_cx) / max(1.0, float(fw))
            baby_ny = float(baby_cy) / max(1.0, float(fh))
        data = {
            'cameraId': S.cfg.get('camera_id'),
            'motion': float(motion) if motion is not None else None,
            'faceConf': float(face_conf) if face_conf is not None else None,
            'personConf': float(person_conf) if person_conf is not None else None,
            'lastFrameTs': float(last_ts) if last_ts else None,
            'babyCx': float(baby_cx) if baby_cx is not None else None,
            'babyCy': float(baby_cy) if baby_cy is not None else None,
            'babyNx': float(baby_nx) if baby_nx is not None else None,
            'babyNy': float(baby_ny) if baby_ny is not None else None,
        }
    return JSONResponse(data)


@APP.put('/config')
async def put_config(req: Request):
    body = await req.json()
    with S.lock:
        for k in USER_CFG_KEYS:
            if k in body:
                S.cfg[k] = body[k]
        if 'audio' in body and isinstance(body['audio'], dict):
            S.cfg['audio'].update(body['audio'])
        if 'rules' in body and isinstance(body['rules'], dict):
            # shallow merge per-event
            for ev, params in body['rules'].items():
                if ev not in S.cfg['rules'] or not isinstance(S.cfg['rules'][ev], dict):
                    S.cfg['rules'][ev] = params
                else:
                    S.cfg['rules'][ev].update(params)
        if 'record_on' in body and isinstance(body['record_on'], list):
            S.cfg['record_on'] = list(body['record_on'])
    save_config_file()
    return JSONResponse({'ok': True})


@APP.get('/preview.jpg')
def preview():
    with S.lock:
        frame = None if S.last_frame is None else S.last_frame.copy()
        roi = S.cfg.get('roi')
        faces = list(S.last_faces) if getattr(S, 'last_faces', None) else []
        draw_debug = bool(S.cfg.get('draw_debug', True))
        persons = list(S.last_persons) if getattr(S, 'last_persons', None) else []
    if frame is None:
        return Response(status_code=503)
    if roi:
        h, w = frame.shape[:2]
        pts = np.array([[int(x*w), int(y*h)] for x,y in roi], dtype=np.int32)
        cv2.polylines(frame, [pts], isClosed=True, color=(0,255,0), thickness=2)
    if draw_debug and faces:
        try:
            from models.yunet import YuNetDetector
            frame = YuNetDetector.draw(frame, faces)
        except Exception:
            pass
    if draw_debug and persons:
        try:
            for p in persons:
                x1, y1, x2, y2 = [int(v) for v in p['bbox']]
                cv2.rectangle(frame, (x1,y1), (x2,y2), (0,128,255), 2)
                cv2.putText(frame, f"{p.get('score',0):.2f}", (x1, max(0,y1-4)),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0,128,255), 1, cv2.LINE_AA)
        except Exception:
            pass
    ok, buf = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 80])
    if not ok:
        return Response(status_code=500)
    return Response(content=buf.tobytes(), media_type='image/jpeg')


@APP.get('/events/stream')
async def events_stream():
    import queue
    q = queue.Queue(maxsize=100)
    S.events_q.append(q)

    async def gen():
        try:
            while True:
                item = await anyio.to_thread.run_sync(q.get)
                yield f"data: {item}\n\n"
        except anyio.get_cancelled_exc_class():
            pass
        finally:
            if q in S.events_q:
                S.events_q.remove(q)

    headers = {
        'Cache-Control': 'no-cache',
        'Content-Type': 'text/event-stream',
        'Connection': 'keep-alive',
        'Access-Control-Allow-Origin': '*',
    }
    return StreamingResponse(gen(), headers=headers)


def main():
    load_config_file()
    th = threading.Thread(target=capture_loop, daemon=True)
    th.start()
    # start audio thread if enabled
    if S.cfg.get('audio', {}).get('enabled', True):
        threading.Thread(target=audio_loop, daemon=True).start()
    uvicorn.run(APP, host='0.0.0.0', port=9108, log_level='info')


if __name__ == '__main__':
    main()
