import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "app"))

from services.jump_metrics import (  # noqa: E402
    FrameSample,
    analyze_jumps,
    detect_flight_phases,
    fatigue_index,
    height_from_flight_time_m,
    is_low_fps,
    meters_per_pixel,
    resolve_fps,
    sayers_peak_power_w,
    takeoff_velocity_ms,
)

GROUND_Y = 900.0
STANDING_HIP_Y = 500.0
LEG_PX = 400.0


def standing(n):
    return [FrameSample(foot_y=GROUND_Y, hip_y=STANDING_HIP_Y, knee_angle=175.0, leg_length_px=LEG_PX)] * n


def countermovement(n, hip_drop_px=100.0, knee_angle=95.0):
    return [FrameSample(foot_y=GROUND_Y, hip_y=STANDING_HIP_Y + hip_drop_px, knee_angle=knee_angle,
                        leg_length_px=None)] * n


def flight(n, rise_px=120.0):
    return [FrameSample(foot_y=GROUND_Y - rise_px, hip_y=STANDING_HIP_Y - rise_px, knee_angle=175.0,
                        leg_length_px=None)] * n


class FpsTest(unittest.TestCase):

    def test_uses_video_fps_when_known(self):
        self.assertEqual(resolve_fps(120.0), 120.0)

    def test_defaults_to_60_when_fps_unknown(self):
        self.assertEqual(resolve_fps(0), 60.0)
        self.assertEqual(resolve_fps(float("nan")), 60.0)

    def test_warns_below_60_fps(self):
        self.assertTrue(is_low_fps(30.0))
        self.assertFalse(is_low_fps(60.0))


class FormulaTest(unittest.TestCase):

    def test_height_from_flight_time(self):
        # h = g * t^2 / 8 -> 0.5 s of flight = 0.3066 m
        self.assertAlmostEqual(height_from_flight_time_m(0.5), 0.3066, places=4)

    def test_takeoff_velocity_from_flight_time(self):
        # v = g * t / 2
        self.assertAlmostEqual(takeoff_velocity_ms(0.5), 2.4525, places=4)

    def test_sayers_peak_power(self):
        # 60.7 * 30 cm + 45.3 * 80 kg - 2055
        self.assertAlmostEqual(sayers_peak_power_w(0.30, 80.0), 3390.0, places=1)

    def test_meters_per_pixel_from_user_height(self):
        # hip->ankle = 0.491 * 1.80 m spread over 400 px
        self.assertAlmostEqual(meters_per_pixel(400.0, 180.0), 0.0022095, places=6)

    def test_meters_per_pixel_without_leg_length(self):
        self.assertIsNone(meters_per_pixel(0.0, 180.0))

    def test_fatigue_index_from_first_to_last(self):
        self.assertAlmostEqual(fatigue_index([0.40, 0.35, 0.30]), 0.25)

    def test_fatigue_index_needs_two_values(self):
        self.assertEqual(fatigue_index([0.40]), 0.0)


class DetectFlightPhasesTest(unittest.TestCase):

    def test_detects_each_flight_phase(self):
        foot_y = [900] * 10 + [800] * 30 + [900] * 10 + [790] * 28 + [900] * 5
        phases = detect_flight_phases(foot_y, ground_y=900, threshold_px=20, min_frames=6)
        self.assertEqual(phases, [(10, 40), (50, 78)])

    def test_ignores_short_landmark_jitter(self):
        foot_y = [900] * 10 + [800] * 2 + [900] * 10
        self.assertEqual(detect_flight_phases(foot_y, ground_y=900, threshold_px=20, min_frames=6), [])

    def test_missing_frames_keep_previous_state(self):
        foot_y = [900] * 5 + [800] * 10 + [None] * 3 + [800] * 10 + [900] * 5
        phases = detect_flight_phases(foot_y, ground_y=900, threshold_px=20, min_frames=6)
        self.assertEqual(phases, [(5, 28)])


class AnalyzeJumpsTest(unittest.TestCase):

    def test_single_jump_with_height_and_body_weight(self):
        # 60 fps, 30 flight frames = 0.5 s
        frames = standing(30) + countermovement(20) + flight(30) + standing(30)
        result = analyze_jumps(frames, fps=60.0, body_weight_kg=80.0, height_cm=180.0)

        self.assertEqual(len(result.jumps), 1)
        jump = result.jumps[0]
        self.assertEqual(jump.jumpNumber, 1)
        self.assertAlmostEqual(jump.flightTime, 0.5)
        self.assertAlmostEqual(jump.heightFlightCm, 30.66, places=2)
        self.assertAlmostEqual(jump.takeoffVelocity, 2.4525, places=4)
        self.assertAlmostEqual(jump.minKneeAngle, 95.0)
        # 100 px of hip drop and 120 px of hip rise at 0.0022095 m/px
        self.assertAlmostEqual(jump.countermovementDepthCm, 22.10, places=1)
        self.assertAlmostEqual(jump.heightDisplacementCm, 26.51, places=1)
        # Sayers: 60.7 * 30.656 cm + 45.3 * 80 kg - 2055
        self.assertAlmostEqual(jump.peakPowerW, 3429.8, places=0)

        self.assertEqual(result.bestJumpHeightCm, jump.heightFlightCm)
        self.assertEqual(result.avgJumpHeightCm, jump.heightFlightCm)
        self.assertEqual(result.peakPowerW, jump.peakPowerW)
        self.assertEqual(result.fatigueIndex, 0.0)

    def test_without_height_and_body_weight_scaled_metrics_are_empty(self):
        frames = standing(30) + countermovement(20) + flight(30) + standing(30)
        jump = analyze_jumps(frames, fps=60.0, body_weight_kg=None, height_cm=None).jumps[0]

        self.assertAlmostEqual(jump.heightFlightCm, 30.66, places=2)
        self.assertIsNone(jump.countermovementDepthCm)
        self.assertIsNone(jump.heightDisplacementCm)
        self.assertIsNone(jump.peakPowerW)

    def test_fatigue_across_several_jumps(self):
        frames = (standing(30) + countermovement(20) + flight(30) + standing(30)
                  + countermovement(20) + flight(24) + standing(30))
        result = analyze_jumps(frames, fps=60.0, body_weight_kg=None, height_cm=None)

        self.assertEqual(len(result.jumps), 2)
        self.assertEqual(result.bestJumpHeightCm, result.jumps[0].heightFlightCm)
        # height scales with t^2: 1 - (24/30)^2 = 0.36
        self.assertAlmostEqual(result.fatigueIndex, 0.36, places=2)

    def test_no_jumps_detected(self):
        result = analyze_jumps(standing(60), fps=60.0, body_weight_kg=80.0, height_cm=180.0)

        self.assertEqual(result.jumps, [])
        self.assertIsNone(result.bestJumpHeightCm)
        self.assertIsNone(result.avgJumpHeightCm)
        self.assertIsNone(result.peakPowerW)
        self.assertEqual(result.fatigueIndex, 0.0)


if __name__ == "__main__":
    unittest.main()
