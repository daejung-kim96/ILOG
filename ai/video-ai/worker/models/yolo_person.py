"""
YOLO (person) minimal ONNXRuntime wrapper

Assumptions
- Model is YOLOv8n exported to ONNX (commonly 1x84x8400, 1x8400x84, or 1x25200x85).
- Class index for 'person' is 0.

Outputs
- list[dict]: {bbox:[x1,y1,x2,y2], score:float}
"""
from typing import List, Tuple, Dict
import numpy as np

try:
    import onnxruntime as ort
except Exception as e:  # pragma: no cover
    ort = None


def _xywh_to_xyxy(xywh: np.ndarray) -> np.ndarray:
    x, y, w, h = xywh.T
    x1 = x - w / 2
    y1 = y - h / 2
    x2 = x + w / 2
    y2 = y + h / 2
    return np.stack([x1, y1, x2, y2], axis=1)


def _nms(boxes: np.ndarray, scores: np.ndarray, iou_thres: float, top_k: int = 300) -> List[int]:
    if boxes.size == 0:
        return []
    idxs = scores.argsort()[::-1]
    keep = []
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


class YoloPerson:
    def __init__(self, model_path: str, input_size: Tuple[int, int] = (640, 640),
                 conf_thres: float = 0.25, iou_thres: float = 0.45):
        if ort is None:
            raise RuntimeError("onnxruntime not installed")
        self.session = ort.InferenceSession(model_path, providers=["CPUExecutionProvider"])
        self.input_name = self.session.get_inputs()[0].name
        self.out_names = [o.name for o in self.session.get_outputs()]
        self.input_size = tuple(int(x) for x in input_size)
        self.conf_thres = float(conf_thres)
        self.iou_thres = float(iou_thres)

    def _preprocess(self, bgr: np.ndarray) -> Tuple[np.ndarray, Tuple[float, float, float, float]]:
        h, w = bgr.shape[:2]
        dw, dh = self.input_size
        r = min(dw / w, dh / h)
        nw, nh = int(w * r), int(h * r)
        resized = cv2.resize(bgr, (nw, nh))
        pad_w, pad_h = dw - nw, dh - nh
        top, bottom = pad_h // 2, pad_h - pad_h // 2
        left, right = pad_w // 2, pad_w - pad_w // 2
        padded = cv2.copyMakeBorder(resized, top, bottom, left, right, cv2.BORDER_CONSTANT, value=(114, 114, 114))
        img = padded[:, :, ::-1].astype(np.float32) / 255.0  # BGR->RGB
        img = np.transpose(img, (2, 0, 1))[None, ...]
        meta = (r, left, top, w, h)
        return img, meta

    def _post(self, out: np.ndarray, meta: Tuple[float, float, float, float]) -> Tuple[np.ndarray, np.ndarray]:
        r, left, top, orig_w, orig_h = meta
        # normalize shapes
        if out.ndim == 3:
            out = out.squeeze(0)
        if out.shape[0] in (84, 85):  # 84x8400
            out = out.T  # -> 8400x84/85
        # out: Nx(84|85)
        xywh = out[:, :4]
        if out.shape[1] == 85:
            obj = out[:, 4]
            cls = out[:, 5:]
            person = cls[:, 0]
            conf = obj * person
        else:  # 84 (YOLOv8 class scores only)
            cls = out[:, 4:]
            conf = cls[:, 0]

        # threshold
        m = conf >= self.conf_thres
        if not np.any(m):
            return np.empty((0, 4), dtype=np.float32), np.empty((0,), dtype=np.float32)
        xywh = xywh[m]
        conf = conf[m]

        # to xyxy on padded image
        xyxy = _xywh_to_xyxy(xywh)
        # undo letterbox
        xyxy[:, [0, 2]] -= left
        xyxy[:, [1, 3]] -= top
        xyxy /= r
        # clip to image
        xyxy[:, 0::2] = np.clip(xyxy[:, 0::2], 0, orig_w - 1)
        xyxy[:, 1::2] = np.clip(xyxy[:, 1::2], 0, orig_h - 1)

        # NMS
        keep = _nms(xyxy, conf, self.iou_thres)
        return xyxy[keep], conf[keep]

    def detect(self, bgr: np.ndarray) -> List[Dict]:
        img, meta = self._preprocess(bgr)
        outs = self.session.run(self.out_names, {self.input_name: img})
        # pick first output tensor that looks like predictions
        pred = None
        for o in outs:
            if o.ndim >= 2 and (o.shape[-1] in (84, 85) or o.shape[-2] in (84, 85) or o.shape[-1] == 85):
                pred = o
                break
        if pred is None:
            return []
        boxes, scores = self._post(pred, meta)
        return [
            {"bbox": [float(x1), float(y1), float(x2), float(y2)], "score": float(s)}
            for (x1, y1, x2, y2), s in zip(boxes, scores)
        ]

try:
    import cv2  # noqa: E402
except Exception:
    # If cv2 import fails earlier, raise in constructor
    pass

