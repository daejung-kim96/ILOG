from __future__ import annotations

from typing import List, Optional, Tuple
import math

from vision.tracker import Track


def check_object_near_child(
    tracks: List[Track],
    baby_track: Optional[Track],
    crib_roi: Optional[Tuple[float, float, float, float]],
    speed_thresh: float = 8.0,
    dist_dec_thresh: float = 20.0,
    max_near_dist: Optional[float] = None,
    require_downward: bool = True,
) -> List[Track]:
    """
    Heuristic: any non-person track inside crib ROI that is
    - moving fast enough (speed >= speed_thresh),
    - optionally moving downwards (vy > 0 if require_downward),
    - within max_near_dist (if provided),
    - and whose distance to the baby is decreasing by more than
      dist_dec_thresh (pixels between frames),
    is treated as OBJECT_NEAR_CHILD.
    """
    events: List[Track] = []
    if baby_track is None:
        return events

    bx, by = baby_track.center

    if crib_roi is not None:
        x1, y1, x2, y2 = crib_roi
    else:
        # no ROI -> treat whole frame as crib
        x1 = y1 = float("-inf")
        x2 = y2 = float("inf")

    for t in tracks:
        # skip baby itself and other persons
        if t.id == baby_track.id or t.cls_id == 0:
            continue
        cx, cy = t.center
        if not (x1 <= cx <= x2 and y1 <= cy <= y2):
            continue
        if require_downward and t.vy <= 0.0:
            # only consider objects moving downwards towards crib
            continue
        v = t.speed
        if v < speed_thresh:
            continue
        dist = math.hypot(cx - bx, cy - by)
        if max_near_dist is not None and dist > max_near_dist:
            # too far from baby to be considered "near"
            t.prev_dist = dist
            continue
        prev = t.prev_dist
        if prev is not None and (prev - dist) > dist_dec_thresh:
            events.append(t)
        t.prev_dist = dist

    return events
