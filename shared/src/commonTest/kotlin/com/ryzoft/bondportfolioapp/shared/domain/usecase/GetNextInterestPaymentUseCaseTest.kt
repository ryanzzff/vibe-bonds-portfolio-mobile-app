package com.ryzoft.bondportfolioapp.shared.domain.usecase

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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetNextInterestPaymentUseCaseTest {
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val useCase = GetNextInterestPaymentUseCase()

    // Sample bond with semi-annual payments, purchased 6 months ago
    private val semiAnnualBond = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Test Corp",
        couponRate = 5.0,
        maturityDate = today.plus(5, DateTimeUnit.YEAR), // Matures in 5 years
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

    // Bond that matures soon
    private val shortMaturityBond = Bond(
        id = 3L,
        bondType = BondType.AGENCY,
        issuerName = "Test Agency",
        couponRate = 2.0,
        maturityDate = today.plus(2, DateTimeUnit.MONTH), // Matures in 2 months
        faceValuePerBond = 2000.0,
        purchaseDate = today.minus(1, DateTimeUnit.YEAR), // Purchased 1 year ago
        purchasePrice = 1950.0,
        paymentFrequency = PaymentFrequency.QUARTERLY, // Pays quarterly
        quantityPurchased = 3
    )

    @Test
    fun `zero coupon bond should return null`() {
        val nextPayment = useCase(zeroCouponBond)
        assertNull(nextPayment)
    }

    @Test
    fun `semi-annual bond should return the upcoming payment`() {
        val nextPayment = useCase(semiAnnualBond)
        assertNotNull(nextPayment)

        // The next payment should be the first future payment date
        val allPayments = GetAllFutureInterestPaymentsUseCase()(semiAnnualBond)
        assertEquals(allPayments.first().paymentDate, nextPayment.paymentDate)
        assertEquals(allPayments.first().amount, nextPayment.amount)
        assertEquals(semiAnnualBond.id, nextPayment.bondId)
    }

    @Test
    fun `bond purchased recently should return the upcoming payment`() {
        // Bond purchased yesterday, pays annually, first payment according to standard market cycle
        val recentBond = Bond(
            id = 4L,
            bondType = BondType.CORPORATE,
            issuerName = "Recent Corp",
            couponRate = 3.0,
            maturityDate = today.plus(10, DateTimeUnit.YEAR),
            faceValuePerBond = 1000.0,
            purchaseDate = today.minus(1, DateTimeUnit.DAY), // Purchased yesterday
            purchasePrice = 1000.0,
            paymentFrequency = PaymentFrequency.ANNUAL,
            quantityPurchased = 1
        )
        val nextPayment = useCase(recentBond)
        assertNotNull(nextPayment)

        // Instead of checking against purchase date + 1 year, 
        // we just verify that we get the correct payment from the calculator
        val allPayments = GetAllFutureInterestPaymentsUseCase()(recentBond)
        assertEquals(allPayments.first().paymentDate, nextPayment.paymentDate)
    }

    @Test
    fun `bond with no future payments should return null`() {
        // Bond that matured yesterday
        val maturedBond = Bond(
            id = 5L,
            bondType = BondType.MUNICIPAL,
            issuerName = "Matured Muni",
            couponRate = 4.0,
            maturityDate = today.minus(1, DateTimeUnit.DAY), // Matured yesterday
            faceValuePerBond = 1000.0,
            purchaseDate = today.minus(5, DateTimeUnit.YEAR),
            purchasePrice = 990.0,
            paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
            quantityPurchased = 2
        )
        val nextPayment = useCase(maturedBond)
        assertNull(nextPayment)
    }
}