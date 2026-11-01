package com.knowspends.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Named gradient presets for cover art, decorative banners, and expressive profile surfaces.
 */
object GradientPresets {
    val Ocean = Brush.linearGradient(listOf(Color(0xFF007799), Color(0xFF00A896), Color(0xFF028090)))
    val Sunset = Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFC107)))
    val Amethyst = Brush.linearGradient(listOf(Color(0xFF7B1FA2), Color(0xFF9C27B0), Color(0xFFBA68C8)))
    val Midnight = Brush.linearGradient(listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64)))
}
