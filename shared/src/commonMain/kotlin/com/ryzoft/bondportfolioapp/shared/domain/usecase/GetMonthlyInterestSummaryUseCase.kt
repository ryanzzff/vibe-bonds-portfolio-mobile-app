package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Month

// Define a typealias for YearMonth for clarity
typealias YearMonth = Pair<Int, Month>

/**
 * Use case for retrieving a summary of total interest income per month for the entire portfolio.
 */
class GetMonthlyInterestSummaryUseCase(private val scheduleUseCase: GetPortfolioInterestScheduleUseCase) {

    /**
     * Executes the use case.
     * @return A Flow emitting a Map where keys are YearMonth (Pair<Int, Month>)
     *         and values are the total interest amount for that month, sorted by YearMonth.
     */
    operator fun invoke(): Flow<Map<YearMonth, Double>> {
        return scheduleUseCase()
            .map { allPayments ->
                // Group payments by year and month
                allPayments
                    .groupBy { YearMonth(it.paymentDate.year, it.paymentDate.month) }
                    // Sum the amounts for each group (month)
                    .mapValues { (_, monthlyPayments) ->
                        monthlyPayments.sumOf { it.amount }
                    }
                    // Sort the resulting map by year and then month
                    .toSortedMap(compareBy({ it.first }, { it.second.ordinal }))
            }
    }
} 