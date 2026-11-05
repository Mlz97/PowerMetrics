package com.example.feperfectsquat.models

/** Builds an [AnalysisSession] with only the fields a test cares about. */
fun testSession(
    id: Long = 1,
    type: ExerciseType? = ExerciseType.SQUAT,
    state: String = "FINISHED",
    createdAt: String? = "2026-10-20T10:00:00",
    totalReps: Int? = null,
    validReps: Int? = null,
    avgConcentricVelocity: Double? = null,
    estimatedRM: Double? = null,
    fatigueIndex: Double? = null,
    weightKg: Double? = null,
    reps: List<RepDetail>? = null,
    totalJumps: Int? = null,
    bestJumpHeightCm: Double? = null,
    peakPowerW: Double? = null,
    jumps: List<JumpDetail>? = null
) = AnalysisSession(
    id = id, exerciseType = type, state = state, createdAt = createdAt,
    totalReps = totalReps, validReps = validReps, avgConcentricVelocity = avgConcentricVelocity,
    estimatedRM = estimatedRM, fatigueIndex = fatigueIndex, weightKg = weightKg, reps = reps,
    videoFps = null, lowFpsWarning = null, bodyWeightKg = null, heightCm = null,
    totalJumps = totalJumps, bestJumpHeightCm = bestJumpHeightCm, avgJumpHeightCm = null,
    peakPowerW = peakPowerW, jumps = jumps
)
