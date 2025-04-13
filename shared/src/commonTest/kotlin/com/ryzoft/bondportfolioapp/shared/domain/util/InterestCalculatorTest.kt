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
        couponRate = 0.05, // 5% annual coupon (decimal)
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
        couponRate = 0.036, // 3.6% annual coupon (decimal)
        maturityDate = today.plus(2, DateTimeUnit.YEAR),
        faceValuePerBond = 5000.0,
        purchaseDate = today.minus(2, DateTimeUnit.MONTH),
        purchasePrice = 5100.0,
        paymentFrequency = PaymentFrequency.MONTHLY,
        quantityPurchased = 2
    )
    
    // Bonds with identical maturity date but different purchase dates
    private val earlyPurchaseBond = Bond(
        id = 4L,
        bondType = BondType.CORPORATE,
        issuerName = "Same Maturity Corp",
        couponRate = 0.04, // 4% annual
        maturityDate = today.plus(3, DateTimeUnit.YEAR).plus(4, DateTimeUnit.MONTH),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(1, DateTimeUnit.YEAR), // Purchased a year ago
        purchasePrice = 950.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 3
    )
    
    private val recentPurchaseBond = Bond(
        id = 5L,
        bondType = BondType.CORPORATE,
        issuerName = "Same Maturity Corp",
        couponRate = 0.04, // 4% annual
        maturityDate = today.plus(3, DateTimeUnit.YEAR).plus(4, DateTimeUnit.MONTH), // Same maturity as earlyPurchaseBond
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(10, DateTimeUnit.DAY), // Purchased recently
        purchasePrice = 980.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
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
    
    @Test
    fun `bonds with same maturity but different purchase dates should have identical payment schedules`() {
        val earlyPurchasePayments = InterestCalculator.calculateFuturePayments(earlyPurchaseBond)
        val recentPurchasePayments = InterestCalculator.calculateFuturePayments(recentPurchaseBond)
        
        // Both should have same number of future payments
        assertTrue(earlyPurchasePayments.size > 0)
        assertEquals(earlyPurchasePayments.size, recentPurchasePayments.size)
        
        // Payment dates should match, regardless of purchase date
        for (i in earlyPurchasePayments.indices) {
            assertEquals(
                earlyPurchasePayments[i].paymentDate,
                recentPurchasePayments[i].paymentDate,
                "Payment dates should be identical regardless of purchase date"
            )
        }
    }
    
    @Test
    fun `payment dates should align with maturity date cycle`() {
        // For a bond with semi-annual payments, payment dates should be on the same day of month as maturity
        val maturityDay = semiAnnualBond.maturityDate.dayOfMonth
        val payments = InterestCalculator.calculateFuturePayments(semiAnnualBond)
        
        for (payment in payments) {
            assertEquals(
                maturityDay, 
                payment.paymentDate.dayOfMonth,
                "Payment day of month should match maturity date day of month"
            )
        }
    }
}