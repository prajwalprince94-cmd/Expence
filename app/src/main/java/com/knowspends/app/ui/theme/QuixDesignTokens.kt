package com.knowspends.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Quix Studio Design System Tokens
 * Source: https://www.quixstudio.net/free/components
 *
 * Dark obsidian palette with neon-lime high-contrast accents, 16dp rounded cards,
 * 999dp pill chips, and subtle translucent border strokes.
 */
object QuixTokens {
    // ── Surfaces & Canvas ────────────────────────────────────────────────────────
    val ObsidianCanvas = Color(0xFF101211)        // Page background: rgb(16, 18, 17)
    val ObsidianCard = Color(0xFF171918)          // Card surface: rgb(23, 25, 24)
    val ObsidianCardElevated = Color(0xFF1F2220)  // Elevated chip / input container
    val ObsidianGlass = Color(0x0FFFFFFF)         // Glassmorphic translucent surface: rgba(255, 255, 255, 0.06)

    // ── Accents & Glows ──────────────────────────────────────────────────────────
    val NeonLime = Color(0xFFEBFFA5)              // Quix signature neon lime: rgb(235, 255, 165)
    val NeonLimeGlow = Color(0x38EBFFA5)          // 22% neon lime glow
    val NeonLimeDim = Color(0x1FEBFFA5)           // 12% neon lime tint
    val ElectricCyan = Color(0xFF64C8FF)          // Secondary cyan accent
    val ElectricIndigo = Color(0xFF7C6CFF)        // Stepper / timeline indigo accent
    val CoralWarning = Color(0xFFFF4565)          // Warning / overspent indicator
    val AmberWarning = Color(0xFFFFB74D)          // 80% budget threshold alert

    // ── Typography Colors ────────────────────────────────────────────────────────
    val TextPrimary = Color(0xFFFAFCFB)           // Headline / title: rgb(250, 252, 251)
    val TextSecondary = Color(0xFFB4BDBA)         // Subtext: rgb(180, 189, 186)
    val TextMuted = Color(0x8CFFFFFF)             // 55% muted caption / label: rgba(255, 255, 255, 0.55)

    // ── Borders & Outlines ───────────────────────────────────────────────────────
    val BorderSubtle = Color(0x14FFFFFF)          // 8% white card border: rgba(255, 255, 255, 0.08)
    val BorderMedium = Color(0x1FFFFFFF)          // 12% white button/input border: rgba(255, 255, 255, 0.12)
    val BorderCardStroke = BorderStroke(1.dp, BorderSubtle)
    val BorderPillStroke = BorderStroke(1.dp, BorderMedium)
    val BorderActiveStroke = BorderStroke(1.dp, NeonLimeGlow)

    // ── Shapes ───────────────────────────────────────────────────────────────────
    val CardShape = RoundedCornerShape(16.dp)
    val CardShapeLarge = RoundedCornerShape(20.dp)
    val PillShape = CircleShape
    val TagShape = RoundedCornerShape(999.dp)
}
