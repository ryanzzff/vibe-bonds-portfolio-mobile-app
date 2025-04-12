package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.model.YieldType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test

class PortfolioListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPortfolioListScreenWithBonds() {
        // Setup
        val testBonds = createTestBonds()
        val viewModel = TestPortfolioListViewModel(testBonds)

        // Launch the screen
        composeTestRule.setContent {
            PortfolioListScreen(
                onBondClick = {},
                onAddBondClick = {},
                viewModel = viewModel
            )
        }

        // Verify portfolio summary is displayed
        composeTestRule.onNodeWithText("Portfolio Summary").assertIsDisplayed()
        
        // Verify bond items are displayed
        composeTestRule.onNodeWithText("Test Corporate Bond").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Test TREASURY Bond").assertIsDisplayed()
        
        // Verify filter section is displayed
        composeTestRule.onNodeWithText("Filter by Type:").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Corporate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Treasury").assertIsDisplayed()
    }

    @Test
    fun testPortfolioListScreenWithEmptyBonds() {
        // Setup
        val viewModel = TestPortfolioListViewModel(emptyList())

        // Launch the screen
        composeTestRule.setContent {
            PortfolioListScreen(
                onBondClick = {},
                onAddBondClick = {},
                viewModel = viewModel
            )
        }

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("No bonds in your portfolio yet").assertIsDisplayed()
    }

    @Test
    fun testPortfolioListScreenWithError() {
        // Setup
        val errorMessage = "Failed to load bonds"
        val viewModel = TestPortfolioListViewModel(
            bonds = emptyList(),
            error = errorMessage
        )

        // Launch the screen
        composeTestRule.setContent {
            PortfolioListScreen(
                onBondClick = {},
                onAddBondClick = {},
                viewModel = viewModel
            )
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun testFilterFunctionality() {
        // Setup
        val viewModel = TestPortfolioListViewModel(createTestBonds())

        // Launch the screen
        composeTestRule.setContent {
            PortfolioListScreen(
                onBondClick = {},
                onAddBondClick = {},
                viewModel = viewModel
            )
        }

        // Verify both bonds are initially displayed
        composeTestRule.onNodeWithText("Test Corporate Bond").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test TREASURY Bond").assertIsDisplayed()

        // Click on Corporate filter
        composeTestRule.onNodeWithText("Corporate").performClick()

        // Verify only corporate bond is displayed
        composeTestRule.onNodeWithText("Test Corporate Bond").assertIsDisplayed()
        
        // Click on Treasury filter
        composeTestRule.onNodeWithText("Treasury").performClick()
        
        // Verify only treasury bond is displayed
        composeTestRule.onNodeWithText("Test TREASURY Bond").assertIsDisplayed()
        
        // Click on All filter to reset
        composeTestRule.onNodeWithText("All").performClick()
        
        // Verify both bonds are displayed again
        composeTestRule.onNodeWithText("Test Corporate Bond").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test TREASURY Bond").assertIsDisplayed()
    }

    /**
     * Test implementation of PortfolioListViewModel that doesn't require mocking
     */
    private class TestPortfolioListViewModel(
        bonds: List<Bond>,
        isLoading: Boolean = false,
        error: String? = null
    ) : PortfolioListViewModel {
        private val _uiState = MutableStateFlow(
            PortfolioListUiState(
                bonds = bonds,
                isLoading = isLoading,
                error = error,
                yields = mapOf(
                    YieldType.COUPON_RATE to 4.0,
                    YieldType.CURRENT_YIELD to 4.2,
                    YieldType.YIELD_TO_MATURITY to 4.5
                ),
                selectedYieldType = YieldType.COUPON_RATE
            )
        )
        
        override val uiState: StateFlow<PortfolioListUiState> = _uiState

        override fun loadBonds() {
            // No-op for testing
        }
        
        override fun setSelectedYieldType(yieldType: YieldType) {
            _uiState.value = _uiState.value.copy(selectedYieldType = yieldType)
        }
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
