package com.example.feperfectsquat.models

import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val GRAVITY = 9.81

/** Sayers equation: peak power (W) from jump height and bodyweight. */
fun sayersPeakPowerW(jumpHeightCm: Double, bodyWeightKg: Double): Int =
    (60.7 * jumpHeightCm + 45.3 * bodyWeightKg - 2055).roundToInt()

/** Flight time (ms) of a jump of the given height: t = 2 * sqrt(2h / g). */
fun flightTimeMs(jumpHeightCm: Double): Int =
    (2 * sqrt((2 * jumpHeightCm / 100) / GRAVITY) * 1000).roundToInt()
