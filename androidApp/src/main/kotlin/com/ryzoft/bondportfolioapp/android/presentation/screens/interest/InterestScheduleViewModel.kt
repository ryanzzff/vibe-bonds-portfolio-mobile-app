package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetMonthlyInterestSummaryUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetPortfolioInterestScheduleUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetYearlyInterestSummaryUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * Tab options for the Interest Schedule screen
 */
enum class InterestScheduleTab {
    UPCOMING_PAYMENTS,
    MONTHLY_SUMMARY,
    YEARLY_SUMMARY
}

/**
 * ViewModel interface for the Interest Schedule Screen
 */
interface InterestScheduleViewModel {
    val uiState: StateFlow<InterestScheduleUiState>
    fun setActiveTab(tab: InterestScheduleTab)
    fun refresh()
}

/**
 * Implementation of the Interest Schedule ViewModel
 */
class InterestScheduleViewModelImpl(
    private val getPortfolioInterestScheduleUseCase: GetPortfolioInterestScheduleUseCase,
    private val getMonthlyInterestSummaryUseCase: GetMonthlyInterestSummaryUseCase,
    private val getYearlyInterestSummaryUseCase: GetYearlyInterestSummaryUseCase
) : ViewModel(), InterestScheduleViewModel {

    private val _uiState = MutableStateFlow(InterestScheduleUiState())
    override val uiState: StateFlow<InterestScheduleUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load all interest schedule data
     */
    private fun loadData() {
        // Load upcoming payments
        getPortfolioInterestScheduleUseCase()
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
            .onEach { payments ->
                _uiState.value = _uiState.value.copy(
                    upcomingPayments = payments,
                    isLoading = false
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading upcoming payments",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)

        // Load monthly summary
        getMonthlyInterestSummaryUseCase()
            .onEach { monthlySummary ->
                _uiState.value = _uiState.value.copy(
                    monthlySummary = monthlySummary
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading monthly summary"
                )
            }
            .launchIn(viewModelScope)

        // Load yearly summary
        getYearlyInterestSummaryUseCase()
            .onEach { yearlySummary ->
                _uiState.value = _uiState.value.copy(
                    yearlySummary = yearlySummary
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading yearly summary"
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Change the active tab
     */
    override fun setActiveTab(tab: InterestScheduleTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    /**
     * Refresh all data
     */
    override fun refresh() {
        loadData()
    }
}

/**
 * UI state for the Interest Schedule screen
 */
data class InterestScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeTab: InterestScheduleTab = InterestScheduleTab.UPCOMING_PAYMENTS,
    val upcomingPayments: List<InterestPayment> = emptyList(),
    val monthlySummary: Map<YearMonth, Double> = emptyMap(),
    val yearlySummary: Map<Int, Double> = emptyMap()
)