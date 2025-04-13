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
import com.ryzoft.bondportfolioapp.shared.domain.usecase.CalculateAverageYieldUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.DeleteBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetAllFutureInterestPaymentsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetMonthlyInterestSummaryUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetNextInterestPaymentUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetPortfolioInterestScheduleUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetYearlyInterestSummaryUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.UpdateBondUseCase

/**
 * Simple dependency provider for use cases
 */
object UseCaseProvider {

    private var bondRepository: BondRepository? = null
    private var getPortfolioInterestScheduleUseCase: GetPortfolioInterestScheduleUseCase? = null
    private var calculateAverageYieldUseCase: CalculateAverageYieldUseCase? = null
    
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
    
    /**
     * Get an instance of DeleteBondUseCase
     */
    fun provideDeleteBondUseCase(context: Context): DeleteBondUseCase {
        return DeleteBondUseCase(provideBondRepository(context))
    }
    
    /**
     * Get an instance of GetAllFutureInterestPaymentsUseCase
     */
    fun provideGetAllFutureInterestPaymentsUseCase(): GetAllFutureInterestPaymentsUseCase {
        return GetAllFutureInterestPaymentsUseCase()
    }

    /**
     * Get an instance of GetNextInterestPaymentUseCase
     */
    fun provideGetNextInterestPaymentUseCase(): GetNextInterestPaymentUseCase {
        return GetNextInterestPaymentUseCase()
    }

    /**
     * Get or create the GetPortfolioInterestScheduleUseCase instance
     */
    fun provideGetPortfolioInterestScheduleUseCase(context: Context): GetPortfolioInterestScheduleUseCase {
        return getPortfolioInterestScheduleUseCase ?: synchronized(this) {
            getPortfolioInterestScheduleUseCase ?: GetPortfolioInterestScheduleUseCase(provideBondRepository(context))
                .also { getPortfolioInterestScheduleUseCase = it }
        }
    }

    /**
     * Get an instance of GetMonthlyInterestSummaryUseCase
     */
    fun provideGetMonthlyInterestSummaryUseCase(context: Context): GetMonthlyInterestSummaryUseCase {
        return GetMonthlyInterestSummaryUseCase(provideGetPortfolioInterestScheduleUseCase(context))
    }

    /**
     * Get an instance of GetYearlyInterestSummaryUseCase
     */
    fun provideGetYearlyInterestSummaryUseCase(context: Context): GetYearlyInterestSummaryUseCase {
        return GetYearlyInterestSummaryUseCase(provideGetPortfolioInterestScheduleUseCase(context))
    }
    
    /**
     * Get or create the CalculateAverageYieldUseCase instance
     */
    fun provideCalculateAverageYieldUseCase(context: Context): CalculateAverageYieldUseCase {
        return calculateAverageYieldUseCase ?: synchronized(this) {
            calculateAverageYieldUseCase ?: CalculateAverageYieldUseCase(provideBondRepository(context))
                .also { calculateAverageYieldUseCase = it }
        }
    }
}
