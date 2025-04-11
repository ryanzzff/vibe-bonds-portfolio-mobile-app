package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.util.InterestCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case for retrieving the next upcoming interest payment for a specific bond.
 */
class GetNextInterestPaymentUseCase {

    /**
     * Executes the use case.
     * @param bond The bond to find the next payment for.
     * @return The next InterestPayment, or null if there are no future payments.
     */
    operator fun invoke(bond: Bond): InterestPayment? {
        val futurePayments = InterestCalculator.calculateFuturePayments(bond)
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Find the first payment that is on or after today
        return futurePayments.firstOrNull { it.paymentDate >= today }
    }
} 