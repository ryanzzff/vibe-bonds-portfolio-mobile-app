package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.repository.FakeBondRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetBondDetailsUseCaseTest {

    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var getBondDetailsUseCase: GetBondDetailsUseCase

    private val bond1 = Bond(
        id = 1L,
        bondType = BondType.TREASURY,
        issuerName = "Issuer A",
        couponRate = 3.0,
        maturityDate = LocalDate(2033,1,1),
        faceValuePerBond = 1000.0,
        purchaseDate = LocalDate(2023,1,1),
        purchasePrice = 1000.0,
        paymentFrequency = PaymentFrequency.ANNUAL,
        quantityPurchased = 5,
        name = "A1"
    )
    private val bond2 = Bond(
        id = 2L,
        bondType = BondType.CORPORATE,
        issuerName = "Issuer B",
        couponRate = 4.0,
        maturityDate = LocalDate(2033,6,1),
        faceValuePerBond = 1000.0,
        purchaseDate = LocalDate(2023,6,1),
        purchasePrice = 990.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 10,
        name = "B2"
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        getBondDetailsUseCase = GetBondDetailsUseCase(fakeRepository)

        // Pre-populate repository
        fakeRepository.insertBonds(listOf(bond1, bond2))
    }

    @Test
    fun `invoke returns correct bond details when bond exists`() = runTest {
        val resultBond = getBondDetailsUseCase(bond2.id)

        assertEquals(bond2, resultBond)
    }

    @Test
    fun `invoke returns null when bond does not exist`() = runTest {
        val resultBond = getBondDetailsUseCase(99L) // Non-existent ID

        assertNull(resultBond)
    }
}
