package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetPortfolioInterestScheduleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class InterestCalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPortfolioInterestScheduleUseCase: GetPortfolioInterestScheduleUseCase
    private lateinit var viewModel: InterestCalendarViewModelImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPortfolioInterestScheduleUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads interest payments and groups them by date`() = runTest {
        // Arrange
        val testDate1 = LocalDate(2025, 5, 15)
        val testDate2 = LocalDate(2025, 5, 15)
        val testDate3 = LocalDate(2025, 6, 1)

        val payments = listOf(
            InterestPayment(1L, "Treasury Bond A", testDate1, 100.0),
            InterestPayment(2L, "Corporate Bond B", testDate2, 200.0),
            InterestPayment(3L, "Municipal Bond C", testDate3, 300.0)
        )
        whenever(getPortfolioInterestScheduleUseCase()).thenReturn(flowOf(payments))

        // Act
        viewModel = InterestCalendarViewModelImpl(getPortfolioInterestScheduleUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(payments, uiState.allPayments)
        assertEquals(2, uiState.paymentsByDate.size)
        assertEquals(2, uiState.paymentsByDate[testDate1]?.size)
        assertEquals(1, uiState.paymentsByDate[testDate3]?.size)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `selectDate updates selectedDate and selectedDatePayments`() = runTest {
        // Arrange
        val testDate1 = LocalDate(2025, 5, 15)
        val testDate2 = LocalDate(2025, 6, 1)

        val payment1 = InterestPayment(1L, "Treasury Bond A", testDate1, 100.0)
        val payment2 = InterestPayment(2L, "Corporate Bond B", testDate1, 200.0)
        val payment3 = InterestPayment(3L, "Municipal Bond C", testDate2, 300.0)
        
        val payments = listOf(payment1, payment2, payment3)
        whenever(getPortfolioInterestScheduleUseCase()).thenReturn(flowOf(payments))
        
        viewModel = InterestCalendarViewModelImpl(getPortfolioInterestScheduleUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.selectDate(testDate1)
        
        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(testDate1, uiState.selectedDate)
        assertEquals(2, uiState.selectedDatePayments.size)
        assertTrue(uiState.selectedDatePayments.contains(payment1))
        assertTrue(uiState.selectedDatePayments.contains(payment2))
    }

    @Test
    fun `refresh reloads data`() = runTest {
        // Arrange
        val testDate = LocalDate(2025, 5, 15)
        val initialPayments = listOf(
            InterestPayment(1L, "Treasury Bond A", testDate, 100.0)
        )
        val updatedPayments = listOf(
            InterestPayment(1L, "Treasury Bond A", testDate, 100.0),
            InterestPayment(2L, "Corporate Bond B", testDate, 200.0)
        )
        
        whenever(getPortfolioInterestScheduleUseCase()).thenReturn(
            flowOf(initialPayments),
            flowOf(updatedPayments)
        )
        
        viewModel = InterestCalendarViewModelImpl(getPortfolioInterestScheduleUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.value.allPayments.size)
        
        // Act
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        assertEquals(2, viewModel.uiState.value.allPayments.size)
    }
}