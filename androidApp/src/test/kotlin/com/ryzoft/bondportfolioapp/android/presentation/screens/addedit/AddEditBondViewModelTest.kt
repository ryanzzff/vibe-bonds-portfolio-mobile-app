package com.ryzoft.bondportfolioapp.android.presentation.screens.addedit

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.usecase.AddBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.UpdateBondUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddEditBondViewModelTest {

    private lateinit var addBondUseCase: AddBondUseCase
    private lateinit var updateBondUseCase: UpdateBondUseCase
    private lateinit var getBondDetailsUseCase: GetBondDetailsUseCase
    private lateinit var viewModel: AddEditBondViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        addBondUseCase = mock()
        updateBondUseCase = mock()
        getBondDetailsUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init with null bondId should set up add mode`() = runTest {
        // When
        viewModel = AddEditBondViewModel(addBondUseCase, updateBondUseCase, getBondDetailsUseCase)
        viewModel.initialize(null)

        // Then
        assertEquals("Add Bond", viewModel.uiState.value.screenTitle)
        assertFalse(viewModel.uiState.value.isEditMode)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `init with bondId should set up edit mode and load bond details`() = runTest {
        // Given
        val bondId = 1L
        val testBond = createTestBond(bondId)
        whenever(getBondDetailsUseCase(bondId)).thenReturn(testBond)

        // When
        viewModel = AddEditBondViewModel(addBondUseCase, updateBondUseCase, getBondDetailsUseCase)
        viewModel.initialize(bondId)

        // Then
        assertEquals("Edit Bond", viewModel.uiState.value.screenTitle)
        assertTrue(viewModel.uiState.value.isEditMode)
        assertEquals(testBond.name, viewModel.uiState.value.name)
        assertEquals(testBond.issuerName, viewModel.uiState.value.issuer)
        assertEquals(testBond.bondType, viewModel.uiState.value.bondType)
        assertEquals(testBond.faceValuePerBond.toString(), viewModel.uiState.value.faceValuePerBond)
        assertEquals(testBond.quantityPurchased.toString(), viewModel.uiState.value.quantityPurchased)
        assertEquals(testBond.purchasePrice.toString(), viewModel.uiState.value.purchasePrice)
        assertEquals(testBond.couponRate * 100, viewModel.uiState.value.couponRate.toDoubleOrNull())
        assertEquals(testBond.paymentFrequency, viewModel.uiState.value.paymentFrequency)
        assertEquals(testBond.notes ?: "", viewModel.uiState.value.notes)
        assertEquals(testBond.isin ?: "", viewModel.uiState.value.isin)
    }

    @Test
    fun `validateForm should return false when required fields are empty`() = runTest {
        // Given
        viewModel = AddEditBondViewModel(addBondUseCase, updateBondUseCase, getBondDetailsUseCase)
        viewModel.initialize(null)

        // When - All fields empty
        val isValid = viewModel.validateForm()

        // Then
        assertFalse(isValid)
        assertEquals(true, viewModel.uiState.value.nameError)
        assertEquals(true, viewModel.uiState.value.issuerError)
        assertEquals(true, viewModel.uiState.value.faceValueError)
        assertEquals(true, viewModel.uiState.value.quantityError)
        assertEquals(true, viewModel.uiState.value.purchasePriceError)
        assertEquals(true, viewModel.uiState.value.couponRateError)
    }

    @Test
    fun `validateForm should return true when all required fields are valid`() = runTest {
        // Given
        viewModel = AddEditBondViewModel(addBondUseCase, updateBondUseCase, getBondDetailsUseCase)
        viewModel.initialize(null)

        // Fill in all required fields
        viewModel.updateName("Test Bond")
        viewModel.updateIssuer("Test Issuer")
        viewModel.updateFaceValue("1000")
        viewModel.updateQuantity("5")
        viewModel.updatePurchasePrice("950")
        viewModel.updateCouponRate("5.0")
        viewModel.updatePurchaseDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
        viewModel.updateMaturityDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(5, DateTimeUnit.YEAR))
        viewModel.updateBondType(BondType.CORPORATE)
        viewModel.updatePaymentFrequency(PaymentFrequency.SEMI_ANNUAL)

        // When
        val isValid = viewModel.validateForm()

        // Then
        assertTrue(isValid)
        assertEquals(false, viewModel.uiState.value.nameError)
        assertEquals(false, viewModel.uiState.value.issuerError)
        assertEquals(false, viewModel.uiState.value.faceValueError)
        assertEquals(false, viewModel.uiState.value.quantityError)
        assertEquals(false, viewModel.uiState.value.purchasePriceError)
        assertEquals(false, viewModel.uiState.value.couponRateError)
    }

    @Test
    fun `saveBond in add mode should call addBondUseCase`() = runTest {
        // Given
        viewModel = AddEditBondViewModel(addBondUseCase, updateBondUseCase, getBondDetailsUseCase)
        viewModel.initialize(null)

        // Fill in required fields
        viewModel.updateName("Test Bond")
        viewModel.updateIssuer("Test Issuer")
        viewModel.updateFaceValue("1000")
        viewModel.updateQuantity("5")
        viewModel.updatePurchasePrice("950")
        viewModel.updateCouponRate("5.0")
        viewModel.updatePurchaseDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
        viewModel.updateMaturityDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(5, DateTimeUnit.YEAR))
        viewModel.updateBondType(BondType.CORPORATE)
        viewModel.updatePaymentFrequency(PaymentFrequency.SEMI_ANNUAL)

        // When
        viewModel.saveBond { /* mock navigation callback */ }

        // Then
        // Verify the addBondUseCase was called (need a proper argument matcher to verify Bond contents)
        verify(addBondUseCase).invoke(any())
    }

    @Test
    fun `saveBond in edit mode should call updateBondUseCase`() = runTest {
        // Given
        val bondId = 1L
        val testBond = createTestBond(bondId)
        whenever(getBondDetailsUseCase(bondId)).thenReturn(testBond)

        viewModel = AddEditBondViewModel(addBondUseCase, updateBondUseCase, getBondDetailsUseCase)
        viewModel.initialize(bondId)

        // Update a field
        viewModel.updateName("Updated Bond Name")

        // When
        viewModel.saveBond { /* mock navigation callback */ }

        // Then
        // Verify the updateBondUseCase was called (need a proper argument matcher to verify Bond contents)
        verify(updateBondUseCase).invoke(any())
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
            purchasePrice = 950.0,
            couponRate = 0.05,
            purchaseDate = now,
            maturityDate = later,
            paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
            notes = "Test notes",
            isin = "US1234567890"
        )
    }
}
