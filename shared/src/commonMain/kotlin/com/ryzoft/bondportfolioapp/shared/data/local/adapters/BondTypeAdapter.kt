package com.ryzoft.bondportfolioapp.shared.data.local.adapters

import app.cash.sqldelight.ColumnAdapter
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType

/**
 * SQLDelight ColumnAdapter to store BondType enum as its String name.
 */
object BondTypeAdapter : ColumnAdapter<BondType, String> {
    override fun decode(databaseValue: String): BondType {
        // Consider adding error handling for unexpected values
        // Defaults to COMPANY if value is unrecognized for safety, could also throw exception
        return try {
            BondType.valueOf(databaseValue)
        } catch (e: IllegalArgumentException) {
            // Log this error in a real app
            println("Warning: Unrecognized BondType '$databaseValue' found in database.")
            BondType.CORPORATE // Or handle appropriately
        }
    }

    override fun encode(value: BondType): String {
        return value.name
    }
}
