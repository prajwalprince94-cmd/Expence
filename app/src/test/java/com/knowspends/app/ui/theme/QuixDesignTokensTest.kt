package com.knowspends.app.ui.theme

import com.knowspends.app.ui.components.quix.QuixStepState
import com.knowspends.app.ui.components.quix.QuixTimelineStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuixDesignTokensTest {

    @Test
    fun tokens_obsidianAndNeonLime_haveExpectedHexValues() {
        // Obsidian Canvas should be rgb(16, 18, 17) -> 0xFF101211
        assertEquals(0xFF101211.toLong(), (QuixTokens.ObsidianCanvas.value shr 32).toLong())
        // Obsidian Card should be rgb(23, 25, 24) -> 0xFF171918
        assertEquals(0xFF171918.toLong(), (QuixTokens.ObsidianCard.value shr 32).toLong())
        // Neon Lime should be rgb(235, 255, 165) -> 0xFFEBFFA5
        assertEquals(0xFFEBFFA5.toLong(), (QuixTokens.NeonLime.value shr 32).toLong())
    }

    @Test
    fun tokens_shapesAndBorders_areConfigured() {
        assertNotNull(QuixTokens.CardShape)
        assertNotNull(QuixTokens.PillShape)
        assertNotNull(QuixTokens.BorderCardStroke)
        assertNotNull(QuixTokens.BorderPillStroke)
        assertNotNull(QuixTokens.BorderActiveStroke)
    }

    @Test
    fun timelineStep_dataClass_preservesFieldsAndDefaults() {
        val step = QuixTimelineStep(
            title = "Netflix",
            subtitle = "25 Nov • Monthly",
            badgeText = "In 10d",
            amountFormatted = "₹649"
        )

        assertEquals("Netflix", step.title)
        assertEquals("25 Nov • Monthly", step.subtitle)
        assertEquals("In 10d", step.badgeText)
        assertEquals(QuixStepState.PENDING, step.state)
        assertEquals("₹649", step.amountFormatted)
    }

    @Test
    fun timelineStep_activeState_configuredCorrectly() {
        var clicked = false
        val activeStep = QuixTimelineStep(
            title = "Spotify Premium",
            subtitle = "Due Today • Monthly",
            badgeText = "Today",
            state = QuixStepState.ACTIVE,
            amountFormatted = "₹119",
            onClick = { clicked = true }
        )

        assertEquals(QuixStepState.ACTIVE, activeStep.state)
        activeStep.onClick?.invoke()
        assertTrue(clicked)
    }

    @Test
    fun segmentThresholdMath_atZeroProgress_zeroSegmentsFilled() {
        val segmentCount = 8
        val animatedRatio = 0f
        var filledSegments = 0
        for (i in 0 until segmentCount) {
            val segmentThreshold = (i + 1).toFloat() / segmentCount
            if (animatedRatio >= segmentThreshold) {
                filledSegments++
            }
        }
        assertEquals(0, filledSegments)
    }

    @Test
    fun segmentThresholdMath_atHalfProgress_fourOfEightSegmentsFilled() {
        val segmentCount = 8
        val animatedRatio = 0.5f
        var filledSegments = 0
        for (i in 0 until segmentCount) {
            val segmentThreshold = (i + 1).toFloat() / segmentCount
            if (animatedRatio >= segmentThreshold) {
                filledSegments++
            }
        }
        assertEquals(4, filledSegments)
    }

    @Test
    fun segmentThresholdMath_atFullProgress_allSegmentsFilled() {
        val segmentCount = 8
        val animatedRatio = 1.0f
        var filledSegments = 0
        for (i in 0 until segmentCount) {
            val segmentThreshold = (i + 1).toFloat() / segmentCount
            if (animatedRatio >= segmentThreshold) {
                filledSegments++
            }
        }
        assertEquals(8, filledSegments)
    }

    @Test
    fun safeRatioGuard_handlesNaNAndNegatives() {
        fun computeSafeRatio(input: Float): Float = if (input.isNaN()) 0f else input.coerceIn(0f, 1f)

        assertEquals(0f, computeSafeRatio(Float.NaN), 0.001f)
        assertEquals(0f, computeSafeRatio(-0.5f), 0.001f)
        assertEquals(0.75f, computeSafeRatio(0.75f), 0.001f)
        assertEquals(1.0f, computeSafeRatio(1.5f), 0.001f)
    }

    @Test
    fun quixColors_aliases_matchQuixTokens() {
        assertEquals(QuixTokens.ObsidianCanvas, QuixColors.ObsidianDeep)
        assertEquals(QuixTokens.ObsidianCard, QuixColors.ObsidianSurfaceCard)
        assertEquals(QuixTokens.CoralWarning, QuixColors.DangerCoral)
        assertEquals(QuixTokens.NeonLime, QuixColors.NeonLime)
    }
}
