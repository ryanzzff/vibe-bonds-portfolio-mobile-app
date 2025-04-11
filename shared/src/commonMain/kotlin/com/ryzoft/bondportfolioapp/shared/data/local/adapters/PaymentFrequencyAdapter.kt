package com.ryzoft.bondportfolioapp.shared.data.local.adapters

import app.cash.sqldelight.ColumnAdapter
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency

/**
 * SQLDelight ColumnAdapter to store PaymentFrequency enum as its String name.
 */
object PaymentFrequencyAdapter : ColumnAdapter<PaymentFrequency, String> {
    override fun decode(databaseValue: String): PaymentFrequency {
        // Consider adding error handling for unexpected values
        // Defaults to SEMI_ANNUAL if value is unrecognized for safety, could also throw exception
        return try {
            PaymentFrequency.valueOf(databaseValue)
        } catch (e: IllegalArgumentException) {
            // Log this error in a real app
            println("Warning: Unrecognized PaymentFrequency '$databaseValue' found in database.")
            PaymentFrequency.SEMI_ANNUAL // Or handle appropriately
        }
    }

    override fun encode(value: PaymentFrequency): String {
        return value.name
    }
}
