"""Logs failures inside a per-frame loop without flooding the log.

Only the first failure is logged with its traceback; the rest are counted and
reported once at the end. Going through `logging` instead of `print` is also safe
when uvicorn's stdout pipe is gone (WinError 233): handlers swallow write errors.
"""

import logging


class FrameErrorLog:
    def __init__(self, logger: logging.Logger):
        self.logger = logger
        self.count = 0

    def record(self, frame_index: int, error: Exception) -> None:
        self.count += 1
        if self.count == 1:
            self.logger.warning("Frame analysis failed at frame %d: %s", frame_index, error, exc_info=error)
        else:
            self.logger.debug("Frame analysis failed at frame %d: %s", frame_index, error)

    def log_summary(self, total_frames: int) -> None:
        if self.count:
            self.logger.warning("Frame analysis failed in %d of %d frames", self.count, total_frames)
