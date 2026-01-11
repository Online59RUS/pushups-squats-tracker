package com.andre.fitnesstracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2D8CFF),
    onPrimary = Color(0xFFFFFFFF),

    background = Color(0xFF010528),
    onBackground = Color(0xFFEAF2FF),

    surface = Color(0xFF0B1A3A),
    onSurface = Color(0xFFEAF2FF),
    surfaceVariant = Color(0xFF132A57),
    onSurfaceVariant = Color(0xFFBBD3FF),

    outline = Color(0xFF2A4C86)
)

@Composable
fun FitnessTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Мы делаем дизайн под тёмную тему с градиентом — поэтому всегда используем DarkColors.
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
