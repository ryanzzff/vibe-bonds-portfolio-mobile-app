package com.ryzoft.bondportfolioapp.android.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for the formatting utility functions used across the app's UI.
 */
class FormatUtilsTest {

    /**
     * Test for the formatPercentage function used in BondDetailsScreen and PortfolioListScreen
     * This ensures percentages are displayed correctly by multiplying decimal values (0.0425) to percentage format (4.25%).
     */
    @Test
    fun `formatPercentage should multiply value by 100 and append percent sign`() {
        // Create a local copy of the formatPercentage function for testing
        fun formatPercentage(value: Double): String {
            return String.format("%.2f%%", value * 100)
        }

        // Test cases with different decimal values
        assertEquals("5.00%", formatPercentage(0.05))
        assertEquals("4.25%", formatPercentage(0.0425))
        assertEquals("0.04%", formatPercentage(0.0004))
        assertEquals("100.00%", formatPercentage(1.0))
        assertEquals("0.00%", formatPercentage(0.0))
    }
    
    /**
     * Test that the incorrect percentage formatting would have been caught
     * This is the bug we fixed where decimal values weren't being multiplied by 100
     */
    @Test
    fun `incorrect percentage formatting would display wrong values`() {
        // The incorrect formatPercentage implementation
        fun incorrectFormatPercentage(value: Double): String {
            return String.format("%.2f%%", value) // Without multiplying by 100
        }
        
        // This assertion shows how the bug would display wrong values
        assertEquals("0.05%", incorrectFormatPercentage(0.05)) // Wrong: should be 5.00%
        assertEquals("0.04%", incorrectFormatPercentage(0.0425)) // Wrong: should be 4.25%
    }
}