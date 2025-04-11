package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondsUseCase

/**
 * Factory for creating PortfolioListViewModel with dependencies
 */
class PortfolioListViewModelFactory(
    private val getBondsUseCase: GetBondsUseCase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioListViewModelImpl::class.java)) {
            return PortfolioListViewModelImpl(getBondsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
