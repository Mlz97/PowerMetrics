package com.example.feperfectsquat.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionStatsTest {

    private fun session(
        type: ExerciseType?,
        state: String = "FINISHED",
        totalReps: Int? = null,
        validReps: Int? = null,
        estimatedRM: Double? = null,
        totalJumps: Int? = null,
        bestJumpHeightCm: Double? = null
    ) = AnalysisSession(
        id = 1, exerciseType = type, state = state, createdAt = null,
        totalReps = totalReps, validReps = validReps, avgConcentricVelocity = null,
        estimatedRM = estimatedRM, fatigueIndex = null, weightKg = null, reps = null,
        videoFps = null, lowFpsWarning = null, bodyWeightKg = null, heightCm = null,
        totalJumps = totalJumps, bestJumpHeightCm = bestJumpHeightCm, avgJumpHeightCm = null,
        peakPowerW = null, jumps = null
    )

    @Test
    fun aggregatesFinishedSquatsAndJumps() {
        val stats = SessionStats.from(listOf(
            session(ExerciseType.SQUAT, totalReps = 5, validReps = 4, estimatedRM = 120.0),
            session(ExerciseType.SQUAT, totalReps = 3, validReps = 3, estimatedRM = 130.5),
            session(ExerciseType.VERTICAL_JUMP, totalJumps = 3, bestJumpHeightCm = 35.2),
            session(ExerciseType.VERTICAL_JUMP, totalJumps = 2, bestJumpHeightCm = 31.0)
        ))

        assertEquals(4, stats.finishedSessions)
        assertEquals(8, stats.totalReps)
        assertEquals(7, stats.validReps)
        assertEquals(130.5, stats.bestEstimatedRM!!, 0.0)
        assertEquals(5, stats.totalJumps)
        assertEquals(35.2, stats.bestJumpHeightCm!!, 0.0)
    }

    @Test
    fun ignoresSessionsThatAreNotFinished() {
        val stats = SessionStats.from(listOf(
            session(ExerciseType.SQUAT, state = "ERROR", totalReps = 5, estimatedRM = 200.0),
            session(ExerciseType.SQUAT, state = "PROCESSING", totalReps = 5)
        ))

        assertEquals(0, stats.finishedSessions)
        assertEquals(0, stats.totalReps)
        assertNull(stats.bestEstimatedRM)
    }

    @Test
    fun sessionWithoutTypeCountsAsSquat() {
        // Sessions created before the vertical jump existed have no exerciseType
        val stats = SessionStats.from(listOf(session(null, totalReps = 2)))

        assertEquals(2, stats.totalReps)
    }
}
