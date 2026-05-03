package com.example.feperfectsquat.models

data class AnalysisSession(
    val id: Long,
    val date: String,
    val score: Double,
    val video: Video?
)
