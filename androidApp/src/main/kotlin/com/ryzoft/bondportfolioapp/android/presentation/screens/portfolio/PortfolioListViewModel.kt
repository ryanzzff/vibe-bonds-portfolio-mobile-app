package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.YieldType
import com.ryzoft.bondportfolioapp.shared.domain.usecase.CalculateAverageYieldUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Interface for the Portfolio List screen ViewModel
 */
interface PortfolioListViewModel {
    val uiState: StateFlow<PortfolioListUiState>
    fun loadBonds()
    fun setSelectedYieldType(yieldType: YieldType)
}

/**
 * Implementation of the PortfolioListViewModel
 */
class PortfolioListViewModelImpl(
    private val getBondsUseCase: GetBondsUseCase,
    private val calculateAverageYieldUseCase: CalculateAverageYieldUseCase
) : ViewModel(), PortfolioListViewModel {

    private val _uiState = MutableStateFlow(PortfolioListUiState())
    override val uiState: StateFlow<PortfolioListUiState> = _uiState.asStateFlow()
    
    private val _selectedYieldType = MutableStateFlow(YieldType.COUPON_RATE)

    init {
        loadBonds()
        loadYields()
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
    
    /**
     * Load yield information for the portfolio
     */
    private fun loadYields() {
        viewModelScope.launch {
            combine(
                calculateAverageYieldUseCase.calculateAllYields(),
                _selectedYieldType
            ) { yields, selectedType ->
                Pair(yields, selectedType)
            }.collect { (yields, selectedType) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        yields = yields,
                        selectedYieldType = selectedType
                    )
                }
            }
        }
    }
    
    /**
     * Set the selected yield type for display
     */
    override fun setSelectedYieldType(yieldType: YieldType) {
        _selectedYieldType.value = yieldType
    }
}

/**
 * UI state for the Portfolio List screen
 */
data class PortfolioListUiState(
    val bonds: List<Bond> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val yields: Map<YieldType, Double> = emptyMap(),
    val selectedYieldType: YieldType = YieldType.COUPON_RATE
)
