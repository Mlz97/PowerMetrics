package com.example.feperfectsquat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The PowerMetrics design is dark-only ("gym-floor friendly")
private val PmColorScheme = darkColorScheme(
    primary = PmAccent,
    onPrimary = Color.White,
    secondary = PmAccentLight,
    onSecondary = Color.White,
    background = PmCanvas,
    onBackground = PmText,
    surface = PmSurface,
    onSurface = PmText,
    surfaceVariant = PmBackground,
    onSurfaceVariant = PmTextSecondary,
    outline = PmLineStrong,
    error = PmError,
)

@Composable
fun FEPerfectSquatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PmColorScheme,
        typography = Typography,
        content = content
    )
}
