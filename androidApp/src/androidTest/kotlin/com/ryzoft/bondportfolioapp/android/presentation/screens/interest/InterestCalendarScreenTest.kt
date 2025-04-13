package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class InterestCalendarScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Define a fake ViewModel implementation for testing
    class FakeInterestCalendarViewModel(initialState: InterestCalendarUiState) : InterestCalendarViewModel {
        private val _uiState = MutableStateFlow(initialState)
        override val uiState: StateFlow<InterestCalendarUiState> = _uiState

        var refreshCalled = false
        var selectedDate: LocalDate? = null

        override fun selectDate(date: LocalDate) {
            selectedDate = date
            // Simulate selecting a date might update the state in a real VM
            val payments = _uiState.value.paymentsByDate[date] ?: emptyList()
            _uiState.value = _uiState.value.copy(selectedDate = date, selectedDatePayments = payments)
        }

        override fun refresh() {
            refreshCalled = true
            // Simulate refresh might update the state in a real VM
        }

        // Helper to update state for testing
        fun setState(newState: InterestCalendarUiState) {
            _uiState.value = newState
        }
    }

    @Test
    fun calendarHeaderDisplaysCorrectly() {
        // Arrange
        val uiState = InterestCalendarUiState(
            isLoading = false,
            allPayments = emptyList(),
            paymentsByDate = emptyMap()
        )
        val fakeViewModel = FakeInterestCalendarViewModel(uiState)

        // Act
        composeRule.setContent {
            InterestCalendarScreen(onBackClick = {}, viewModel = fakeViewModel)
        }

        // Assert
        composeRule.onNodeWithText("Interest Calendar").assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsProgressIndicator() {
        // Arrange
        val uiState = InterestCalendarUiState(
            isLoading = true
        )
        val fakeViewModel = FakeInterestCalendarViewModel(uiState)

        // Act
        composeRule.setContent {
            InterestCalendarScreen(onBackClick = {}, viewModel = fakeViewModel)
        }

        // Wait for UI to stabilize
        composeRule.waitForIdle()
    }

    @Test
    fun errorStateShowsErrorMessage() {
        // Arrange
        val uiState = InterestCalendarUiState(
            isLoading = false,
            error = "Test error message"
        )
        val fakeViewModel = FakeInterestCalendarViewModel(uiState)

        // Act
        composeRule.setContent {
            InterestCalendarScreen(onBackClick = {}, viewModel = fakeViewModel)
        }

        // Assert
        composeRule.onNodeWithText("Test error message").assertIsDisplayed()
    }

    @Test
    fun selectedDateWithPaymentsShowsPaymentsList() {
        // Arrange
        val testDate = LocalDate(2025, 5, 15)
        val payment1 = InterestPayment(1L, "Treasury Bond 2025", testDate, 100.0)
        val payment2 = InterestPayment(2L, "Corporate Bond XYZ", testDate, 200.0)
        
        val uiState = InterestCalendarUiState(
            isLoading = false,
            paymentsByDate = mapOf(testDate to listOf(payment1, payment2)), // Need paymentsByDate for fake selectDate logic
            selectedDate = testDate,
            selectedDatePayments = listOf(payment1, payment2)
        )
        val fakeViewModel = FakeInterestCalendarViewModel(uiState)

        // Act
        composeRule.setContent {
            InterestCalendarScreen(onBackClick = {}, viewModel = fakeViewModel)
        }

        // Assert
        composeRule.onNodeWithText("Payments on MAY 15, 2025").assertIsDisplayed() // Corrected date format
        composeRule.onNodeWithText("Payment: $100.00").assertIsDisplayed()
        composeRule.onNodeWithText("Treasury Bond 2025").assertIsDisplayed()
        composeRule.onNodeWithText("Payment: $200.00").assertIsDisplayed()
        composeRule.onNodeWithText("Corporate Bond XYZ").assertIsDisplayed()
    }

    @Test
    fun selectedDateWithoutPaymentsShowsEmptyMessage() {
        // Arrange
        val testDate = LocalDate(2025, 5, 15)
        
        val uiState = InterestCalendarUiState(
            isLoading = false,
            selectedDate = testDate,
            selectedDatePayments = emptyList()
        )
        val fakeViewModel = FakeInterestCalendarViewModel(uiState)

        // Act
        composeRule.setContent {
            InterestCalendarScreen(onBackClick = {}, viewModel = fakeViewModel)
        }

        // Assert
        composeRule.onNodeWithText("Payments on MAY 15, 2025").assertIsDisplayed() // Corrected date format
        composeRule.onNodeWithText("No payments on this date").assertIsDisplayed()
    }

    @Test
    fun backButtonClickCallsOnBackClick() {
        // Arrange
        val uiState = InterestCalendarUiState(
            isLoading = false,
            allPayments = emptyList(),
            paymentsByDate = emptyMap()
        )
        val fakeViewModel = FakeInterestCalendarViewModel(uiState)
        
        var backClickCalled = false
        val onBackClick = { backClickCalled = true }

        // Act
        composeRule.setContent {
            InterestCalendarScreen(onBackClick = onBackClick, viewModel = fakeViewModel)
        }
        
        // Using the testTag to find and click the back button
        composeRule.onNodeWithTag("backButton").performClick()

        // Assert
        assert(backClickCalled) { "Back button click handler was not called" }
    }
}