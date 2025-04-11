package com.ryzoft.bondportfolioapp.android.di

import android.content.Context
import com.ryzoft.bondportfolioapp.db.BondPortfolioDB
import com.ryzoft.bondportfolioapp.db.Bonds
import com.ryzoft.bondportfolioapp.shared.data.local.DatabaseDriverFactory
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.BondTypeAdapter
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.LocalDateAdapter
import com.ryzoft.bondportfolioapp.shared.data.local.adapters.PaymentFrequencyAdapter
import com.ryzoft.bondportfolioapp.shared.data.repository.BondRepositoryImpl
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import com.ryzoft.bondportfolioapp.shared.domain.usecase.AddBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.UpdateBondUseCase

/**
 * Simple dependency provider for use cases
 */
object UseCaseProvider {

    private var bondRepository: BondRepository? = null
    
    /**
     * Get or create the BondRepository instance
     */
    private fun provideBondRepository(context: Context): BondRepository {
        return bondRepository ?: synchronized(this) {
            bondRepository ?: createBondRepository(context).also { bondRepository = it }
        }
    }
    
    /**
     * Create a new BondRepository instance
     */
    private fun createBondRepository(context: Context): BondRepository {
        val driver = DatabaseDriverFactory(context).createDriver()
        val database = BondPortfolioDB(
            driver = driver,
            BondsAdapter = Bonds.Adapter(
                bondTypeAdapter = BondTypeAdapter,
                purchaseDateAdapter = LocalDateAdapter,
                maturityDateAdapter = LocalDateAdapter,
                paymentFrequencyAdapter = PaymentFrequencyAdapter
            )
        )
        return BondRepositoryImpl(database)
    }
    
    /**
     * Get an instance of GetBondsUseCase
     */
    fun provideGetBondsUseCase(context: Context): GetBondsUseCase {
        return GetBondsUseCase(provideBondRepository(context))
    }
    
    /**
     * Get an instance of AddBondUseCase
     */
    fun provideAddBondUseCase(context: Context): AddBondUseCase {
        return AddBondUseCase(provideBondRepository(context))
    }
    
    /**
     * Get an instance of UpdateBondUseCase
     */
    fun provideUpdateBondUseCase(context: Context): UpdateBondUseCase {
        return UpdateBondUseCase(provideBondRepository(context))
    }
    
    /**
     * Get an instance of GetBondDetailsUseCase
     */
    fun provideGetBondDetailsUseCase(context: Context): GetBondDetailsUseCase {
        return GetBondDetailsUseCase(provideBondRepository(context))
    }
}
