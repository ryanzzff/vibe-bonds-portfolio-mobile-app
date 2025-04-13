package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllFutureInterestPaymentsUseCaseTest {
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val useCase = GetAllFutureInterestPaymentsUseCase()
    
    // Sample corporate bond with semi-annual payments
    private val sampleBond = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "XYZ Corp",
        couponRate = 0.045, // 4.5% annual coupon as decimal (0.045)
        maturityDate = today.plus(3, DateTimeUnit.YEAR), // 3 years from today
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(3, DateTimeUnit.MONTH), // Purchased 3 months ago
        purchasePrice = 990.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 5
    )
    
    // Zero coupon bond
    private val zeroCouponBond = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "US Treasury",
        couponRate = 0.0,
        maturityDate = today.plus(5, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(1, DateTimeUnit.MONTH),
        purchasePrice = 800.0,
        paymentFrequency = PaymentFrequency.ZERO_COUPON,
        quantityPurchased = 10
    )
    
    @Test
    fun `regular bond should have correct number of payments`() {
        val payments = useCase(sampleBond)
        
        // Expect 6 payments over 3 years (2 per year)
        assertEquals(6, payments.size)
    }
    
    @Test
    fun `zero coupon bond should return empty list`() {
        val payments = useCase(zeroCouponBond)
        
        assertTrue(payments.isEmpty())
    }
    
    @Test
    fun `payment amount should be calculated correctly`() {
        val payments = useCase(sampleBond)
        
        // Payment amount: $1000 face value × 5 bonds × 4.5% coupon rate ÷ 2 payments per year = $112.5
        assertEquals(112.5, payments.first().amount)
    }
    
    @Test
    fun `payments should be associated with correct bond ID`() {
        val payments = useCase(sampleBond)
        
        payments.forEach { payment ->
            assertEquals(sampleBond.id, payment.bondId)
        }
    }
    
    @Test
    fun `payments should be in chronological order`() {
        val payments = useCase(sampleBond)
        
        for (i in 0 until payments.size - 1) {
            assertTrue(payments[i].paymentDate < payments[i + 1].paymentDate)
        }
    }
}