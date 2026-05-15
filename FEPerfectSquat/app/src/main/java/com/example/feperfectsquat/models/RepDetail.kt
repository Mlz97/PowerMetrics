package com.example.feperfectsquat.models

data class RepDetail(
    val repNumber: Int,
    val valid: Boolean,
    val minHipAngle: Double?,
    val minKneeAngle: Double?,
    val concentricVelocity: Double?
)
