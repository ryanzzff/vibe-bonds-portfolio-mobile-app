package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.model.YieldType
import com.ryzoft.bondportfolioapp.shared.domain.repository.FakeBondRepository
import com.ryzoft.bondportfolioapp.shared.domain.util.YieldCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CalculateAverageYieldUseCaseTest {
    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var useCase: CalculateAverageYieldUseCase
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    // Test bonds
    private val bond1 = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Test Corp",
        couponRate = 5.0,
        maturityDate = today.plus(5, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(30, DateTimeUnit.DAY),
        purchasePrice = 1000.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 10
    )
    
    private val bond2 = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "Test Treasury",
        couponRate = 3.0,
        maturityDate = today.plus(10, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(60, DateTimeUnit.DAY),
        purchasePrice = 950.0,
        paymentFrequency = PaymentFrequency.ANNUAL,
        quantityPurchased = 5
    )
    
    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        useCase = CalculateAverageYieldUseCase(fakeRepository)
    }
    
    @Test
    fun `invoke returns 0 when repository is empty`() = runTest {
        val result = useCase(YieldType.COUPON_RATE).first()
        assertEquals(0.0, result)
    }
    
    @Test
    fun `invoke returns correct coupon rate`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))
        
        val result = useCase(YieldType.COUPON_RATE).first()
        val expected = YieldCalculator.calculateAverageCouponRate(listOf(bond1, bond2))
        
        assertEquals(expected, result, 0.01)
    }
    
    @Test
    fun `invoke returns correct current yield`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))
        
        val result = useCase(YieldType.CURRENT_YIELD).first()
        val expected = YieldCalculator.calculateAverageCurrentYield(listOf(bond1, bond2))
        
        assertEquals(expected, result, 0.01)
    }
    
    @Test
    fun `invoke returns correct yield to maturity`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))
        
        val result = useCase(YieldType.YIELD_TO_MATURITY).first()
        val expected = YieldCalculator.calculateAverageYTM(listOf(bond1, bond2))
        
        assertEquals(expected, result, 0.01)
    }
    
    @Test
    fun `calculateAllYields returns map with all yield types`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))
        
        val results = useCase.calculateAllYields().first()
        
        // Check that all yield types are included
        assertEquals(YieldType.values().size, results.size)
        
        // Check that each yield type has a value
        YieldType.values().forEach { yieldType ->
            assertTrue(results.containsKey(yieldType))
            
            // Check that the values match what we'd get from invoke
            val expectedValue = useCase(yieldType).first()
            val actualValue = results[yieldType]
            assertNotNull(actualValue, "Value for $yieldType should not be null")
            assertEquals(expectedValue, actualValue, 0.01)
        }
    }
    
    @Test
    fun `calculateAllYields returns zeros for empty repository`() = runTest {
        val results = useCase.calculateAllYields().first()
        
        assertEquals(YieldType.values().size, results.size)
        
        YieldType.values().forEach { yieldType ->
            val value = results[yieldType]
            assertNotNull(value, "Value for $yieldType should not be null")
            assertEquals(0.0, value)
        }
    }
} 