package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryzoft.bondportfolioapp.android.di.UseCaseProvider

/**
 * Factory for creating an instance of InterestCalendarViewModel
 */
class InterestCalendarViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InterestCalendarViewModelImpl::class.java)) {
            val getPortfolioInterestScheduleUseCase = UseCaseProvider.provideGetPortfolioInterestScheduleUseCase(context)
            
            return InterestCalendarViewModelImpl(
                getPortfolioInterestScheduleUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}