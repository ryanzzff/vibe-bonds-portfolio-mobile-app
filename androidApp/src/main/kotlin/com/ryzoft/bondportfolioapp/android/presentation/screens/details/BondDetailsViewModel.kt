package com.ryzoft.bondportfolioapp.android.presentation.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.usecase.DeleteBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel interface for the Bond Details screen
 */
interface BondDetailsViewModel {
    val uiState: StateFlow<BondDetailsUiState>
    
    /**
     * Load bond details by ID
     */
    fun loadBondDetails(bondId: Long)
    
    /**
     * Toggle the delete confirmation dialog
     */
    fun toggleDeleteConfirmDialog(show: Boolean)
    
    /**
     * Delete the current bond
     */
    fun deleteBond(onComplete: () -> Unit)
}

/**
 * Implementation of the BondDetailsViewModel
 */
class BondDetailsViewModelImpl(
    private val getBondDetailsUseCase: GetBondDetailsUseCase,
    private val deleteBondUseCase: DeleteBondUseCase
) : ViewModel(), BondDetailsViewModel {

    private val _uiState = MutableStateFlow(BondDetailsUiState())
    override val uiState: StateFlow<BondDetailsUiState> = _uiState.asStateFlow()

    /**
     * Load bond details by ID
     */
    override fun loadBondDetails(bondId: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val bond = getBondDetailsUseCase(bondId)
                if (bond != null) {
                    _uiState.update { 
                        it.copy(
                            bond = bond,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            bond = null,
                            isLoading = false,
                            error = "Bond not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    /**
     * Toggle the delete confirmation dialog
     */
    override fun toggleDeleteConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmDialog = show) }
    }

    /**
     * Delete the current bond
     */
    override fun deleteBond(onComplete: () -> Unit) {
        val bondId = uiState.value.bond?.id ?: return
        
        viewModelScope.launch {
            try {
                deleteBondUseCase(bondId)
                // Navigate back after successful deletion
                onComplete()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to delete bond")
                }
            }
        }
    }
}

/**
 * UI state for the Bond Details screen
 */
data class BondDetailsUiState(
    val bond: Bond? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteConfirmDialog: Boolean = false
)
