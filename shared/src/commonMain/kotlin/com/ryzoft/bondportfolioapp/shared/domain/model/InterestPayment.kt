package com.ryzoft.bondportfolioapp.shared.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents a calculated interest payment for a specific bond.
 */
data class InterestPayment(
    val bondId: Long, // Foreign key linking back to the Bond
    val paymentDate: LocalDate,
    val amount: Double // Calculated interest amount for this payment date
)
