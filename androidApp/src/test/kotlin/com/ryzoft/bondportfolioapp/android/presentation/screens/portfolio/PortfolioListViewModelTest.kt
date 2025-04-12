package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.model.YieldType
import com.ryzoft.bondportfolioapp.shared.domain.usecase.CalculateAverageYieldUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioListViewModelTest {

    private lateinit var getBondsUseCase: GetBondsUseCase
    private lateinit var calculateAverageYieldUseCase: CalculateAverageYieldUseCase
    private lateinit var viewModel: PortfolioListViewModelImpl
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getBondsUseCase = mock()
        calculateAverageYieldUseCase = mock()
        
        // Setup default mock behavior for calculateAverageYieldUseCase
        val mockYields = mapOf(
            YieldType.COUPON_RATE to 4.0,
            YieldType.CURRENT_YIELD to 4.2,
            YieldType.YIELD_TO_MATURITY to 4.5
        )
        whenever(calculateAverageYieldUseCase.calculateAllYields()).thenReturn(flowOf(mockYields))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBonds should update state with bonds from use case`() = runTest {
        // Given
        val testBonds = createTestBonds()
        whenever(getBondsUseCase()).thenReturn(flowOf(testBonds))

        // When
        viewModel = PortfolioListViewModelImpl(getBondsUseCase, calculateAverageYieldUseCase)

        // Then
        assertEquals(testBonds, viewModel.uiState.value.bonds)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `loadBonds should handle empty list`() = runTest {
        // Given
        val emptyList = emptyList<Bond>()
        whenever(getBondsUseCase()).thenReturn(flowOf(emptyList))

        // When
        viewModel = PortfolioListViewModelImpl(getBondsUseCase, calculateAverageYieldUseCase)

        // Then
        assertEquals(emptyList, viewModel.uiState.value.bonds)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `loadBonds should handle exception`() = runTest {
        // Given
        val errorMessage = "Test error"
        whenever(getBondsUseCase()).thenReturn(flow<List<Bond>> { 
            throw RuntimeException(errorMessage)
        })

        // When
        viewModel = PortfolioListViewModelImpl(getBondsUseCase, calculateAverageYieldUseCase)

        // Then
        assertEquals(emptyList<Bond>(), viewModel.uiState.value.bonds)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.error)
    }
    
    @Test
    fun `setSelectedYieldType should update state with selected yield type`() = runTest {
        // Given
        whenever(getBondsUseCase()).thenReturn(flowOf(emptyList()))
        viewModel = PortfolioListViewModelImpl(getBondsUseCase, calculateAverageYieldUseCase)
        
        // When
        viewModel.setSelectedYieldType(YieldType.YIELD_TO_MATURITY)
        
        // Then
        assertEquals(YieldType.YIELD_TO_MATURITY, viewModel.uiState.value.selectedYieldType)
    }

    private fun createTestBonds(): List<Bond> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val later = now.plus(5, DateTimeUnit.YEAR)
        
        return listOf(
            Bond(
                id = 1,
                name = "Test Corporate Bond",
                issuerName = "Test Corp",
                bondType = BondType.CORPORATE,
                faceValuePerBond = 1000.0,
                quantityPurchased = 5,
                purchasePrice = 980.0,
                purchaseDate = now,
                maturityDate = later,
                couponRate = 5.0,
                paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
                notes = "Test notes",
                isin = "US1234567890"
            ),
            Bond(
                id = 2,
                name = "Test TREASURY Bond",
                issuerName = "Test TREASURY",
                bondType = BondType.TREASURY,
                faceValuePerBond = 1000.0,
                quantityPurchased = 10,
                purchasePrice = 1000.0,
                purchaseDate = now,
                maturityDate = later,
                couponRate = 3.0,
                paymentFrequency = PaymentFrequency.ANNUAL,
                notes = null,
                isin = "US9876543210"
            )
        )
    }
}
