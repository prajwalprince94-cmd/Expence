package com.knowspends.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.knowspends.app.R

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, weight = FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, weight = FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, weight = FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, weight = FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, weight = FontWeight.ExtraBold),
    Font(R.font.plus_jakarta_sans, weight = FontWeight.Black)
)

val KiloFont = FontFamily(Font(R.font.kilo))
val OrbitronFont = FontFamily(Font(R.font.orbitron_variable))
val ShareTechMonoFont = FontFamily(Font(R.font.share_tech_mono))
val ChococookyFont = FontFamily(Font(R.font.chococooky))

enum class AppFont(val displayName: String, val id: String) {
    PLUS_JAKARTA_SANS("Plus Jakarta Sans", "plus_jakarta_sans"),
    KILO("Kilo", "kilo"),
    ORBITRON("Orbitron", "orbitron"),
    SHARE_TECH_MONO("Share Tech Mono", "share_tech_mono"),
    CHOCOCOOKY("Chococooky", "chococooky"),
    DEFAULT("System Default", "default"),
    SERIF("Serif", "serif"),
    MONOSPACE("Monospace", "monospace")
}

fun getFontFamily(fontId: String): FontFamily = when (fontId) {
    AppFont.KILO.id -> KiloFont
    AppFont.ORBITRON.id -> OrbitronFont
    AppFont.SHARE_TECH_MONO.id -> ShareTechMonoFont
    AppFont.CHOCOCOOKY.id -> ChococookyFont
    AppFont.SERIF.id -> FontFamily.Serif
    AppFont.MONOSPACE.id -> FontFamily.Monospace
    AppFont.DEFAULT.id -> FontFamily.Default
    else -> PlusJakartaSans
}

/**
 * Creates an Expressive typography scale faithfully adapted from Vyxel Apps.
 * Supports dynamic font family switching and display scaling.
 */
fun createAppTypography(
    fontFamily: FontFamily = PlusJakartaSans,
    scaleMultiplier: Float = 1.0f
): Typography {
    fun s(spVal: Float): androidx.compose.ui.unit.TextUnit = (spVal * scaleMultiplier).sp

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = s(54f),
            lineHeight = s(60f),
            letterSpacing = (-1.2).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = s(43f),
            lineHeight = s(50f),
            letterSpacing = (-0.9).sp,
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = s(34f),
            lineHeight = s(42f),
            letterSpacing = (-0.5).sp,
        ),

        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = s(30f),
            lineHeight = s(38f),
            letterSpacing = (-0.4).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = s(25f),
            lineHeight = s(32f),
            letterSpacing = (-0.3).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = s(21f),
            lineHeight = s(28f),
            letterSpacing = (-0.2).sp,
        ),

        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = s(20f),
            lineHeight = s(27f),
            letterSpacing = (-0.2).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(16f),
            lineHeight = s(23f),
            letterSpacing = 0.1.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(14f),
            lineHeight = s(20f),
            letterSpacing = 0.1.sp,
        ),

        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = s(16f),
            lineHeight = s(24f),
            letterSpacing = 0.15.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = s(14f),
            lineHeight = s(21f),
            letterSpacing = 0.2.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = s(12f),
            lineHeight = s(17f),
            letterSpacing = 0.3.sp,
        ),

        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(14f),
            lineHeight = s(20f),
            letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(12f),
            lineHeight = s(16f),
            letterSpacing = 0.4.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = s(11f),
            lineHeight = s(15f),
            letterSpacing = 0.5.sp,
        ),
    )
}

val Typography = createAppTypography()
