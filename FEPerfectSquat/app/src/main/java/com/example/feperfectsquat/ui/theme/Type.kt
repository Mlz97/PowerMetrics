package com.example.feperfectsquat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// The design uses Archivo (wide) and JetBrains Mono; system sans and monospace stand in for them
val PmSans = FontFamily.SansSerif
val PmMono = FontFamily.Monospace

/** Big condensed-look headings ("Squat smarter.", screen titles). */
fun pmDisplay(size: TextUnit) = TextStyle(
    fontFamily = PmSans,
    fontWeight = FontWeight.ExtraBold,
    fontSize = size,
    lineHeight = size * 1.05f,
    letterSpacing = (-0.02).em
)

/** Numbers and metric values. */
fun pmMono(size: TextUnit, weight: FontWeight = FontWeight.Bold) = TextStyle(
    fontFamily = PmMono,
    fontWeight = weight,
    fontSize = size
)

/** Small uppercase mono labels ("FILMING TIPS", "RANK BY"). */
fun pmEyebrow(size: TextUnit = 11.sp) = TextStyle(
    fontFamily = PmMono,
    fontWeight = FontWeight.SemiBold,
    fontSize = size,
    letterSpacing = 0.13.em
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = PmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp
    )
)
