package com.knowspends.app.domain.model

data class NarrativeInsight(
    val id: String,
    val title: String,
    val narrative: String,
    val iconKey: String = "auto_awesome", // "star", "trending_up", "warning", "calendar", "savings", "store", "pie"
    val type: InsightType = InsightType.NEUTRAL,
    val metricHighlight: String? = null
)

enum class InsightType {
    POSITIVE,
    CAUTION,
    NEUTRAL,
    HIGHLIGHT
}
