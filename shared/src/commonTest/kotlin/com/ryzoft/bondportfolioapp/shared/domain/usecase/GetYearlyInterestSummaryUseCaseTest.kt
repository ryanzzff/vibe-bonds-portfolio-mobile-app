package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.repository.FakeBondRepository
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

class GetYearlyInterestSummaryUseCaseTest {
    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var scheduleUseCase: GetPortfolioInterestScheduleUseCase
    private lateinit var summaryUseCase: GetYearlyInterestSummaryUseCase
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Bond 1: Pays 100 semi-annually, 2 payments left this year, 2 next year
    private val bond1 = Bond(
        id = 1L,
        bondType = BondType.CORPORATE,
        issuerName = "Corp A",
        couponRate = 4.0,
        maturityDate = today.plus(1, DateTimeUnit.YEAR).plus(1, DateTimeUnit.MONTH), // Matures in > 1 year
        faceValuePerBond = 1000.0,
        purchaseDate = today.minus(6, DateTimeUnit.MONTH), // First payment date likely passed
        purchasePrice = 1000.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 5 // Payment = 100
    )

    // Bond 2: Pays 400 annually, 1 payment this year, 1 next year, 1 year after
    private val bond2 = Bond(
        id = 2L,
        bondType = BondType.TREASURY,
        issuerName = "Treasury B",
        couponRate = 2.0,
        maturityDate = today.plus(2, DateTimeUnit.YEAR).plus(4, DateTimeUnit.MONTH), // Matures in > 2 years
        faceValuePerBond = 2000.0,
        purchaseDate = today.minus(9, DateTimeUnit.MONTH), // Next payment in ~3 months
        purchasePrice = 2010.0,
        paymentFrequency = PaymentFrequency.ANNUAL,
        quantityPurchased = 10 // Payment = 400
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        scheduleUseCase = GetPortfolioInterestScheduleUseCase(fakeRepository)
        summaryUseCase = GetYearlyInterestSummaryUseCase(scheduleUseCase)
    }

    @Test
    fun `invoke aggregates payments correctly by year`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))

        val summary = summaryUseCase().first()

        // Manually calculate expected yearly totals
        val paymentsBond1 = GetAllFutureInterestPaymentsUseCase()(bond1)
        val paymentsBond2 = GetAllFutureInterestPaymentsUseCase()(bond2)
        val allPayments = paymentsBond1 + paymentsBond2

        val expectedSummary = mutableMapOf<Int, Double>()
        allPayments.forEach { payment ->
            val year = payment.paymentDate.year
            expectedSummary[year] = expectedSummary.getOrDefault(year, 0.0) + payment.amount
        }

        assertEquals(expectedSummary.size, summary.size)
        expectedSummary.forEach { (year, amount) ->
            // Assert that the key exists and then compare the non-null value
            assertNotNull(summary[year], "Payment for $year should exist")
            assertEquals(amount, summary[year]!!, 0.001) // Use !! after checking for null
        }
    }

    @Test
    fun `invoke returns empty map when repository is empty`() = runTest {
        val summary = summaryUseCase().first()
        assertTrue(summary.isEmpty())
    }

    @Test
    fun `invoke returns sorted map by year`() = runTest {
        fakeRepository.insertBonds(listOf(bond1, bond2))
        val summary = summaryUseCase().first()

        val keys = summary.keys.toList()
        for (i in 0 until keys.size - 1) {
            assertTrue(keys[i] < keys[i + 1])
        }
    }
} 