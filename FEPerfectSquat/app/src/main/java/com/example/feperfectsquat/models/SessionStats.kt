package com.example.feperfectsquat.models

/** Totals shown on the statistics screen; only finished sessions count. */
data class SessionStats(
    val finishedSessions: Int,
    val totalReps: Int,
    val validReps: Int,
    val bestEstimatedRM: Double?,
    val totalJumps: Int,
    val bestJumpHeightCm: Double?
) {
    companion object {
        fun from(sessions: List<AnalysisSession>): SessionStats {
            val finished = sessions.filter { it.state == "FINISHED" }
            val squats = finished.filter { it.exerciseType != ExerciseType.VERTICAL_JUMP }
            val jumps = finished.filter { it.exerciseType == ExerciseType.VERTICAL_JUMP }
            return SessionStats(
                finishedSessions = finished.size,
                totalReps = squats.sumOf { it.totalReps ?: 0 },
                validReps = squats.sumOf { it.validReps ?: 0 },
                bestEstimatedRM = squats.mapNotNull { it.estimatedRM }.maxOrNull(),
                totalJumps = jumps.sumOf { it.totalJumps ?: 0 },
                bestJumpHeightCm = jumps.mapNotNull { it.bestJumpHeightCm }.maxOrNull()
            )
        }
    }
}
