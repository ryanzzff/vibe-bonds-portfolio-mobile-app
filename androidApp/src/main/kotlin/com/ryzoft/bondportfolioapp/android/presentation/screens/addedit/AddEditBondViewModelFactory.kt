package com.ryzoft.bondportfolioapp.android.presentation.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryzoft.bondportfolioapp.shared.domain.usecase.AddBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.UpdateBondUseCase

/**
 * Factory for creating AddEditBondViewModel with dependencies
 */
class AddEditBondViewModelFactory(
    private val addBondUseCase: AddBondUseCase,
    private val updateBondUseCase: UpdateBondUseCase,
    private val getBondDetailsUseCase: GetBondDetailsUseCase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditBondViewModelImpl::class.java)) {
            return AddEditBondViewModelImpl(
                addBondUseCase,
                updateBondUseCase,
                getBondDetailsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
