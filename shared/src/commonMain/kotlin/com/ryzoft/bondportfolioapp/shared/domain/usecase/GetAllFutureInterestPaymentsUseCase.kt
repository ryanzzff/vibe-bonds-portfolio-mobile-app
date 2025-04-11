package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.util.InterestCalculator

/**
 * Use case for retrieving all future interest payments for a specific bond.
 */
class GetAllFutureInterestPaymentsUseCase {

    /**
     * Executes the use case.
     * @param bond The bond to calculate interest payments for.
     * @return A list of future interest payments for the bond.
     */
    operator fun invoke(bond: Bond): List<InterestPayment> {
        return InterestCalculator.calculateFuturePayments(bond)
    }
} 