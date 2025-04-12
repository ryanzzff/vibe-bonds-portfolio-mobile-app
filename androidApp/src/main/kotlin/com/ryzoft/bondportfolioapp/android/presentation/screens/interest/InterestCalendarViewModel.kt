package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetPortfolioInterestScheduleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel interface for the Interest Calendar Screen
 */
interface InterestCalendarViewModel {
    val uiState: StateFlow<InterestCalendarUiState>
    fun selectDate(date: LocalDate)
    fun refresh()
}

/**
 * Implementation of the Interest Calendar ViewModel
 */
class InterestCalendarViewModelImpl(
    private val getPortfolioInterestScheduleUseCase: GetPortfolioInterestScheduleUseCase
) : ViewModel(), InterestCalendarViewModel {

    private val _uiState = MutableStateFlow(InterestCalendarUiState())
    override val uiState: StateFlow<InterestCalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load interest schedule data
     */
    private fun loadData() {
        getPortfolioInterestScheduleUseCase()
            .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
            .onEach { payments ->
                val paymentsByDate = payments.groupBy { it.paymentDate }
                val selectedDatePayments = if (_uiState.value.selectedDate != null) {
                    paymentsByDate[_uiState.value.selectedDate] ?: emptyList()
                } else {
                    emptyList()
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allPayments = payments,
                    paymentsByDate = paymentsByDate,
                    selectedDatePayments = selectedDatePayments
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading interest payments",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Select a date on the calendar
     */
    override fun selectDate(date: LocalDate) {
        val selectedDatePayments = _uiState.value.paymentsByDate[date] ?: emptyList()
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedDatePayments = selectedDatePayments
        )
    }

    /**
     * Refresh calendar data
     */
    override fun refresh() {
        loadData()
    }
}

/**
 * UI state for the Interest Calendar screen
 */
data class InterestCalendarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allPayments: List<InterestPayment> = emptyList(),
    val paymentsByDate: Map<LocalDate, List<InterestPayment>> = emptyMap(),
    val selectedDate: LocalDate? = getCurrentDate(),
    val selectedDatePayments: List<InterestPayment> = emptyList()
)

/**
 * Helper function to get current date
 */
private fun getCurrentDate(): LocalDate {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.date
}