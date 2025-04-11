package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Interface for the Portfolio List screen ViewModel
 */
interface PortfolioListViewModel {
    val uiState: StateFlow<PortfolioListUiState>
    fun loadBonds()
}

/**
 * Implementation of the PortfolioListViewModel
 */
class PortfolioListViewModelImpl(
    private val getBondsUseCase: GetBondsUseCase
) : ViewModel(), PortfolioListViewModel {

    private val _uiState = MutableStateFlow(PortfolioListUiState())
    override val uiState: StateFlow<PortfolioListUiState> = _uiState.asStateFlow()

    init {
        loadBonds()
    }

    /**
     * Load all bonds in the portfolio
     */
    override fun loadBonds() {
        viewModelScope.launch {
            getBondsUseCase()
                .onStart { 
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred"
                        )
                    }
                }
                .collect { bonds ->
                    _uiState.update { 
                        it.copy(
                            bonds = bonds,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}

/**
 * UI state for the Portfolio List screen
 */
data class PortfolioListUiState(
    val bonds: List<Bond> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
