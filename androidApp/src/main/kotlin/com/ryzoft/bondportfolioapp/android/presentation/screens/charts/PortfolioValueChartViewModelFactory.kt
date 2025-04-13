package com.ryzoft.bondportfolioapp.android.presentation.screens.charts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryzoft.bondportfolioapp.android.di.UseCaseProvider

/**
 * Factory for creating an instance of PortfolioValueChartViewModel
 */
class PortfolioValueChartViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioValueChartViewModelImpl::class.java)) {
            val getPortfolioValueHistoryUseCase = UseCaseProvider.provideGetPortfolioValueHistoryUseCase(context)
            
            return PortfolioValueChartViewModelImpl(
                getPortfolioValueHistoryUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}