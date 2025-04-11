package com.ryzoft.bondportfolioapp.shared.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents a single bond holding in the user's portfolio.
 */
data class Bond(
    val id: Long, // Unique identifier from the database
    val bondType: BondType,
    val issuerName: String,
    val couponRate: Double, // Annual coupon rate as a percentage (e.g., 2.5 for 2.5%)
    val maturityDate: LocalDate,
    val faceValuePerBond: Double, // Face value of a single bond (e.g., 1000.0)
    val purchaseDate: LocalDate,
    val purchasePrice: Double, // Price paid per 100 face value (e.g., 99.5)
    val paymentFrequency: PaymentFrequency,
    val quantityPurchased: Int, // Number of bonds purchased
    val currency: String = "USD", // Defaulted to USD as per requirements

    // Optional fields
    val name: String? = null, // User-defined nickname
    val isinCusip: String? = null, // ISIN or CUSIP identifier
    val notes: String? = null
)
