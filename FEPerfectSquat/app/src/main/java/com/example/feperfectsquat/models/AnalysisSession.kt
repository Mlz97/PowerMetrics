package com.example.feperfectsquat.models

data class AnalysisSession(
    val id: Long,
    val exerciseType: ExerciseType?,
    val state: String,
    val createdAt: String?,
    val totalReps: Int?,
    val validReps: Int?,
    val avgConcentricVelocity: Double?,
    val estimatedRM: Double?,
    val fatigueIndex: Double?,
    val weightKg: Int?,
    val videoFps: Double?,
    val lowFpsWarning: Boolean?,
    val bodyWeightKg: Double?,
    val heightCm: Double?,
    val totalJumps: Int?,
    val bestJumpHeightCm: Double?,
    val avgJumpHeightCm: Double?,
    val peakPowerW: Double?,
    val jumps: List<JumpDetail>?
)
