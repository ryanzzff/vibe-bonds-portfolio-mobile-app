package com.ryzoft.bondportfolioapp.android.presentation.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryzoft.bondportfolioapp.shared.domain.usecase.DeleteBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase

/**
 * Factory for creating BondDetailsViewModel with dependencies
 */
class BondDetailsViewModelFactory(
    private val getBondDetailsUseCase: GetBondDetailsUseCase,
    private val deleteBondUseCase: DeleteBondUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BondDetailsViewModelImpl::class.java)) {
            return BondDetailsViewModelImpl(
                getBondDetailsUseCase = getBondDetailsUseCase,
                deleteBondUseCase = deleteBondUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
