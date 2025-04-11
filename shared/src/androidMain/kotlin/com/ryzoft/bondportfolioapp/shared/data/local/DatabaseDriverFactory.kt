package com.ryzoft.bondportfolioapp.shared.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ryzoft.bondportfolioapp.db.BondPortfolioDB
import kotlin.ExperimentalStdlibApi

/**
 * Actual implementation of the DatabaseDriverFactory for Android.
 */
@OptIn(ExperimentalStdlibApi::class)
actual class DatabaseDriverFactory actual constructor() {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = BondPortfolioDB.Schema,
            context = context,
            name = "BondPortfolio.db"
        )
    }
}
