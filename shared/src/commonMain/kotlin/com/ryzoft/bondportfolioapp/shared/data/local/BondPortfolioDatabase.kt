package com.ryzoft.bondportfolioapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import com.ryzoft.bondportfolioapp.db.BondPortfolioDB
import com.ryzoft.bondportfolioapp.db.Bonds
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.BondTypeAdapter
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.LocalDateAdapter
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.PaymentFrequencyAdapter

class BondPortfolioDatabase(driver: SqlDriver) {
    private val database = BondPortfolioDB(
        driver = driver,
        BondsAdapter = Bonds.Adapter(
            bondTypeAdapter = BondTypeAdapter,
            purchaseDateAdapter = LocalDateAdapter,
            maturityDateAdapter = LocalDateAdapter,
            paymentFrequencyAdapter = PaymentFrequencyAdapter
        )
    )

    val bondQueries get() = database.bondQueries
}
