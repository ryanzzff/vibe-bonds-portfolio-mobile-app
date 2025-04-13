package com.ryzoft.bondportfolioapp.android.presentation.screens.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.PortfolioValue
import com.ryzoft.bondportfolioapp.shared.domain.model.TimeRange
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetPortfolioValueHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * ViewModel interface for the Portfolio Value Chart screen
 */
interface PortfolioValueChartViewModel {
    val uiState: StateFlow<PortfolioValueChartUiState>
    fun setTimeRange(timeRange: TimeRange)
    fun refresh()
}

/**
 * Implementation of the Portfolio Value Chart ViewModel
 */
class PortfolioValueChartViewModelImpl(
    private val getPortfolioValueHistoryUseCase: GetPortfolioValueHistoryUseCase
) : ViewModel(), PortfolioValueChartViewModel {

    private val _uiState = MutableStateFlow(PortfolioValueChartUiState())
    override val uiState: StateFlow<PortfolioValueChartUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load portfolio value history data
     */
    private fun loadData() {
        getPortfolioValueHistoryUseCase(uiState.value.selectedTimeRange)
            .onStart { 
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                ) 
            }
            .onEach { portfolioValues ->
                _uiState.value = _uiState.value.copy(
                    portfolioValues = portfolioValues,
                    isLoading = false
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading portfolio value history",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Set the selected time range
     */
    override fun setTimeRange(timeRange: TimeRange) {
        if (timeRange != _uiState.value.selectedTimeRange) {
            _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
            loadData()
        }
    }

    /**
     * Refresh data
     */
    override fun refresh() {
        loadData()
    }
}

/**
 * UI state for the Portfolio Value Chart screen
 */
data class PortfolioValueChartUiState(
    val portfolioValues: List<PortfolioValue> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.ONE_YEAR,
    val isLoading: Boolean = false,
    val error: String? = null
)