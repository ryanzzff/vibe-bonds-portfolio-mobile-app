package com.ryzoft.bondportfolioapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import kotlin.ExperimentalStdlibApi

/**
 * Expected factory for creating platform-specific SQLDelight drivers.
 * Platform-specific implementations may have additional constructor parameters.
 */
@OptIn(ExperimentalStdlibApi::class)
expect class DatabaseDriverFactory {
    /**
     * Creates the platform-specific SqlDriver.
     */
    fun createDriver(): SqlDriver
}
