package com.ryzoft.bondportfolioapp.shared.domain.util

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YieldCalculatorTest {
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    // Bond with 5% coupon rate, bought at par
    private val parBond = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Par Corp",
        couponRate = 5.0, // 5% annual
        maturityDate = today.plus(5, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(30, DateTimeUnit.DAY),
        purchasePrice = 1000.0, // Bought at par
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 10
    )
    
    // Bond with 3% coupon, bought at a discount
    private val discountBond = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "Discount Treasury",
        couponRate = 3.0, // 3% annual
        maturityDate = today.plus(10, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(60, DateTimeUnit.DAY),
        purchasePrice = 900.0, // Discount
        paymentFrequency = PaymentFrequency.ANNUAL,
        quantityPurchased = 5
    )
    
    // Bond with 4% coupon, bought at a premium
    private val premiumBond = Bond(
        id = 3L,
        bondType = BondType.MUNICIPAL,
        issuerName = "Premium Muni",
        couponRate = 4.0, // 4% annual
        maturityDate = today.plus(3, DateTimeUnit.YEAR),
        faceValuePerBond = 5000.0,
        purchaseDate = today.minus(90, DateTimeUnit.DAY),
        purchasePrice = 5200.0, // Premium
        paymentFrequency = PaymentFrequency.QUARTERLY,
        quantityPurchased = 2
    )
    
    // Zero coupon bond
    private val zeroCouponBond = Bond(
        id = 4L,
        bondType = BondType.CORPORATE,
        issuerName = "Zero Corp",
        couponRate = 0.0,
        maturityDate = today.plus(15, DateTimeUnit.YEAR),
        faceValuePerBond = 10000.0,
        purchaseDate = today.minus(10, DateTimeUnit.DAY),
        purchasePrice = 5000.0, // Deep discount
        paymentFrequency = PaymentFrequency.ZERO_COUPON,
        quantityPurchased = 1
    )
    
    @Test
    fun `calculateAverageCouponRate returns weighted average`() {
        val bonds = listOf(parBond, discountBond, premiumBond)
        
        // Expected:
        // parBond: 5% * 10 * 1000 = 50,000
        // discountBond: 3% * 5 * 1000 = 15,000
        // premiumBond: 4% * 2 * 5000 = 40,000
        // Total weight: 10*1000 + 5*1000 + 2*5000 = 25,000
        // Weighted avg: (50,000 + 15,000 + 40,000) / 25,000 = 4.2%
        
        val expectedAvg = 4.2
        val actualAvg = YieldCalculator.calculateAverageCouponRate(bonds)
        
        assertEquals(expectedAvg, actualAvg, 0.01)
    }
    
    @Test
    fun `calculateAverageCouponRate handles empty list`() {
        assertEquals(0.0, YieldCalculator.calculateAverageCouponRate(emptyList()))
    }
    
    @Test
    fun `calculateCurrentYield for par bond is same as coupon rate`() {
        val currentYield = YieldCalculator.calculateCurrentYield(parBond)
        assertEquals(parBond.couponRate, currentYield, 0.01)
    }
    
    @Test
    fun `calculateCurrentYield for discount bond is higher than coupon rate`() {
        val currentYield = YieldCalculator.calculateCurrentYield(discountBond)
        assertTrue(currentYield > discountBond.couponRate)
        
        val expectedYield = (discountBond.faceValuePerBond * (discountBond.couponRate / 100.0) / discountBond.purchasePrice) * 100.0
        // For a discount bond with 3% coupon and 900 price: (1000 * 0.03 / 900) * 100 = 3.33%
        assertEquals(expectedYield, currentYield, 0.01)
    }
    
    @Test
    fun `calculateCurrentYield for premium bond is lower than coupon rate`() {
        val currentYield = YieldCalculator.calculateCurrentYield(premiumBond)
        assertTrue(currentYield < premiumBond.couponRate)
        
        val expectedYield = (premiumBond.faceValuePerBond * (premiumBond.couponRate / 100.0) / premiumBond.purchasePrice) * 100.0
        // For a premium bond with 4% coupon and 5200 price: (5000 * 0.04 / 5200) * 100 = 3.85%
        assertEquals(expectedYield, currentYield, 0.01)
    }
    
    @Test
    fun `calculateAverageCurrentYield returns weighted average`() {
        val bonds = listOf(parBond, discountBond, premiumBond)
        
        // Weights are based on investment amount
        val parInvestment = parBond.purchasePrice * parBond.quantityPurchased // 10,000
        val discountInvestment = discountBond.purchasePrice * discountBond.quantityPurchased // 4,500
        val premiumInvestment = premiumBond.purchasePrice * premiumBond.quantityPurchased // 10,400
        val totalInvestment = parInvestment + discountInvestment + premiumInvestment // 24,900
        
        val parYield = YieldCalculator.calculateCurrentYield(parBond) // ~5%
        val discountYield = YieldCalculator.calculateCurrentYield(discountBond) // ~3.33%
        val premiumYield = YieldCalculator.calculateCurrentYield(premiumBond) // ~3.85%
        
        val expectedAvg = (parYield * parInvestment + discountYield * discountInvestment + premiumYield * premiumInvestment) / totalInvestment
        val actualAvg = YieldCalculator.calculateAverageCurrentYield(bonds)
        
        assertEquals(expectedAvg, actualAvg, 0.01)
    }
    
    @Test
    fun `calculateYTM for bond at par approximates coupon rate`() {
        val ytm = YieldCalculator.calculateYTM(parBond)
        assertEquals(parBond.couponRate, ytm, 0.25) // Approximately equal
    }
    
    @Test
    fun `calculateYTM for discount bond is higher than coupon rate`() {
        val ytm = YieldCalculator.calculateYTM(discountBond)
        assertTrue(ytm > discountBond.couponRate)
    }
    
    @Test
    fun `calculateYTM for premium bond is lower than coupon rate`() {
        val ytm = YieldCalculator.calculateYTM(premiumBond)
        assertTrue(ytm < premiumBond.couponRate)
    }
    
    @Test
    fun `calculateYTM for zero coupon bond is close to expected`() {
        val ytm = YieldCalculator.calculateYTM(zeroCouponBond)
        
        // Expected YTM for zero coupon bond:
        // YTM = (FV/Price)^(1/years) - 1
        // With FV=10000, Price=5000, years=15: (10000/5000)^(1/15) - 1 = 0.0472 = 4.72%
        val expectedYTM = ((zeroCouponBond.faceValuePerBond / zeroCouponBond.purchasePrice).pow(1.0 / 15.0) - 1.0) * 100.0
        
        assertEquals(expectedYTM, ytm, 0.1) // Within 0.1 percentage point
    }
    
    @Test
    fun `calculateAverageYTM returns weighted average`() {
        val bonds = listOf(parBond, discountBond, zeroCouponBond)
        val avgYTM = YieldCalculator.calculateAverageYTM(bonds)
        
        // Verify it's a reasonable number between minimum and maximum values
        val minYTM = minOf(
            YieldCalculator.calculateYTM(parBond),
            YieldCalculator.calculateYTM(discountBond),
            YieldCalculator.calculateYTM(zeroCouponBond)
        )
        val maxYTM = maxOf(
            YieldCalculator.calculateYTM(parBond),
            YieldCalculator.calculateYTM(discountBond),
            YieldCalculator.calculateYTM(zeroCouponBond)
        )
        
        assertTrue(avgYTM >= minYTM - 0.01 && avgYTM <= maxYTM + 0.01)
    }
} 