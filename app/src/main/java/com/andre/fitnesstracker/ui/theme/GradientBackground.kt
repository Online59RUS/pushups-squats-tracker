package com.andre.fitnesstracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackground(content: @Composable BoxScope.() -> Unit) {
    val brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF004B8E),
            Color(0xFF010528)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush),
        content = content
    )
}
