"""
Multiclass YOLO detector wrapper.

This is similar to models/yolo_person.py but exposes all COCO
classes instead of collapsing everything into 'person'.

It expects a YOLOv8-style ONNX model such as yolov8n.onnx placed
under /app/models (container) or ai/video-ai/worker/models (repo).
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import List, Tuple, Dict

import numpy as np

try:  # pragma: no cover - runtime dependency
    import onnxruntime as ort
except Exception:  # pragma: no cover
    ort = None

try:
    import cv2  # type: ignore
except Exception:  # pragma: no cover
    cv2 = None


def _xywh_to_xyxy(xywh: np.ndarray) -> np.ndarray:
    """Convert [x_c,y_c,w,h] to [x1,y1,x2,y2] (corner format)."""
    x, y, w, h = xywh.T
    x1 = x - w / 2
    y1 = y - h / 2
    x2 = x + w / 2
    y2 = y + h / 2
    return np.stack([x1, y1, x2, y2], axis=1)


def _nms(boxes: np.ndarray, scores: np.ndarray, iou_thres: float, top_k: int = 300) -> List[int]:
    """Simple NMS used by the existing yolo_person wrapper."""
    if boxes.size == 0:
        return []
    idxs = scores.argsort()[::-1]
    keep: List[int] = []
    while idxs.size > 0 and len(keep) < top_k:
        i = idxs[0]
        keep.append(i)
        if idxs.size == 1:
            break
        rest = idxs[1:]
        xx1 = np.maximum(boxes[i, 0], boxes[rest, 0])
        yy1 = np.maximum(boxes[i, 1], boxes[rest, 1])
        xx2 = np.minimum(boxes[i, 2], boxes[rest, 2])
        yy2 = np.minimum(boxes[i, 3], boxes[rest, 3])
        w = np.maximum(0, xx2 - xx1)
        h = np.maximum(0, yy2 - yy1)
        inter = w * h
        area_i = (boxes[i, 2] - boxes[i, 0]) * (boxes[i, 3] - boxes[i, 1])
        area_r = (boxes[rest, 2] - boxes[rest, 0]) * (boxes[rest, 3] - boxes[rest, 1])
        union = area_i + area_r - inter
        iou = inter / np.maximum(union, 1e-6)
        idxs = rest[iou <= iou_thres]
    return keep


@dataclass
class YoloDetection:
    cls: int
    conf: float
    bbox: Tuple[float, float, float, float]


class YoloDetector:
    """
    Multiclass YOLO detector using an ONNXRuntime session.

    Example output:
      [
        { "cls": 0, "conf": 0.91, "bbox": (x1,y1,x2,y2) },  # person
        { "cls": 39, "conf": 0.76, "bbox": (...) },         # bottle
      ]
    """

    def __init__(self, model_path: str = "/app/models/yolov8n.onnx",
                 conf: float = 0.3, iou: float = 0.45,
                 input_size: Tuple[int, int] = (640, 640)) -> None:
        if ort is None:
            raise RuntimeError("onnxruntime is required for YoloDetector")
        if cv2 is None:
            raise RuntimeError("opencv-python is required for YoloDetector")
        self.session = ort.InferenceSession(model_path, providers=["CPUExecutionProvider"])
        self.input_name = self.session.get_inputs()[0].name
        self.out_names = [o.name for o in self.session.get_outputs()]
        self.input_size = tuple(int(x) for x in input_size)
        self.conf_thres = float(conf)
        self.iou_thres = float(iou)

    def _preprocess(self, bgr: np.ndarray) -> Tuple[np.ndarray, Tuple[float, float, float, float]]:
        """Letterbox-resize BGR frame to model input."""
        h, w = bgr.shape[:2]
        dw, dh = self.input_size
        r = min(dw / w, dh / h)
        nw, nh = int(w * r), int(h * r)
        resized = cv2.resize(bgr, (nw, nh))
        pad_w, pad_h = dw - nw, dh - nh
        top, bottom = pad_h // 2, pad_h - pad_h // 2
        left, right = pad_w // 2, pad_w - pad_w // 2
        padded = cv2.copyMakeBorder(
            resized, top, bottom, left, right,
            cv2.BORDER_CONSTANT, value=(114, 114, 114),
        )
        img = padded[:, :, ::-1].astype(np.float32) / 255.0  # BGR->RGB, 0-1
        img = np.transpose(img, (2, 0, 1))[None, ...]        # NCHW
        meta = (r, left, top, w, h)
        return img, meta

    def _post(self, out: np.ndarray, meta: Tuple[float, float, float, float]) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
        """
        Post-process raw model output into boxes, scores, class ids.
        Supports common YOLOv8 ONNX export layouts.
        """
        r, left, top, orig_w, orig_h = meta

        if out.ndim == 3:
            out = out.squeeze(0)
        if out.shape[0] in (84, 85):  # 84x8400 or 85x8400
            out = out.T  # -> Nx84/85

        xywh = out[:, :4]

        scores = None
        cls_ids = None

        if out.shape[1] == 85:
            obj = out[:, 4:5]          # (N,1)
            cls = out[:, 5:]           # (N,80)
            cls_conf = cls * obj       # (N,80)
            cls_ids = cls_conf.argmax(axis=1)
            scores = cls_conf.max(axis=1)
        else:  # 84 (YOLOv8 class scores only)
            cls = out[:, 4:]
            cls_ids = cls.argmax(axis=1)
            scores = cls.max(axis=1)

        cls_ids = cls_ids.astype(np.int32)
        scores = scores.astype(np.float32)

        mask = scores >= self.conf_thres
        if not np.any(mask):
            return (np.empty((0, 4), dtype=np.float32),
                    np.empty((0,), dtype=np.float32),
                    np.empty((0,), dtype=np.int32))

        xywh = xywh[mask]
        scores = scores[mask]
        cls_ids = cls_ids[mask]

        # to xyxy on padded image
        xyxy = _xywh_to_xyxy(xywh)
        # undo letterbox
        xyxy[:, [0, 2]] -= left
        xyxy[:, [1, 3]] -= top
        xyxy /= r
        # clip to original image
        xyxy[:, 0::2] = np.clip(xyxy[:, 0::2], 0, orig_w - 1)
        xyxy[:, 1::2] = np.clip(xyxy[:, 1::2], 0, orig_h - 1)

        # NMS per class (simple implementation)
        final_boxes: List[np.ndarray] = []
        final_scores: List[np.ndarray] = []
        final_cls: List[np.ndarray] = []
        for cid in np.unique(cls_ids):
            m = cls_ids == cid
            b = xyxy[m]
            s = scores[m]
            keep = _nms(b, s, self.iou_thres)
            if not keep:
                continue
            final_boxes.append(b[keep])
            final_scores.append(s[keep])
            final_cls.append(np.full(len(keep), cid, dtype=np.int32))

        if not final_boxes:
            return (np.empty((0, 4), dtype=np.float32),
                    np.empty((0,), dtype=np.float32),
                    np.empty((0,), dtype=np.int32))

        boxes = np.concatenate(final_boxes, axis=0)
        scores = np.concatenate(final_scores, axis=0)
        cls_ids = np.concatenate(final_cls, axis=0)
        return boxes, scores, cls_ids

    def detect(self, bgr: np.ndarray) -> List[Dict]:
        """
        Run detection on a BGR frame and return list of dict:
          { "cls": int_cls_id, "conf": float_conf, "bbox": (x1,y1,x2,y2) }
        """
        img, meta = self._preprocess(bgr)
        outs = self.session.run(self.out_names, {self.input_name: img})

        pred = None
        for o in outs:
            if o.ndim >= 2 and (o.shape[-1] in (84, 85) or o.shape[-2] in (84, 85) or o.shape[-1] == 85):
                pred = o
                break
        if pred is None:
            return []

        boxes, scores, cls_ids = self._post(pred, meta)
        results: List[Dict] = []
        for (x1, y1, x2, y2), s, cid in zip(boxes, scores, cls_ids):
            results.append({
                "cls": int(cid),
                "conf": float(s),
                "bbox": (float(x1), float(y1), float(x2), float(y2)),
            })
        return results

