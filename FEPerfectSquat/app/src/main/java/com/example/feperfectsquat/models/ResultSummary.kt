package com.example.feperfectsquat.models

import java.util.Locale
import kotlin.math.roundToInt

/** How a rep/jump bar is highlighted in the result sheet. */
enum class BarTone { TOP, NORMAL, LOW, INVALID }

data class ResultBar(val label: String, val fraction: Float, val tone: BarTone)
data class ResultStat(val label: String, val value: String)

/** Everything the Analyze result sheet shows for a finished session. */
data class ResultSummary(val title: String, val note: String, val bars: List<ResultBar>, val stats: List<ResultStat>)

// Velocity loss above this means the set went close to failure
private const val HIGH_VELOCITY_LOSS = 0.25
// A rep slower than this share of the first one is drawn as slow
private const val SLOW_REP_SHARE = 0.85
// Leaves room above the tallest bar for its value label
private const val MAX_BAR = 0.8f

private const val NO_DATA = "—"

fun summarize(session: AnalysisSession, unit: WeightUnit): ResultSummary =
    if (session.isJump()) summarizeJump(session) else summarizeSquat(session, unit)

private fun summarizeSquat(session: AnalysisSession, unit: WeightUnit): ResultSummary {
    val reps = session.reps.orEmpty()
    val velocities = reps.map { it.concentricVelocity ?: 0.0 }
    val first = velocities.firstOrNull() ?: 0.0
    val max = velocities.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val bars = reps.mapIndexed { i, rep ->
        val v = velocities[i]
        val tone = when {
            !rep.valid -> BarTone.INVALID
            i == 0 -> BarTone.TOP
            v < first * SLOW_REP_SHARE -> BarTone.LOW
            else -> BarTone.NORMAL
        }
        ResultBar("%.2f".format(Locale.US, v), (v / max).toFloat() * MAX_BAR, tone)
    }

    val advice = session.fatigueIndex?.let {
        if (it < HIGH_VELOCITY_LOSS) "Bar speed held up well — room for another set." else "Speed dropped a lot — good place to stop."
    }
    val note = listOfNotNull(session.weightKg?.let { formatWeight(it, unit) }, advice).joinToString(" · ")

    return ResultSummary(
        title = "Nice set! ${session.totalReps ?: reps.size} reps · Squat",
        note = note,
        bars = bars,
        stats = listOf(
            // Backend velocity is normalised to body size (1/s)
            ResultStat("Mean vel.", session.avgConcentricVelocity?.let { "%.2f 1/s".format(Locale.US, it) } ?: NO_DATA),
            ResultStat("Est. 1RM", session.estimatedRM?.let { formatWeight(it, unit) } ?: NO_DATA),
            ResultStat("Vel. loss", session.fatigueIndex?.let { "${(it * 100).roundToInt()}%" } ?: NO_DATA),
        )
    )
}

private fun summarizeJump(session: AnalysisSession): ResultSummary {
    val jumps = session.jumps.orEmpty()
    val heights = jumps.map { it.heightFlightCm ?: 0.0 }
    val bestIndex = heights.indices.maxByOrNull { heights[it] } ?: -1
    val max = heights.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val bars = heights.mapIndexed { i, h ->
        ResultBar("%.1f".format(Locale.US, h), (h / max).toFloat() * MAX_BAR, if (i == bestIndex) BarTone.TOP else BarTone.NORMAL)
    }

    val best = session.bestJumpHeightCm
    val flightMs = jumps.getOrNull(bestIndex)?.flightTime?.let { (it * 1000).roundToInt() } ?: best?.let { flightTimeMs(it) }
    return ResultSummary(
        title = "${session.totalJumps ?: jumps.size} jumps · Vertical jump",
        note = "Your best jump counts towards your record.",
        bars = bars,
        stats = listOf(
            ResultStat("Best height", best?.let { "%.1f cm".format(Locale.US, it) } ?: NO_DATA),
            ResultStat("Flight time", flightMs?.let { "$it ms" } ?: NO_DATA),
            ResultStat("Peak power", session.peakPowerW?.let { "%,d W".format(Locale.US, it.roundToInt()) } ?: NO_DATA),
        )
    )
}
