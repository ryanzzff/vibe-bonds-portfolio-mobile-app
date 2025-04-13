package com.ryzoft.bondportfolioapp.shared.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents a historical portfolio value data point for charting purposes.
 */
data class PortfolioValue(
    val date: LocalDate,
    val totalValue: Double
)

/**
 * Defines the time range options for portfolio value chart.
 */
enum class TimeRange(val displayName: String) {
    ONE_MONTH("1 Month"),
    SIX_MONTHS("6 Months"),
    ONE_YEAR("1 Year"),
    ALL_TIME("All Time")
}