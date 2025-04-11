package com.ryzoft.bondportfolioapp.shared.domain.use_case

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

class DeleteBondUseCaseTest {

    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var deleteBondUseCase: DeleteBondUseCase

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
        deleteBondUseCase = DeleteBondUseCase(fakeRepository)

        // Pre-populate repository
        fakeRepository.insertBonds(listOf(bond1, bond2))
    }

    @Test
    fun `invoke deletes correct bond from repository`() = runTest {
        var bonds = fakeRepository.getAllBonds().first()
        assertEquals(2, bonds.size)

        deleteBondUseCase(bond1.id)

        bonds = fakeRepository.getAllBonds().first()
        assertEquals(1, bonds.size)
        assertEquals(bond2, bonds.first()) // Only bond2 should remain
    }

    @Test
    fun `invoke does nothing if bond ID does not exist`() = runTest {
         var bonds = fakeRepository.getAllBonds().first()
        assertEquals(2, bonds.size)

        deleteBondUseCase(99L) // Non-existent ID

        bonds = fakeRepository.getAllBonds().first()
        assertEquals(2, bonds.size) // List should be unchanged
        assertTrue(bonds.contains(bond1))
        assertTrue(bonds.contains(bond2))
    }
}
