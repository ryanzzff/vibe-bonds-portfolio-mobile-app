package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.repository.FakeBondRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPortfolioInterestScheduleUseCaseTest {
    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var useCase: GetPortfolioInterestScheduleUseCase
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val bond1 = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Corp A",
        couponRate = 4.0, // 4% annual
        maturityDate = today.plus(1, DateTimeUnit.YEAR), // Matures in 1 year
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(6, DateTimeUnit.MONTH),
        purchasePrice = 1000.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 5
    )

    private val bond2 = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "Treasury B",
        couponRate = 2.0, // 2% annual
        maturityDate = today.plus(2, DateTimeUnit.YEAR), // Matures in 2 years
        faceValuePerBond = 2000.0,
        purchaseDate = today.minus(3, DateTimeUnit.MONTH),
        purchasePrice = 2010.0,
        paymentFrequency = PaymentFrequency.ANNUAL,
        quantityPurchased = 10
    )

    private val zeroCouponBond = Bond(
        id = 3L,
        bondType = BondType.CORPORATE, // BondType can be anything, PaymentFrequency is key
        issuerName = "Zero Coupon Inc",
        couponRate = 0.0,
        maturityDate = today.plus(5, DateTimeUnit.YEAR),
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(1, DateTimeUnit.MONTH),
        purchasePrice = 750.0,
        paymentFrequency = PaymentFrequency.ZERO_COUPON, // Correct enum here
        quantityPurchased = 1
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        useCase = GetPortfolioInterestScheduleUseCase(fakeRepository)
    }

    @Test
    fun `invoke combines payments from all bonds and sorts them`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2, zeroCouponBond))

        val schedule = useCase().first()

        // Calculate expected payments manually for verification
        val paymentsBond1 = GetAllFutureInterestPaymentsUseCase()(bond1)
        val paymentsBond2 = GetAllFutureInterestPaymentsUseCase()(bond2)
        // Zero coupon bond should have no payments, so it's excluded here
        val expectedTotalPayments = (paymentsBond1 + paymentsBond2).sortedBy { it.paymentDate }

        assertEquals(expectedTotalPayments.size, schedule.size)
        assertEquals(expectedTotalPayments, schedule)
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runTest {
        val schedule = useCase().first()
        assertTrue(schedule.isEmpty())
    }

    @Test
    fun `invoke handles portfolio with only zero coupon bonds`() = runTest {
        fakeRepository.insertBonds(listOf(zeroCouponBond))
        val schedule = useCase().first()
        assertTrue(schedule.isEmpty())
    }
} 