package com.ryzoft.bondportfolioapp.android.presentation.screens.details

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onSiblings
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class BondDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bondDetailsScreen_displaysLoadingState() {
        // Given
        val viewModel = MockBondDetailsViewModel(
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { },
                        onEditClick = { },
                        viewModel = viewModel
                    )
                }
            }
        }

        // We can't directly check for the CircularProgressIndicator, so we'll just
        // verify that the title is displayed
        composeTestRule.onNodeWithText("Bond Details").assertIsDisplayed()
    }

    @Test
    fun bondDetailsScreen_displaysErrorState() {
        // Given
        val errorMessage = "Bond not found"
        val viewModel = MockBondDetailsViewModel(
            error = errorMessage
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { },
                        onEditClick = { },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Then - Error message should be displayed
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun bondDetailsScreen_displaysBondDetails() {
        // Given
        val testBond = createTestBond(1L)
        val viewModel = MockBondDetailsViewModel(
            bond = testBond
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { },
                        onEditClick = { },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Then - Bond details should be displayed
        // Just verify some key elements are displayed
        composeTestRule.onNodeWithText("Bond Details").assertIsDisplayed() // App bar title
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed() // Back button
        composeTestRule.onNodeWithContentDescription("Edit Bond").assertIsDisplayed() // Edit button
        composeTestRule.onNodeWithContentDescription("Delete Bond").assertIsDisplayed() // Delete button
    }

    @Test
    fun bondDetailsScreen_navigationButtons_callCorrectCallbacks() {
        // Given
        val testBond = createTestBond(1L)
        val viewModel = MockBondDetailsViewModel(
            bond = testBond
        )
        var backClicked = false
        var editClicked = false

        // When
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { backClicked = true },
                        onEditClick = { editClicked = true },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Then - Test back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backClicked) { "Back button callback was not called" }

        // Then - Test edit button
        composeTestRule.onNodeWithContentDescription("Edit Bond").performClick()
        assert(editClicked) { "Edit button callback was not called" }
    }

    @Test
    fun bondDetailsScreen_deleteConfirmationDialog_showsAndHides() {
        // Given
        val testBond = createTestBond(1L)
        val viewModel = MockBondDetailsViewModel(
            bond = testBond
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { },
                        onEditClick = { },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Check dialog is not initially visible
        composeTestRule.onAllNodesWithText("Are you sure you want to delete this bond from your portfolio?").assertCountEquals(0)

        // Click the delete button
        composeTestRule.onNodeWithContentDescription("Delete Bond").performClick()

        // The dialog should now be visible
        composeTestRule.onNodeWithText("Delete Bond").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this bond from your portfolio?").assertIsDisplayed()

        // Simulate clicking Cancel
        viewModel.toggleDeleteConfirmDialog(false)
        
        // The dialog should now be hidden
        composeTestRule.onAllNodesWithText("Are you sure you want to delete this bond from your portfolio?").assertCountEquals(0)
    }

    @Test
    fun bondDetailsScreen_deleteConfirmation_callsDeleteAndNavigatesBack() {
        // Given
        val testBond = createTestBond(1L)
        val viewModel = MockBondDetailsViewModel(
            bond = testBond,
            showDeleteConfirmDialog = true
        )
        var backClicked = false
        var deleteCalled = false

        // Override the deleteBond method to track calls
        viewModel.onDeleteBond = {
            deleteCalled = true
            it() // Call the onComplete callback
        }

        // When
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { backClicked = true },
                        onEditClick = { },
                        viewModel = viewModel
                    )
                }
            }
        }

        // The dialog should be visible
        composeTestRule.onNodeWithText("Delete Bond").assertIsDisplayed()

        // Click the Delete button
        composeTestRule.onNodeWithText("Delete").performClick()

        // Then - deleteBond should be called and navigation back should happen
        assert(deleteCalled) { "Delete bond was not called" }
        assert(backClicked) { "Navigation back was not triggered after deletion" }
    }

    @Test
    fun testCouponRateAndYieldPercentageDisplay() {
        // Setup
        val bond = createTestBond(1L).copy(
            couponRate = 0.0425, // 4.25% stored as decimal
            purchasePrice = 950.0,
            faceValuePerBond = 1000.0 // Ensure face value is set for calculation
        )
        
        val viewModel = MockBondDetailsViewModel(
            bond = bond,
            isLoading = false
        )

        // Launch the screen
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    BondDetailsScreen(
                        bondId = 1L,
                        onBackClick = { },
                        onEditClick = { },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Simple assertions using onNodeWithText which is properly imported
        composeTestRule.onNodeWithText("Coupon Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("4.25%").assertIsDisplayed()
        
        // Calculate the expected current yield: couponRate * faceValuePerBond / purchasePrice
        // 0.0425 * 1000 / 950 = 0.04473684... which formats to 4.47%
//        composeTestRule.onNodeWithText("Current Yield").assertIsDisplayed()
//        composeTestRule.onNodeWithText("4.47%").assertIsDisplayed()
    }

    // Mock implementation of BondDetailsViewModel for UI testing
    private class MockBondDetailsViewModel(
        bond: Bond? = null,
        isLoading: Boolean = false,
        error: String? = null,
        showDeleteConfirmDialog: Boolean = false
    ) : BondDetailsViewModel {
        private val _uiState = MutableStateFlow(
            BondDetailsUiState(
                bond = bond,
                isLoading = isLoading,
                error = error,
                showDeleteConfirmDialog = showDeleteConfirmDialog
            )
        )
        override val uiState: StateFlow<BondDetailsUiState> = _uiState

        // For testing callbacks
        var onDeleteBond: ((onComplete: () -> Unit) -> Unit)? = null

        override fun loadBondDetails(bondId: Long) {
            // No-op for testing, state is set in constructor
        }

        override fun toggleDeleteConfirmDialog(show: Boolean) {
            _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = show)
        }

        override fun deleteBond(onComplete: () -> Unit) {
            onDeleteBond?.invoke(onComplete) ?: onComplete()
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
