package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class InterestScheduleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun interestScheduleScreen_displaysTopAppBar() {
        // Given
        composeTestRule.setContent {
            InterestScheduleScreen(
                onBackClick = {},
                onCalendarClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Interest Schedule").assertIsDisplayed()
    }

    @Test
    fun interestScheduleScreen_displaysTabs() {
        // Given
        composeTestRule.setContent {
            InterestScheduleScreen(
                onBackClick = {},
                onCalendarClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Upcoming").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yearly").assertIsDisplayed()
    }

    @Test
    fun interestScheduleScreen_tabSwitching() {
        // Given
        composeTestRule.setContent {
            InterestScheduleScreen(
                onBackClick = {},
                onCalendarClick = {}
            )
        }

        // When: Click on Monthly tab
        composeTestRule.onNodeWithText("Monthly").performClick()

        // Then: Monthly tab content should be displayed
        // Note: In a real test with a mocked ViewModel, you would assert 
        // on specific content that appears only in the Monthly tab

        // When: Click on Yearly tab
        composeTestRule.onNodeWithText("Yearly").performClick()

        // Then: Yearly tab content should be displayed
        // Note: In a real test with a mocked ViewModel, you would assert 
        // on specific content that appears only in the Yearly tab
    }
    
    @Test
    fun calendarButtonClick_navigatesToCalendarScreen() {
        // Given
        var calendarClicked = false
        
        composeTestRule.setContent {
            InterestScheduleScreen(
                onBackClick = {},
                onCalendarClick = { calendarClicked = true }
            )
        }
        
        // When
        composeTestRule.onNodeWithContentDescription("Calendar View").performClick()
        
        // Then
        assert(calendarClicked)
    }
}