package com.ryzoft.bondportfolioapp.android.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun navHost_startsWithPortfolioList() {
        // Start with the AppNavHost
        composeTestRule.setContent {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
        
        // Verify we're on the portfolio list screen by checking for the "Add Bond" FAB
        composeTestRule.onNodeWithContentDescription("Add Bond").assertIsDisplayed()
    }
    
    @Test
    fun navHost_navigateToAddBondScreen() {
        // Start with the AppNavHost
        composeTestRule.setContent {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
        
        // Click on the Add Bond FAB
        composeTestRule.onNodeWithContentDescription("Add Bond").performClick()
        
        // Verify we navigated to Add Bond screen by checking for the screen title
        composeTestRule.onNodeWithText("Add Bond").assertIsDisplayed()
    }
    
    @Test
    fun navHost_navigateToBondDetailsScreen() {
        // Start with the AppNavHost - for this test, we would need a bond in the list
        // This is a more complex test that requires data setup
        // In a real test, you would either:
        // 1. Mock the repository to return test bonds
        // 2. Add a test bond to the database before running the test
        
        // For now, this is a placeholder for the test structure
        composeTestRule.setContent {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
        
        // In a real test with data:
        // composeTestRule.onNodeWithText("Test Bond Name").performClick()
        // composeTestRule.onNodeWithText("Bond Details").assertIsDisplayed()
    }
    
    @Test
    fun navHost_navigateFromDetailsToEditScreen() {
        // This is another test that requires bond data setup
        // Placeholder for the navigation test from details to edit
        
        // In a real test implementation:
        // 1. Start at the details screen for a specific bond
        // 2. Click the Edit button
        // 3. Verify we're on the Edit screen with the bond details loaded
    }
    
    @Test
    fun navHost_navigateBackFromAddScreen() {
        // Start with the AppNavHost
        composeTestRule.setContent {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
        
        // Navigate to Add Bond screen
        composeTestRule.onNodeWithContentDescription("Add Bond").performClick()
        
        // Click the back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Verify we're back on the portfolio list
        composeTestRule.onNodeWithContentDescription("Add Bond").assertIsDisplayed()
    }
}
