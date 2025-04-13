package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.PortfolioValue
import com.ryzoft.bondportfolioapp.shared.domain.model.TimeRange
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to get historical portfolio value for charting.
 * For MVP, this simulates historical data based on current bonds.
 * Future versions could use actual historical data from external API or local storage.
 */
class GetPortfolioValueHistoryUseCase(private val repository: BondRepository) {

    /**
     * Get portfolio value history for the specified time range.
     * @param timeRange The time range for which to get the history.
     * @return Flow of list of portfolio value data points.
     */
    operator fun invoke(timeRange: TimeRange): Flow<List<PortfolioValue>> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Start date based on selected time range
        val startDate = when (timeRange) {
            TimeRange.ONE_MONTH -> today.minus(1, DateTimeUnit.MONTH)
            TimeRange.SIX_MONTHS -> today.minus(6, DateTimeUnit.MONTH)
            TimeRange.ONE_YEAR -> today.minus(1, DateTimeUnit.YEAR)
            TimeRange.ALL_TIME -> today.minus(3, DateTimeUnit.YEAR) // For demo, use 3 years
        }
        
        return repository.getAllBonds().map { bonds ->
            generatePortfolioValueHistory(bonds, startDate, today)
        }
    }
    
    /**
     * Generate portfolio value history data points.
     * This simulates historical data as if the current portfolio was held during the entire period.
     * 
     * In a real app, this would use actual historical data or price changes.
     */
    private fun generatePortfolioValueHistory(
        bonds: List<Bond>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PortfolioValue> {
        val result = mutableListOf<PortfolioValue>()
        
        if (bonds.isEmpty()) {
            return emptyList()
        }
        
        // Calculate the current total portfolio value
        val currentPortfolioValue = bonds.sumOf { bond ->
            (bond.purchasePrice / 100) * bond.faceValuePerBond * bond.quantityPurchased
        }
        
        // For the purposes of the MVP, we'll create simulated data based on current value
        // Actual price changes would be implemented in a future version
        
        // Calculate how many data points we need 
        val numberOfMonths = calculateMonthsBetween(startDate, endDate)
        val dataPointsNeeded = minOf(numberOfMonths + 1, 24) // Cap at 24 data points for visualization
        
        // Generate the data points with some randomness to simulate market changes
        for (i in 0 until dataPointsNeeded) {
            val pointDate = if (i == dataPointsNeeded - 1) {
                endDate // Ensure the last point is always today
            } else {
                // Calculate the months to add
                val monthsToAdd = ((numberOfMonths.toDouble() * i) / (dataPointsNeeded - 1)).toLong()
                // Use the custom function that correctly handles month addition
                addMonths(startDate, monthsToAdd)
            }
            
            // Create some randomness in the historical values (±5%)
            // Current value is always the actual value without randomization
            val pointValue = if (pointDate == endDate) {
                currentPortfolioValue
            } else {
                // Add slight randomness to simulate market changes
                val randomFactor = 0.95 + (Math.random() * 0.10) // between 0.95 and 1.05
                currentPortfolioValue * randomFactor
            }
            
            result.add(PortfolioValue(pointDate, pointValue))
        }
        
        return result.sortedBy { it.date }
    }
    
    private fun calculateMonthsBetween(start: LocalDate, end: LocalDate): Int {
        val yearDiff = end.year - start.year
        val monthDiff = end.monthNumber - start.monthNumber
        return (yearDiff * 12) + monthDiff
    }
    
    /**
     * Adds the specified number of months to a LocalDate
     */
    private fun addMonths(date: LocalDate, months: Long): LocalDate {
        val year = date.year + (date.monthNumber + months.toInt() - 1) / 12
        val month = ((date.monthNumber + months.toInt() - 1) % 12) + 1
        val day = minOf(date.dayOfMonth, getLastDayOfMonth(year, month))
        return LocalDate(year, month, day)
    }
    
    private fun getLastDayOfMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }
    
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}