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

val InterFont = FontFamily(Font(R.font.inter))
val OutfitFont = FontFamily(Font(R.font.outfit))
val KiloFont = FontFamily(Font(R.font.kilo))
val OrbitronFont = FontFamily(Font(R.font.orbitron_variable))
val ShareTechMonoFont = FontFamily(Font(R.font.share_tech_mono))
val ChococookyFont = FontFamily(Font(R.font.chococooky))

enum class AppFont(val displayName: String, val id: String) {
    PLUS_JAKARTA_SANS("Plus Jakarta Sans", "plus_jakarta_sans"),
    INTER("Inter", "inter"),
    OUTFIT("Outfit", "outfit"),
    KILO("Kilo", "kilo"),
    ORBITRON("Orbitron", "orbitron"),
    SHARE_TECH_MONO("Share Tech Mono", "share_tech_mono"),
    CHOCOCOOKY("Chococooky", "chococooky"),
    DEFAULT("System Default", "default"),
    SERIF("Serif", "serif"),
    MONOSPACE("Monospace", "monospace"),
    CURSIVE("Cursive", "cursive")
}

fun getFontFamily(fontId: String): FontFamily = when (fontId) {
    AppFont.INTER.id -> InterFont
    AppFont.OUTFIT.id -> OutfitFont
    AppFont.KILO.id -> KiloFont
    AppFont.ORBITRON.id -> OrbitronFont
    AppFont.SHARE_TECH_MONO.id -> ShareTechMonoFont
    AppFont.CHOCOCOOKY.id -> ChococookyFont
    AppFont.SERIF.id -> FontFamily.Serif
    AppFont.MONOSPACE.id -> FontFamily.Monospace
    AppFont.CURSIVE.id -> FontFamily.Cursive
    AppFont.DEFAULT.id -> FontFamily.Default
    else -> PlusJakartaSans
}

/**
 * Creates the official Material 3 Expressive typography scale with pixel-perfect
 * sp sizes and line heights optimized for mobile readability and One UI font scaling.
 */
fun createAppTypography(fontFamily: FontFamily = PlusJakartaSans): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp,
        ),

        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),

        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),

        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),

        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
    )
}

val Typography = createAppTypography()
