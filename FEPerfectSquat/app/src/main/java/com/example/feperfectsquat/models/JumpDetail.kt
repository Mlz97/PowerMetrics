package com.example.feperfectsquat.models

data class JumpDetail(
    val jumpNumber: Int,
    val flightTime: Double?,
    val heightFlightCm: Double?,
    val takeoffVelocity: Double?,
    val minKneeAngle: Double?,
    val countermovementDepthCm: Double?,
    val heightDisplacementCm: Double?,
    val peakPowerW: Double?
)
