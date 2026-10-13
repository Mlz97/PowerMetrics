"""Powerlifting depth rule: the hip must break the parallel of the knee.

MediaPipe landmarks use image coordinates where y grows downwards, so the
hip is below the knee when its y is greater. Joint centers are used as an
approximation of the hip crease and the top of the knee.
"""


def is_below_parallel(hip_y: float, knee_y: float) -> bool:
    return hip_y > knee_y


class DepthTracker:
    """Tracks whether a rep reached depth, ignoring single-frame landmark jitter."""

    def __init__(self, min_frames: int = 2):
        self.min_frames = min_frames
        self.depth_reached = False
        self._consecutive_frames = 0

    def update(self, hip_y: float, knee_y: float) -> None:
        if is_below_parallel(hip_y, knee_y):
            self._consecutive_frames += 1
            if self._consecutive_frames >= self.min_frames:
                self.depth_reached = True
        else:
            self._consecutive_frames = 0

    def reset(self) -> None:
        self.depth_reached = False
        self._consecutive_frames = 0
