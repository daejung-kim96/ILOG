"""
Simple multi-object tracker for the AI worker.

This is intentionally lightweight: IoU-based association only.
It keeps per-track velocity so downstream rules (WAKE_UP,
OBJECT_NEAR_CHILD, etc.) can reason about speed and direction.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import List, Dict, Tuple, Optional

import math


def _iou(box_a: Tuple[float, float, float, float],
         box_b: Tuple[float, float, float, float]) -> float:
    ax1, ay1, ax2, ay2 = box_a
    bx1, by1, bx2, by2 = box_b
    inter_x1 = max(ax1, bx1)
    inter_y1 = max(ay1, by1)
    inter_x2 = min(ax2, bx2)
    inter_y2 = min(ay2, by2)
    w = max(0.0, inter_x2 - inter_x1)
    h = max(0.0, inter_y2 - inter_y1)
    inter = w * h
    if inter <= 0.0:
        return 0.0
    area_a = max(0.0, ax2 - ax1) * max(0.0, ay2 - ay1)
    area_b = max(0.0, bx2 - bx1) * max(0.0, by2 - by1)
    denom = area_a + area_b - inter
    if denom <= 0.0:
        return 0.0
    return inter / denom


@dataclass
class Track:
    id: int
    cls_id: int
    bbox: Tuple[float, float, float, float]
    vx: float = 0.0
    vy: float = 0.0
    miss: int = 0
    prev_cx: float | None = None
    prev_cy: float | None = None
    prev_dist: float | None = None

    @property
    def center(self) -> Tuple[float, float]:
        x1, y1, x2, y2 = self.bbox
        return (0.5 * (x1 + x2), 0.5 * (y1 + y2))

    @property
    def speed(self) -> float:
        return math.hypot(self.vx, self.vy)


class SimpleTracker:
    """
    Very small IoU-based tracker.

    - `update(detections)` accepts list of dicts with keys:
        { "cls": int, "conf": float, "bbox": (x1,y1,x2,y2) }
    - Returns list[Track] with stable ids and simple velocity.
    """

    def __init__(self, max_miss: int = 10, iou_thresh: float = 0.3) -> None:
        self.max_miss = max_miss
        self.iou_thresh = iou_thresh
        self._next_id = 1
        self._tracks: Dict[int, Track] = {}

    def _new_track(self, det: Dict) -> Track:
        tid = self._next_id
        self._next_id += 1
        bbox = det["bbox"]
        cls_id = int(det.get("cls", -1))
        cx, cy = 0.5 * (bbox[0] + bbox[2]), 0.5 * (bbox[1] + bbox[3])
        t = Track(id=tid, cls_id=cls_id, bbox=bbox, prev_cx=cx, prev_cy=cy)
        self._tracks[tid] = t
        return t

    def update(self, detections: List[Dict]) -> List[Track]:
        # Current active track ids
        track_ids = list(self._tracks.keys())

        # Build IoU matrix tracks x detections
        ious: List[List[float]] = []
        for tid in track_ids:
            t = self._tracks[tid]
            row = []
            for det in detections:
                row.append(_iou(t.bbox, det["bbox"]))
            ious.append(row)

        # For greedy matching we keep sets of unmatched indices
        unmatched_tracks = set(track_ids)
        unmatched_dets = set(range(len(detections)))

        # Greedy association: repeatedly pick best IoU pair
        while unmatched_tracks and unmatched_dets:
            best_iou = self.iou_thresh
            best_tid = None
            best_didx = None
            for ti, tid in enumerate(track_ids):
                if tid not in unmatched_tracks:
                    continue
                row = ious[ti]
                for didx in unmatched_dets:
                    v = row[didx]
                    if v > best_iou:
                        best_iou = v
                        best_tid = tid
                        best_didx = didx
            if best_tid is None or best_didx is None:
                break

            # Match found
            unmatched_tracks.remove(best_tid)
            unmatched_dets.remove(best_didx)

            det = detections[best_didx]
            t = self._tracks[best_tid]
            # update velocity
            cx_prev, cy_prev = t.center
            x1, y1, x2, y2 = det["bbox"]
            cx = 0.5 * (x1 + x2)
            cy = 0.5 * (y1 + y2)
            t.vx = cx - cx_prev
            t.vy = cy - cy_prev
            t.prev_cx = cx_prev
            t.prev_cy = cy_prev
            t.bbox = det["bbox"]
            t.cls_id = int(det.get("cls", t.cls_id))
            t.miss = 0

        # Unmatched detections -> new tracks
        for didx in unmatched_dets:
            self._new_track(detections[didx])

        # Unmatched tracks -> increase miss
        for tid in list(unmatched_tracks):
            t = self._tracks.get(tid)
            if t is None:
                continue
            t.miss += 1
            if t.miss > self.max_miss:
                self._tracks.pop(tid, None)

        return list(self._tracks.values())


def pick_baby_track(tracks: List[Track],
                    crib_roi: Optional[Tuple[float, float, float, float]],
                    person_cls_id: int = 0) -> Optional[Track]:
    """
    Heuristic: among person tracks, pick the one inside crib_roi if possible,
    otherwise the closest one to its center.
    """
    persons = [t for t in tracks if t.cls_id == person_cls_id]
    if not persons:
        return None
    if crib_roi is None:
        # just pick the largest-area person
        return max(persons, key=lambda t: (t.bbox[2] - t.bbox[0]) * (t.bbox[3] - t.bbox[1]))

    x1, y1, x2, y2 = crib_roi
    cx_roi = 0.5 * (x1 + x2)
    cy_roi = 0.5 * (y1 + y2)

    inside: List[Track] = []
    for t in persons:
        cx, cy = t.center
        if x1 <= cx <= x2 and y1 <= cy <= y2:
            inside.append(t)
    if inside:
        # pick the one closest to crib center
        return min(inside, key=lambda t: math.hypot(t.center[0] - cx_roi, t.center[1] - cy_roi))

    # fallback: closest person to crib center
    return min(persons, key=lambda t: math.hypot(t.center[0] - cx_roi, t.center[1] - cy_roi))

