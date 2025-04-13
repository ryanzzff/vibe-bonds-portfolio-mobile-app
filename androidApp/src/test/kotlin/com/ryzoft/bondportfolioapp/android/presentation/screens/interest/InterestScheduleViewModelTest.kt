package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetMonthlyInterestSummaryUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetPortfolioInterestScheduleUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetYearlyInterestSummaryUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class InterestScheduleViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPortfolioInterestScheduleUseCase: GetPortfolioInterestScheduleUseCase
    private lateinit var getMonthlyInterestSummaryUseCase: GetMonthlyInterestSummaryUseCase
    private lateinit var getYearlyInterestSummaryUseCase: GetYearlyInterestSummaryUseCase
    private lateinit var viewModel: InterestScheduleViewModelImpl
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getPortfolioInterestScheduleUseCase = mock()
        getMonthlyInterestSummaryUseCase = mock()
        getYearlyInterestSummaryUseCase = mock()
        
        // Set up test data
        val upcomingPayments = listOf(
            InterestPayment(
                bondId = 1L,
                bondName = "Treasury Bond 2025",
                paymentDate = LocalDate(2025, 5, 15),
                amount = 100.0
            ),
            InterestPayment(
                bondId = 2L,
                bondName = "Corporate Bond XYZ",
                paymentDate = LocalDate(2025, 6, 20),
                amount = 50.0
            )
        )
        
        val monthlySummary = mapOf(
            YearMonth(2025, Month.MAY) to 100.0,
            YearMonth(2025, Month.JUNE) to 50.0
        )
        
        val yearlySummary = mapOf(
            2025 to 150.0,
            2026 to 300.0
        )
        
        // Mock the use case responses
        whenever(getPortfolioInterestScheduleUseCase()).thenReturn(flowOf(upcomingPayments))
        whenever(getMonthlyInterestSummaryUseCase()).thenReturn(flowOf(monthlySummary))
        whenever(getYearlyInterestSummaryUseCase()).thenReturn(flowOf(yearlySummary))
        
        // Create the view model
        viewModel = InterestScheduleViewModelImpl(
            getPortfolioInterestScheduleUseCase,
            getMonthlyInterestSummaryUseCase,
            getYearlyInterestSummaryUseCase
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should load data correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        
        // Verify initial state
        assertEquals(InterestScheduleTab.UPCOMING_PAYMENTS, state.activeTab)
        assertEquals(2, state.upcomingPayments.size)
        assertEquals(2, state.monthlySummary.size)
        assertEquals(2, state.yearlySummary.size)
        assertEquals(100.0, state.monthlySummary[YearMonth(2025, Month.MAY)])
        assertEquals(50.0, state.monthlySummary[YearMonth(2025, Month.JUNE)])
        assertEquals(150.0, state.yearlySummary[2025])
        assertEquals(300.0, state.yearlySummary[2026])
        assertNull(state.error)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `setActiveTab should update state correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When changing tab to Monthly Summary
        viewModel.setActiveTab(InterestScheduleTab.MONTHLY_SUMMARY)
        
        // Then state should update
        val state = viewModel.uiState.value
        assertEquals(InterestScheduleTab.MONTHLY_SUMMARY, state.activeTab)
        
        // When changing tab to Yearly Summary
        viewModel.setActiveTab(InterestScheduleTab.YEARLY_SUMMARY)
        
        // Then state should update
        val updatedState = viewModel.uiState.value
        assertEquals(InterestScheduleTab.YEARLY_SUMMARY, updatedState.activeTab)
    }
}