package com.ryzoft.bondportfolioapp.shared.domain.model

/**
 * Represents different types of bond yield calculations.
 */
enum class YieldType {
    COUPON_RATE,    // The stated coupon rate
    CURRENT_YIELD,  // Annual coupon payment ÷ Current price
    YIELD_TO_MATURITY // Yield to maturity (YTM)
} 