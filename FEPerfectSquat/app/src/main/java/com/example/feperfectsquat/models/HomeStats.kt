package com.example.feperfectsquat.models

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val STATS_WINDOW_MS = 30L * 24 * 60 * 60 * 1000
private const val CHART_POINTS = 8

/** Parses the backend's ISO local date-time ("2026-10-20T10:15:30.123"); null when missing or malformed. */
fun parseCreatedAt(createdAt: String?): Date? {
    if (createdAt == null || createdAt.length < 19) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(createdAt.take(19))
    } catch (e: ParseException) {
        null
    }
}

fun AnalysisSession.isJump() = exerciseType == ExerciseType.VERTICAL_JUMP

private fun List<AnalysisSession>.finishedOf(lift: ExerciseType) =
    filter { it.state == "FINISHED" && (it.exerciseType ?: ExerciseType.SQUAT) == lift }

data class SquatStats(
    val best1RmKg: Double?,
    val previousBest1RmKg: Double?,
    val bestVelocity: Double?,
    val previousBestVelocity: Double?,
    val validReps: Int
)

data class JumpStats(
    val bestHeightCm: Double?,
    val previousBestHeightCm: Double?,
    val bestPeakPowerW: Double?,
    val previousBestPeakPowerW: Double?
)

/** Bests of the last 30 days for the Home panel, each with the best before that window to show progress. */
data class HomeStats(val squat: SquatStats, val jump: JumpStats) {
    companion object {
        fun from(sessions: List<AnalysisSession>, now: Date): HomeStats {
            val windowStart = now.time - STATS_WINDOW_MS
            fun List<AnalysisSession>.split(): Pair<List<AnalysisSession>, List<AnalysisSession>> {
                val dated = mapNotNull { s -> parseCreatedAt(s.createdAt)?.let { s to it.time } }
                return dated.filter { it.second >= windowStart }.map { it.first } to
                    dated.filter { it.second < windowStart }.map { it.first }
            }

            val (squats, oldSquats) = sessions.finishedOf(ExerciseType.SQUAT).split()
            val (jumps, oldJumps) = sessions.finishedOf(ExerciseType.VERTICAL_JUMP).split()
            return HomeStats(
                squat = SquatStats(
                    best1RmKg = squats.mapNotNull { it.estimatedRM }.maxOrNull(),
                    previousBest1RmKg = oldSquats.mapNotNull { it.estimatedRM }.maxOrNull(),
                    bestVelocity = squats.mapNotNull { it.avgConcentricVelocity }.maxOrNull(),
                    previousBestVelocity = oldSquats.mapNotNull { it.avgConcentricVelocity }.maxOrNull(),
                    validReps = squats.sumOf { it.validReps ?: 0 }
                ),
                jump = JumpStats(
                    bestHeightCm = jumps.mapNotNull { it.bestJumpHeightCm }.maxOrNull(),
                    previousBestHeightCm = oldJumps.mapNotNull { it.bestJumpHeightCm }.maxOrNull(),
                    bestPeakPowerW = jumps.mapNotNull { it.peakPowerW }.maxOrNull(),
                    previousBestPeakPowerW = oldJumps.mapNotNull { it.peakPowerW }.maxOrNull()
                )
            )
        }
    }
}

/** Most recent finished session of any lift, for the Home "Historic" card. */
fun lastFinishedSession(sessions: List<AnalysisSession>): AnalysisSession? =
    sessions.filter { it.state == "FINISHED" }.maxByOrNull { parseCreatedAt(it.createdAt)?.time ?: Long.MIN_VALUE }

/** Finished sessions of one lift, newest first. */
fun sessionsOf(sessions: List<AnalysisSession>, lift: ExerciseType): List<AnalysisSession> =
    sessions.finishedOf(lift).sortedByDescending { parseCreatedAt(it.createdAt)?.time ?: Long.MIN_VALUE }

/** Progress chart of the Historic tab: estimated 1RM (squat) or best height (jump) of the last 8 sessions, oldest first. */
fun historyChart(sessions: List<AnalysisSession>, lift: ExerciseType): List<Double> =
    sessionsOf(sessions, lift)
        .mapNotNull { if (lift == ExerciseType.VERTICAL_JUMP) it.bestJumpHeightCm else it.estimatedRM }
        .take(CHART_POINTS)
        .reversed()
