import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "app"))

from services.depth import DepthTracker, is_below_parallel  # noqa: E402


class IsBelowParallelTest(unittest.TestCase):
    # Image coordinates: y grows downwards.

    def test_hip_lower_than_knee_is_below_parallel(self):
        self.assertTrue(is_below_parallel(hip_y=0.62, knee_y=0.60))

    def test_hip_higher_than_knee_is_not_below_parallel(self):
        self.assertFalse(is_below_parallel(hip_y=0.55, knee_y=0.60))

    def test_hip_level_with_knee_is_not_below_parallel(self):
        self.assertFalse(is_below_parallel(hip_y=0.60, knee_y=0.60))


class DepthTrackerTest(unittest.TestCase):

    def test_depth_reached_when_below_parallel_for_consecutive_frames(self):
        tracker = DepthTracker(min_frames=2)
        tracker.update(hip_y=0.62, knee_y=0.60)
        tracker.update(hip_y=0.63, knee_y=0.60)
        self.assertTrue(tracker.depth_reached)

    def test_single_jitter_frame_does_not_reach_depth(self):
        tracker = DepthTracker(min_frames=2)
        tracker.update(hip_y=0.62, knee_y=0.60)
        tracker.update(hip_y=0.55, knee_y=0.60)
        tracker.update(hip_y=0.61, knee_y=0.60)
        self.assertFalse(tracker.depth_reached)

    def test_depth_stays_reached_after_rising_above_parallel(self):
        tracker = DepthTracker(min_frames=2)
        tracker.update(hip_y=0.62, knee_y=0.60)
        tracker.update(hip_y=0.62, knee_y=0.60)
        tracker.update(hip_y=0.40, knee_y=0.60)
        self.assertTrue(tracker.depth_reached)

    def test_reset_clears_depth_for_next_rep(self):
        tracker = DepthTracker(min_frames=2)
        tracker.update(hip_y=0.62, knee_y=0.60)
        tracker.update(hip_y=0.62, knee_y=0.60)
        tracker.reset()
        tracker.update(hip_y=0.62, knee_y=0.60)
        self.assertFalse(tracker.depth_reached)


if __name__ == "__main__":
    unittest.main()
