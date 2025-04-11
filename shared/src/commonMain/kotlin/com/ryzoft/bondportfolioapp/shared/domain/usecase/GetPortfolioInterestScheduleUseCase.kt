package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import com.ryzoft.bondportfolioapp.shared.domain.util.InterestCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving a combined schedule of all future interest payments
 * for the entire bond portfolio.
 */
class GetPortfolioInterestScheduleUseCase(private val repository: BondRepository) {

    /**
     * Executes the use case.
     * @return A Flow emitting a list of all future interest payments from all bonds,
     *         sorted chronologically by payment date.
     */
    operator fun invoke(): Flow<List<InterestPayment>> {
        return repository.getAllBonds()
            .map { bonds ->
                // Calculate future payments for each bond
                val allPayments = bonds.flatMap {
                    InterestCalculator.calculateFuturePayments(it)
                }
                // Sort the combined list by date
                allPayments.sortedBy { it.paymentDate }
            }
    }
} 