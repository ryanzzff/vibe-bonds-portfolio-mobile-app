package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.repository.FakeBondRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetBondsUseCaseTest {

    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var getBondsUseCase: GetBondsUseCase

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
        getBondsUseCase = GetBondsUseCase(fakeRepository)
    }

    @Test
    fun `invoke returns all bonds from repository`() = runTest {
        // Arrange: Add bonds to repository
        fakeRepository.insertBonds(listOf(bond1, bond2))

        // Act: Call the use case
        val resultFlow = getBondsUseCase()
        val resultBonds = resultFlow.first()

        // Assert: Check if all bonds are returned
        assertEquals(2, resultBonds.size)
        assertTrue(resultBonds.contains(bond1))
        assertTrue(resultBonds.contains(bond2))
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runTest {
        // Arrange: Repository is empty (default state after setUp)

        // Act: Call the use case
        val resultFlow = getBondsUseCase()
        val resultBonds = resultFlow.first()

        // Assert: Check if the list is empty
        assertTrue(resultBonds.isEmpty())
    }
}
