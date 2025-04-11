package com.ryzoft.bondportfolioapp.shared.domain.util

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterestCalculatorTest {
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    // Sample bond with semi-annual payments
    private val semiAnnualBond = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Test Corp",
        couponRate = 5.0, // 5% annual coupon
        maturityDate = today.plus(5, DateTimeUnit.YEAR), // 5 years from today
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(6, DateTimeUnit.MONTH), // Purchased 6 months ago
        purchasePrice = 980.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 10
    )
    
    // Zero coupon bond
    private val zeroCouponBond = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "Test Treasury",
        couponRate = 0.0,
        maturityDate = today.plus(10, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(1, DateTimeUnit.MONTH),
        purchasePrice = 700.0,
        paymentFrequency = PaymentFrequency.ZERO_COUPON,
        quantityPurchased = 5
    )
    
    // Monthly payment bond
    private val monthlyBond = Bond(
        id = 3L,
        bondType = BondType.MUNICIPAL,
        issuerName = "Test Municipality",
        couponRate = 3.6, // 3.6% annual coupon
        maturityDate = today.plus(2, DateTimeUnit.YEAR),
        faceValuePerBond = 5000.0,
        purchaseDate = today.minus(2, DateTimeUnit.MONTH),
        purchasePrice = 5100.0,
        paymentFrequency = PaymentFrequency.MONTHLY,
        quantityPurchased = 2
    )
    
    @Test
    fun `zero coupon bond should have no payments`() {
        val payments = InterestCalculator.calculateFuturePayments(zeroCouponBond)
        assertTrue(payments.isEmpty())
    }
    
    @Test
    fun `semi-annual bond should have correct payment count`() {
        val payments = InterestCalculator.calculateFuturePayments(semiAnnualBond)
        
        // Expect 10 payments over 5 years (2 per year)
        assertEquals(10, payments.size)
    }
    
    @Test
    fun `semi-annual bond should have correct payment amount`() {
        val payments = InterestCalculator.calculateFuturePayments(semiAnnualBond)
        
        // Payment amount should be: $1000 face value × 10 bonds × 5% coupon rate ÷ 2 payments per year = $250
        assertEquals(250.0, payments.first().amount)
    }
    
    @Test
    fun `monthly bond should have correct payment count`() {
        val payments = InterestCalculator.calculateFuturePayments(monthlyBond)
        
        // Expect 24 payments over 2 years (12 per year)
        assertEquals(24, payments.size)
    }
    
    @Test
    fun `monthly bond should have correct payment amount`() {
        val payments = InterestCalculator.calculateFuturePayments(monthlyBond)
        
        // Payment amount: $5000 face value × 2 bonds × 3.6% coupon rate ÷ 12 payments per year = $30
        assertEquals(30.0, payments.first().amount)
    }
    
    @Test
    fun `payment dates should be chronological`() {
        val payments = InterestCalculator.calculateFuturePayments(semiAnnualBond)
        
        for (i in 0 until payments.size - 1) {
            assertTrue(payments[i].paymentDate < payments[i + 1].paymentDate)
        }
    }
    
    @Test
    fun `payment dates should be correctly spaced`() {
        val payments = InterestCalculator.calculateFuturePayments(semiAnnualBond)
        
        // For semi-annual bonds, payments should be 6 months apart
        for (i in 0 until payments.size - 1) {
            val currentDate = payments[i].paymentDate
            val nextDate = payments[i + 1].paymentDate
            
            // Calculate difference in months
            val currentYear = currentDate.year
            val currentMonth = currentDate.monthNumber
            val nextYear = nextDate.year
            val nextMonth = nextDate.monthNumber
            
            val monthsDiff = (nextYear - currentYear) * 12 + (nextMonth - currentMonth)
            
            assertEquals(6, monthsDiff)
        }
    }
} 