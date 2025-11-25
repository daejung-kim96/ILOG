import cv2


class YuNetDetector:
    def __init__(self, model_path: str, input_size=(320, 320), score_threshold=0.3):
        self.model_path = model_path
        self.input_size = tuple(input_size)
        self.score_threshold = float(score_threshold)
        self.det = None
        # Prefer FaceDetectorYN if available
        if hasattr(cv2, 'FaceDetectorYN_create'):
            try:
                self.det = cv2.FaceDetectorYN_create(model_path, "", self.input_size, self.score_threshold)
            except Exception:
                self.det = None

    def detect(self, frame):
        if self.det is None:
            return []
        h, w = frame.shape[:2]
        try:
            self.det.setInputSize((w, h))
            _, faces = self.det.detect(frame)
            out = []
            if faces is not None:
                for f in faces:
                    x, y, ww, hh, score = float(f[0]), float(f[1]), float(f[2]), float(f[3]), float(f[14]) if len(f) >= 15 else float(f[4])
                    out.append({'bbox': [x, y, x+ww, y+hh], 'score': score})
            return out
        except Exception:
            return []

    @staticmethod
    def draw(frame, faces):
        import numpy as np
        out = frame
        for f in faces or []:
            try:
                x1, y1, x2, y2 = [int(v) for v in f['bbox']]
                cv2.rectangle(out, (x1,y1), (x2,y2), (0,255,0), 2)
                cv2.putText(out, f"{f.get('score',0):.2f}", (x1, max(0,y1-4)), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0,255,0), 1, cv2.LINE_AA)
            except Exception:
                pass
        return out

