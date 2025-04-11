package com.ryzoft.bondportfolioapp.shared.data.local.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDate

/**
 * SQLDelight ColumnAdapter to store kotlinx.datetime.LocalDate as an ISO-8601 String (YYYY-MM-DD).
 */
object LocalDateAdapter : ColumnAdapter<LocalDate, String> {
    override fun decode(databaseValue: String): LocalDate {
        return LocalDate.parse(databaseValue)
    }

    override fun encode(value: LocalDate): String {
        return value.toString() // LocalDate.toString() produces ISO-8601 format
    }
}
