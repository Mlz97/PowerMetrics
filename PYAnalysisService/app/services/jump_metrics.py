"""Vertical jump (countermovement jump) metrics, independent from MediaPipe.

All positions are in pixels with image coordinates: y grows downwards, so a
body part going up has a smaller y. Field names mirror the API contract.
"""
import math
from dataclasses import dataclass, field
from statistics import median
from typing import List, Optional, Sequence, Tuple

GRAVITY = 9.81
DEFAULT_FPS = 60.0
MIN_RECOMMENDED_FPS = 60.0
# Hip (greater trochanter) height ~0.530 H and ankle height ~0.039 H (Drillis & Contini)
LEG_TO_STATURE_RATIO = 0.491
# Feet must rise this fraction of the leg length to count as airborne
AIRBORNE_THRESHOLD_RATIO = 0.05
MIN_FLIGHT_SECONDS = 0.1
COUNTERMOVEMENT_WINDOW_SECONDS = 1.5
STANDING_KNEE_ANGLE = 160.0


@dataclass(frozen=True)
class FrameSample:
    foot_y: Optional[float]
    hip_y: Optional[float]
    knee_angle: Optional[float]
    leg_length_px: Optional[float]


@dataclass
class JumpMetrics:
    jumpNumber: int
    flightTime: float
    heightFlightCm: float
    takeoffVelocity: float
    minKneeAngle: Optional[float] = None
    countermovementDepthCm: Optional[float] = None
    heightDisplacementCm: Optional[float] = None
    peakPowerW: Optional[float] = None


@dataclass
class JumpSessionMetrics:
    jumps: List[JumpMetrics] = field(default_factory=list)
    bestJumpHeightCm: Optional[float] = None
    avgJumpHeightCm: Optional[float] = None
    peakPowerW: Optional[float] = None
    fatigueIndex: float = 0.0


def resolve_fps(raw_fps: Optional[float]) -> float:
    if not raw_fps or math.isnan(raw_fps) or raw_fps <= 0:
        return DEFAULT_FPS
    return raw_fps


def is_low_fps(fps: float) -> bool:
    return fps < MIN_RECOMMENDED_FPS


def height_from_flight_time_m(flight_time_s: float) -> float:
    return GRAVITY * flight_time_s ** 2 / 8


def takeoff_velocity_ms(flight_time_s: float) -> float:
    return GRAVITY * flight_time_s / 2


def sayers_peak_power_w(height_m: float, body_mass_kg: float) -> float:
    return 60.7 * (height_m * 100) + 45.3 * body_mass_kg - 2055


def meters_per_pixel(leg_length_px: Optional[float], height_cm: Optional[float]) -> Optional[float]:
    if not leg_length_px or leg_length_px <= 0 or not height_cm:
        return None
    return (height_cm / 100 * LEG_TO_STATURE_RATIO) / leg_length_px


def fatigue_index(values: Sequence[float]) -> float:
    if len(values) < 2 or values[0] <= 0:
        return 0.0
    return max(0.0, 1.0 - values[-1] / values[0])


def detect_flight_phases(foot_y: Sequence[Optional[float]], ground_y: float, threshold_px: float,
                         min_frames: int) -> List[Tuple[int, int]]:
    """Returns (takeoff_frame, landing_frame) pairs; frames without feet keep the previous state."""
    phases = []
    airborne = False
    takeoff = 0
    for index, y in enumerate(foot_y):
        if y is not None:
            is_up = ground_y - y > threshold_px
            if is_up and not airborne:
                airborne, takeoff = True, index
            elif not is_up and airborne:
                airborne = False
                if index - takeoff >= min_frames:
                    phases.append((takeoff, index))
    return phases


def analyze_jumps(frames: Sequence[FrameSample], fps: float, body_weight_kg: Optional[float],
                  height_cm: Optional[float]) -> JumpSessionMetrics:
    foot_values = [f.foot_y for f in frames if f.foot_y is not None]
    leg_values = [f.leg_length_px for f in frames if f.leg_length_px]
    if not foot_values or not leg_values:
        return JumpSessionMetrics()

    ground_y = median(foot_values)
    leg_length_px = median(leg_values)
    threshold_px = leg_length_px * AIRBORNE_THRESHOLD_RATIO
    phases = detect_flight_phases([f.foot_y for f in frames], ground_y, threshold_px,
                                  min_frames=max(1, round(MIN_FLIGHT_SECONDS * fps)))

    airborne = set()
    for takeoff, landing in phases:
        airborne.update(range(takeoff, landing))
    standing_hips = [f.hip_y for i, f in enumerate(frames)
                     if i not in airborne and f.hip_y is not None
                     and f.knee_angle is not None and f.knee_angle > STANDING_KNEE_ANGLE]
    standing_hip_y = median(standing_hips) if standing_hips else None
    scale = meters_per_pixel(leg_length_px, height_cm)
    window = round(COUNTERMOVEMENT_WINDOW_SECONDS * fps)

    jumps = []
    for number, (takeoff, landing) in enumerate(phases, start=1):
        flight_time = (landing - takeoff) / fps
        height_m = height_from_flight_time_m(flight_time)
        before = frames[max(0, takeoff - window):takeoff]
        during = frames[takeoff:landing]
        knee_angles = [f.knee_angle for f in before if f.knee_angle is not None]
        hips_before = [f.hip_y for f in before if f.hip_y is not None]
        hips_during = [f.hip_y for f in during if f.hip_y is not None]

        depth_cm = displacement_cm = None
        if scale and standing_hip_y is not None:
            if hips_before:
                depth_cm = max(0.0, max(hips_before) - standing_hip_y) * scale * 100
            if hips_during:
                displacement_cm = max(0.0, standing_hip_y - min(hips_during)) * scale * 100

        jumps.append(JumpMetrics(
            jumpNumber=number,
            flightTime=flight_time,
            heightFlightCm=height_m * 100,
            takeoffVelocity=takeoff_velocity_ms(flight_time),
            minKneeAngle=min(knee_angles) if knee_angles else None,
            countermovementDepthCm=depth_cm,
            heightDisplacementCm=displacement_cm,
            peakPowerW=sayers_peak_power_w(height_m, body_weight_kg) if body_weight_kg else None,
        ))

    if not jumps:
        return JumpSessionMetrics()

    heights = [j.heightFlightCm for j in jumps]
    powers = [j.peakPowerW for j in jumps if j.peakPowerW is not None]
    return JumpSessionMetrics(
        jumps=jumps,
        bestJumpHeightCm=max(heights),
        avgJumpHeightCm=sum(heights) / len(heights),
        peakPowerW=max(powers) if powers else None,
        fatigueIndex=fatigue_index(heights),
    )
