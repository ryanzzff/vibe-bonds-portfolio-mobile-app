package com.ryzoft.bondportfolioapp.shared.data.local

import com.ryzoft.bondportfolioapp.db.BondPortfolioDB
import com.ryzoft.bondportfolioapp.db.Bonds
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.BondTypeAdapter
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.LocalDateAdapter
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.PaymentFrequencyAdapter

/**
 * Helper to create and configure the BondPortfolioDB instance.
 */
class DatabaseHelper(databaseDriverFactory: DatabaseDriverFactory) {

    private val driver = databaseDriverFactory.createDriver()

    // Create the Database instance, providing the driver and adapters
    val database = BondPortfolioDB(
        driver = driver,
        BondsAdapter = Bonds.Adapter(
            bondTypeAdapter = BondTypeAdapter,
            purchaseDateAdapter = LocalDateAdapter,
            maturityDateAdapter = LocalDateAdapter,
            paymentFrequencyAdapter = PaymentFrequencyAdapter
        )
    )

    // Optional: Could add a function here to close the driver if needed on certain platforms
    // fun closeDriver() { driver.close() }
}
