package com.example.feperfectsquat.models

import java.util.Locale
import kotlin.math.roundToInt

/** One athlete on a community board. Squat entries carry load/velocity/power, jump entries carry height. */
data class CommunityEntry(
    val name: String,
    val bodyWeightKg: Double,
    val loadKg: Double? = null,
    val meanVelocity: Double? = null,
    val powerW: Double? = null,
    val jumpHeightCm: Double? = null
) {
    val initials: String get() = name.split(' ').filter { it.isNotBlank() }.joinToString("") { it.first().toString() }
}

enum class CommunityMetric(val lift: ExerciseType, val label: String) {
    LOAD(ExerciseType.SQUAT, "Load"),
    RELATIVE(ExerciseType.SQUAT, "Relative"),
    MEAN_VELOCITY(ExerciseType.SQUAT, "Mean vel."),
    POWER(ExerciseType.SQUAT, "Power"),
    HEIGHT(ExerciseType.VERTICAL_JUMP, "Height"),
    JUMP_POWER(ExerciseType.VERTICAL_JUMP, "Power"),
    POWER_PER_KG(ExerciseType.VERTICAL_JUMP, "W/kg"),
    FLIGHT_TIME(ExerciseType.VERTICAL_JUMP, "Flight time");

    fun value(e: CommunityEntry): Double = when (this) {
        LOAD -> e.loadKg ?: 0.0
        RELATIVE -> (e.loadKg ?: 0.0) / e.bodyWeightKg
        MEAN_VELOCITY -> e.meanVelocity ?: 0.0
        POWER -> e.powerW ?: 0.0
        HEIGHT -> e.jumpHeightCm ?: 0.0
        JUMP_POWER -> sayersPeakPowerW(e.jumpHeightCm ?: 0.0, e.bodyWeightKg).toDouble()
        POWER_PER_KG -> sayersPeakPowerW(e.jumpHeightCm ?: 0.0, e.bodyWeightKg) / e.bodyWeightKg
        FLIGHT_TIME -> flightTimeMs(e.jumpHeightCm ?: 0.0).toDouble()
    }

    fun format(e: CommunityEntry, unit: WeightUnit): String = when (this) {
        LOAD -> formatWeight(value(e), unit)
        RELATIVE -> "%.2f×BW".format(Locale.US, value(e))
        MEAN_VELOCITY -> "%.2f m/s".format(Locale.US, value(e))
        POWER, JUMP_POWER -> "%,d W".format(Locale.US, value(e).roundToInt())
        HEIGHT -> "%.1f cm".format(Locale.US, value(e))
        POWER_PER_KG -> "%.1f W/kg".format(Locale.US, value(e))
        FLIGHT_TIME -> "${value(e).roundToInt()} ms"
    }

    companion object {
        fun forLift(lift: ExerciseType) = entries.filter { it.lift == lift }
    }
}

fun rank(entries: List<CommunityEntry>, metric: CommunityMetric): List<CommunityEntry> =
    entries.sortedByDescending { metric.value(it) }
