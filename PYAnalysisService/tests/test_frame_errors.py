import logging
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "app"))

from services.frame_errors import FrameErrorLog  # noqa: E402

LOGGER_NAME = "test.frame_errors"


def _fail(message):
    try:
        raise ValueError(message)
    except ValueError as exc:
        return exc


class FrameErrorLogTest(unittest.TestCase):

    def test_first_failure_is_logged_with_traceback(self):
        errors = FrameErrorLog(logging.getLogger(LOGGER_NAME))

        with self.assertLogs(LOGGER_NAME, level="WARNING") as logs:
            errors.record(frame_index=7, error=_fail("no landmarks"))

        self.assertEqual(len(logs.records), 1)
        self.assertIn("frame 7", logs.records[0].getMessage())
        self.assertIn("no landmarks", logs.records[0].getMessage())
        self.assertIsNotNone(logs.records[0].exc_info)

    def test_repeated_failures_are_only_counted(self):
        errors = FrameErrorLog(logging.getLogger(LOGGER_NAME))

        with self.assertLogs(LOGGER_NAME, level="WARNING") as logs:
            for frame in range(5):
                errors.record(frame_index=frame, error=_fail("boom"))

        self.assertEqual(len(logs.records), 1)
        self.assertEqual(errors.count, 5)

    def test_summary_reports_total_failed_frames(self):
        errors = FrameErrorLog(logging.getLogger(LOGGER_NAME))
        with self.assertLogs(LOGGER_NAME, level="WARNING"):
            errors.record(frame_index=0, error=_fail("a"))
            errors.record(frame_index=1, error=_fail("b"))

        with self.assertLogs(LOGGER_NAME, level="WARNING") as logs:
            errors.log_summary(total_frames=10)

        self.assertIn("2 of 10 frames", logs.records[0].getMessage())

    def test_summary_is_silent_without_failures(self):
        errors = FrameErrorLog(logging.getLogger(LOGGER_NAME))

        with self.assertNoLogs(LOGGER_NAME, level="WARNING"):
            errors.log_summary(total_frames=10)


if __name__ == "__main__":
    unittest.main()
