package com.ryzoft.bondportfolioapp.shared.domain.model

/**
 * Represents how often a bond pays interest.
 */
enum class PaymentFrequency {
    SEMI_ANNUAL,
    ANNUAL,
    QUARTERLY,
    MONTHLY,
    ZERO_COUPON // For bonds that don't pay periodic interest
}
