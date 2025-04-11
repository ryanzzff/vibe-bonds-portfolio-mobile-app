package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.repository.FakeBondRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetMonthlyInterestSummaryUseCaseTest {
    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var scheduleUseCase: GetPortfolioInterestScheduleUseCase
    private lateinit var summaryUseCase: GetMonthlyInterestSummaryUseCase
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val bond1 = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Corp A",
        couponRate = 4.0, // 4% annual = 2% semi-annual
        maturityDate = today.plus(1, DateTimeUnit.YEAR), // 2 more payments
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(6, DateTimeUnit.MONTH),
        purchasePrice = 1000.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 5 // Payment = 1000 * 5 * 0.04 / 2 = 100
    )

    private val bond2 = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "Treasury B",
        couponRate = 2.0, // 2% annual
        maturityDate = today.plus(2, DateTimeUnit.YEAR), // 2 more payments
        faceValuePerBond = 2000.0,
        purchaseDate = today.minus(9, DateTimeUnit.MONTH), // Next payment in ~3 months
        purchasePrice = 2010.0,
        paymentFrequency = PaymentFrequency.ANNUAL,
        quantityPurchased = 10 // Payment = 2000 * 10 * 0.02 / 1 = 400
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        scheduleUseCase = GetPortfolioInterestScheduleUseCase(fakeRepository)
        summaryUseCase = GetMonthlyInterestSummaryUseCase(scheduleUseCase)
    }

    @Test
    fun `invoke aggregates payments correctly by month`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))

        val summary = summaryUseCase().first()

        // Manually determine expected payments and their months
        val paymentsBond1 = GetAllFutureInterestPaymentsUseCase()(bond1)
        val paymentsBond2 = GetAllFutureInterestPaymentsUseCase()(bond2)
        val allPayments = paymentsBond1 + paymentsBond2

        val expectedSummary = mutableMapOf<YearMonth, Double>()
        allPayments.forEach { payment ->
            val yearMonth = YearMonth(payment.paymentDate.year, payment.paymentDate.month)
            expectedSummary[yearMonth] = expectedSummary.getOrDefault(yearMonth, 0.0) + payment.amount
        }

        assertEquals(expectedSummary.size, summary.size)
        expectedSummary.forEach { (yearMonth, amount) ->
            // Assert that the key exists and then compare the non-null value
            assertNotNull(summary[yearMonth], "Payment for $yearMonth should exist")
            assertEquals(amount, summary[yearMonth]!!, 0.001) // Use !! after checking for null
        }
    }

    @Test
    fun `invoke returns empty map when repository is empty`() = runTest {
        val summary = summaryUseCase().first()
        assertTrue(summary.isEmpty())
    }

    @Test
    fun `invoke returns sorted map`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))
        val summary = summaryUseCase().first()

        val keys = summary.keys.toList()
        for (i in 0 until keys.size - 1) {
            val current = keys[i]
            val next = keys[i + 1]
            assertTrue(current.first < next.first || (current.first == next.first && current.second.ordinal < next.second.ordinal))
        }
    }
} 