package com.example.feperfectsquat.models

import java.util.Locale
import kotlin.math.roundToInt

enum class WeightUnit(val label: String) { KG("kg"), LB("lb") }

private const val LB_PER_KG = 2.20462

/** Weight shown in the user's unit: kg with at most one decimal, lb rounded to the nearest 5. */
fun formatWeightNumber(kg: Double, unit: WeightUnit): String = when (unit) {
    WeightUnit.KG -> {
        val rounded = (kg * 10).roundToInt() / 10.0
        if (rounded % 1.0 == 0.0) rounded.toInt().toString() else String.format(Locale.US, "%.1f", rounded)
    }
    WeightUnit.LB -> (((kg * LB_PER_KG) / 5).roundToInt() * 5).toString()
}

fun formatWeight(kg: Double, unit: WeightUnit): String = "${formatWeightNumber(kg, unit)} ${unit.label}"
