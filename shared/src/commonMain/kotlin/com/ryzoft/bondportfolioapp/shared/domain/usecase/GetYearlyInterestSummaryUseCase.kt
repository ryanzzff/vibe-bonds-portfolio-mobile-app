package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving a summary of total interest income per year for the entire portfolio.
 */
class GetYearlyInterestSummaryUseCase(private val scheduleUseCase: GetPortfolioInterestScheduleUseCase) {

    /**
     * Executes the use case.
     * @return A Flow emitting a Map where keys are the year (Int)
     *         and values are the total interest amount for that year, sorted by year.
     */
    operator fun invoke(): Flow<Map<Int, Double>> {
        return scheduleUseCase()
            .map { allPayments ->
                // Group payments by year
                allPayments
                    .groupBy { it.paymentDate.year }
                    // Sum the amounts for each group (year)
                    .mapValues { (_, yearlyPayments) ->
                        yearlyPayments.sumOf { it.amount }
                    }
                    // Sort the resulting map by year
                    .toSortedMap()
            }
    }
} 