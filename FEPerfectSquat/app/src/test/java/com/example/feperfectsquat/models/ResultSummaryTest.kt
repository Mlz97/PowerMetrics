package com.example.feperfectsquat.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultSummaryTest {

    private fun rep(n: Int, v: Double?, valid: Boolean = true) = RepDetail(n, valid, null, null, v)
    private fun jump(n: Int, cm: Double, flight: Double? = null) = JumpDetail(n, flight, cm, null, null, null, null, null)

    @Test
    fun squatSummaryShowsRepsLoadAndVelocityLoss() {
        val summary = summarize(
            testSession(
                totalReps = 3, validReps = 3, weightKg = 120.0, avgConcentricVelocity = 0.55,
                estimatedRM = 140.0, fatigueIndex = 0.12,
                reps = listOf(rep(1, 0.60), rep(2, 0.55), rep(3, 0.50))
            ),
            WeightUnit.KG
        )

        assertEquals("Nice set! 3 reps · Squat", summary.title)
        assertEquals("120 kg · Bar speed held up well — room for another set.", summary.note)
        assertEquals(listOf("0.60", "0.55", "0.50"), summary.bars.map { it.label })
        assertEquals(BarTone.TOP, summary.bars.first().tone)
        assertEquals(
            listOf("Mean vel." to "0.55 1/s", "Est. 1RM" to "140 kg", "Vel. loss" to "12%"),
            summary.stats.map { it.label to it.value }
        )
    }

    @Test
    fun bigVelocityLossSuggestsStopping() {
        val summary = summarize(testSession(weightKg = 100.0, fatigueIndex = 0.31, totalReps = 1, reps = listOf(rep(1, 0.4))), WeightUnit.KG)

        assertTrue(summary.note.endsWith("Speed dropped a lot — good place to stop."))
    }

    @Test
    fun slowAndInvalidRepsGetTheirOwnTone() {
        val bars = summarize(
            testSession(reps = listOf(rep(1, 0.60), rep(2, 0.50), rep(3, 0.58, valid = false))),
            WeightUnit.KG
        ).bars

        // 0.50 is below 85% of the first rep (0.51)
        assertEquals(listOf(BarTone.TOP, BarTone.LOW, BarTone.INVALID), bars.map { it.tone })
    }

    @Test
    fun jumpSummaryHighlightsTheBestJump() {
        val summary = summarize(
            testSession(
                type = ExerciseType.VERTICAL_JUMP, totalJumps = 3, bestJumpHeightCm = 41.2, peakPowerW = 4160.0,
                jumps = listOf(jump(1, 38.5), jump(2, 41.2, flight = 0.58), jump(3, 40.1))
            ),
            WeightUnit.KG
        )

        assertEquals("3 jumps · Vertical jump", summary.title)
        assertEquals(listOf(BarTone.NORMAL, BarTone.TOP, BarTone.NORMAL), summary.bars.map { it.tone })
        assertEquals(
            listOf("Best height" to "41.2 cm", "Flight time" to "580 ms", "Peak power" to "4,160 W"),
            summary.stats.map { it.label to it.value }
        )
    }
}
