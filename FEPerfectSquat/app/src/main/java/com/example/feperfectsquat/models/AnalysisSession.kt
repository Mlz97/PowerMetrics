package com.example.feperfectsquat.models

data class AnalysisSession(
    val id: Long,
    val state: String,
    val createdAt: String?,
    val totalReps: Int?,
    val validReps: Int?,
    val avgConcentricVelocity: Double?,
    val estimatedRM: Double?,
    val fatigueIndex: Double?,
    val weightKg: Int?
)
