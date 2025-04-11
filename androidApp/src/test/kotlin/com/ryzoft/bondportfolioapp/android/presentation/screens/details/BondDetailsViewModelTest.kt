package com.ryzoft.bondportfolioapp.android.presentation.screens.details

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.usecase.DeleteBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BondDetailsViewModelTest {

    private lateinit var getBondDetailsUseCase: GetBondDetailsUseCase
    private lateinit var deleteBondUseCase: DeleteBondUseCase
    private lateinit var viewModel: BondDetailsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getBondDetailsUseCase = mock()
        deleteBondUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBondDetails should update state with bond from use case`() = runTest {
        // Given
        val bondId = 1L
        val testBond = createTestBond(bondId)
        whenever(getBondDetailsUseCase(bondId)).thenReturn(testBond)

        // When
        viewModel = BondDetailsViewModel(getBondDetailsUseCase, deleteBondUseCase)
        viewModel.loadBondDetails(bondId)

        // Then
        assertEquals(testBond, viewModel.uiState.value.bond)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun `loadBondDetails should handle null bond`() = runTest {
        // Given
        val bondId = 1L
        whenever(getBondDetailsUseCase(bondId)).thenReturn(null)

        // When
        viewModel = BondDetailsViewModel(getBondDetailsUseCase, deleteBondUseCase)
        viewModel.loadBondDetails(bondId)

        // Then
        assertEquals(null, viewModel.uiState.value.bond)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Bond not found", viewModel.uiState.value.error)
    }

    @Test
    fun `toggleDeleteConfirmDialog should update showDeleteConfirmDialog state`() = runTest {
        // Given
        val bondId = 1L
        val testBond = createTestBond(bondId)
        whenever(getBondDetailsUseCase(bondId)).thenReturn(testBond)
        viewModel = BondDetailsViewModel(getBondDetailsUseCase, deleteBondUseCase)
        viewModel.loadBondDetails(bondId)

        // When - Show dialog
        viewModel.toggleDeleteConfirmDialog(true)

        // Then
        assertEquals(true, viewModel.uiState.value.showDeleteConfirmDialog)

        // When - Hide dialog
        viewModel.toggleDeleteConfirmDialog(false)

        // Then
        assertEquals(false, viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun `deleteBond should call DeleteBondUseCase and update state`() = runTest {
        // Given
        val bondId = 1L
        val testBond = createTestBond(bondId)
        whenever(getBondDetailsUseCase(bondId)).thenReturn(testBond)
        viewModel = BondDetailsViewModel(getBondDetailsUseCase, deleteBondUseCase)
        viewModel.loadBondDetails(bondId)

        // When
        viewModel.deleteBond { /* mock navigation callback */ }

        // Then
        verify(deleteBondUseCase).invoke(bondId)
    }

    private fun createTestBond(id: Long): Bond {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val later = now.plus(5, DateTimeUnit.YEAR)
        
        return Bond(
            id = id,
            name = "Test Corporate Bond",
            issuerName = "Test Corp",
            bondType = BondType.CORPORATE,
            faceValuePerBond = 1000.0,
            quantityPurchased = 5,
            purchasePrice = 980.0,
            purchaseDate = now,
            maturityDate = later,
            couponRate = 0.05,
            paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
            notes = "Test notes",
            isin = "US1234567890"
        )
    }
}
