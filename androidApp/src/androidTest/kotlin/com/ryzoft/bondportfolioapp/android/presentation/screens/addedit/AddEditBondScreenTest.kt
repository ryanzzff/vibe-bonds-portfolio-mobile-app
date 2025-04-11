package com.ryzoft.bondportfolioapp.android.presentation.screens.addedit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test

class AddEditBondScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addBondScreen_displaysEmptyForm() {
        // Given
        val viewModel = TestAddEditBondViewModel(null)
        var navigateBackCalled = false
        var saveCompleteCalled = false

        // When
        composeTestRule.setContent {
            AddEditBondScreen(
                bondId = null,
                onBackClick = { navigateBackCalled = true },
                onSaveComplete = { saveCompleteCalled = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Add Bond").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bond Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Issuer Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("ISIN/CUSIP").assertIsDisplayed()
        composeTestRule.onNodeWithText("Face Value Per Bond *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quantity Purchased *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Purchase Price (per 100 face value) *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coupon Rate (%) *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment Frequency *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Purchase Date *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Maturity Date *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Bond").assertIsDisplayed()
    }

    @Test
    fun editBondScreen_displaysFilledForm() {
        // Given
        val testBond = createTestBond(1L)
        val viewModel = TestAddEditBondViewModel(1L, testBond)
        var navigateBackCalled = false
        var saveCompleteCalled = false

        // When
        composeTestRule.setContent {
            AddEditBondScreen(
                bondId = 1L,
                onBackClick = { navigateBackCalled = true },
                onSaveComplete = { saveCompleteCalled = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Edit Bond").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Corporate Bond").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Corp").assertIsDisplayed()
        composeTestRule.onNodeWithText("Corporate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Semi-Annual").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Bond").assertIsDisplayed()
    }

    @Test
    fun navigationButtons_callCorrectCallbacks() {
        // Given
        val viewModel = TestAddEditBondViewModel(null)
        var navigateBackCalled = false
        var saveCompleteCalled = false

        // When
        composeTestRule.setContent {
            AddEditBondScreen(
                bondId = null,
                onBackClick = { navigateBackCalled = true },
                onSaveComplete = { saveCompleteCalled = true },
                viewModel = viewModel
            )
        }

        // Then - Test back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(navigateBackCalled)

        // Test save button
        composeTestRule.onNodeWithText("Save Bond").performClick()
        assert(saveCompleteCalled)
    }

    @Test
    fun formValidation_showsErrorsForEmptyRequiredFields() {
        // Given
        val viewModel = TestAddEditBondViewModel(null)
        viewModel.setValidationErrors(true)

        // When
        composeTestRule.setContent {
            AddEditBondScreen(
                bondId = null,
                onBackClick = { },
                onSaveComplete = { },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Issuer name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Valid face value is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Valid quantity is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Valid purchase price is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Valid coupon rate is required").assertIsDisplayed()
    }

    // Test implementation of AddEditBondViewModel for UI testing
    private class TestAddEditBondViewModel(
        private val bondId: Long?,
        private val testBond: Bond? = null
    ) : AddEditBondViewModel {
        private val _uiState = MutableStateFlow(
            if (bondId != null && testBond != null) {
                AddEditBondUiState(
                    isEditMode = true,
                    isLoading = false,
                    screenTitle = "Edit Bond",
                    name = testBond.name ?: "",
                    issuer = testBond.issuerName,
                    isin = testBond.isin ?: "",
                    bondType = testBond.bondType,
                    faceValuePerBond = testBond.faceValuePerBond.toString(),
                    quantityPurchased = testBond.quantityPurchased.toString(),
                    purchasePrice = testBond.purchasePrice.toString(),
                    couponRate = (testBond.couponRate * 100).toString(),
                    paymentFrequency = testBond.paymentFrequency,
                    purchaseDate = testBond.purchaseDate,
                    maturityDate = testBond.maturityDate,
                    notes = testBond.notes ?: ""
                )
            } else {
                AddEditBondUiState(
                    isEditMode = false,
                    isLoading = false,
                    screenTitle = "Add Bond"
                )
            }
        )
        override val uiState = _uiState

        fun setValidationErrors(hasErrors: Boolean) {
            _uiState.value = _uiState.value.copy(
                nameError = hasErrors,
                issuerError = hasErrors,
                faceValueError = hasErrors,
                quantityError = hasErrors,
                purchasePriceError = hasErrors,
                couponRateError = hasErrors
            )
        }

        override fun initialize(bondId: Long?) {
            // Already initialized in constructor
        }

        override fun updateName(name: String) {
            _uiState.value = _uiState.value.copy(name = name, nameError = false)
        }

        override fun updateIssuer(issuer: String) {
            _uiState.value = _uiState.value.copy(issuer = issuer, issuerError = false)
        }

        override fun updateIsin(isin: String) {
            _uiState.value = _uiState.value.copy(isin = isin)
        }

        override fun updateBondType(bondType: BondType) {
            _uiState.value = _uiState.value.copy(bondType = bondType)
        }

        override fun updateFaceValue(value: String) {
            _uiState.value = _uiState.value.copy(faceValuePerBond = value, faceValueError = false)
        }

        override fun updateQuantity(quantity: String) {
            _uiState.value = _uiState.value.copy(quantityPurchased = quantity, quantityError = false)
        }

        override fun updatePurchasePrice(price: String) {
            _uiState.value = _uiState.value.copy(purchasePrice = price, purchasePriceError = false)
        }

        override fun updateCouponRate(rate: String) {
            _uiState.value = _uiState.value.copy(couponRate = rate, couponRateError = false)
        }

        override fun updatePaymentFrequency(frequency: PaymentFrequency) {
            _uiState.value = _uiState.value.copy(paymentFrequency = frequency)
        }

        override fun updatePurchaseDate(date: LocalDate) {
            _uiState.value = _uiState.value.copy(purchaseDate = date)
        }

        override fun updateMaturityDate(date: LocalDate) {
            _uiState.value = _uiState.value.copy(maturityDate = date)
        }

        override fun updateNotes(notes: String) {
            _uiState.value = _uiState.value.copy(notes = notes)
        }

        override fun validateForm(): Boolean {
            return !(_uiState.value.nameError || _uiState.value.issuerError || 
                    _uiState.value.faceValueError || _uiState.value.quantityError || 
                    _uiState.value.purchasePriceError || _uiState.value.couponRateError)
        }

        override fun saveBond(onComplete: () -> Unit) {
            onComplete()
        }
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
            couponRate = 0.05, // 5%
            paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
            purchaseDate = now,
            maturityDate = later,
            currency = "USD",
            isin = "US123456AB12",
            notes = "Test notes"
        )
    }
}
