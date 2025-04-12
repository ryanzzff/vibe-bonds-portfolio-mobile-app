package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryzoft.bondportfolioapp.android.di.UseCaseProvider

/**
 * Factory for creating an instance of InterestScheduleViewModel
 */
class InterestScheduleViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InterestScheduleViewModelImpl::class.java)) {
            val getPortfolioInterestScheduleUseCase = UseCaseProvider.provideGetPortfolioInterestScheduleUseCase(context)
            val getMonthlyInterestSummaryUseCase = UseCaseProvider.provideGetMonthlyInterestSummaryUseCase(context)
            val getYearlyInterestSummaryUseCase = UseCaseProvider.provideGetYearlyInterestSummaryUseCase(context)
            
            return InterestScheduleViewModelImpl(
                getPortfolioInterestScheduleUseCase,
                getMonthlyInterestSummaryUseCase,
                getYearlyInterestSummaryUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}