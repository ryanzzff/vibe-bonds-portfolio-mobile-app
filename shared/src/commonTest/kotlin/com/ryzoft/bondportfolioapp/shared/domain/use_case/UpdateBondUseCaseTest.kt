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
import kotlin.test.assertNotNull

class UpdateBondUseCaseTest {

    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var updateBondUseCase: UpdateBondUseCase

    private val originalBond = Bond(
        id = 1L,
        bondType = BondType.MUNICIPAL,
        issuerName = "Original Issuer",
        couponRate = 4.0,
        maturityDate = LocalDate(2033, 2, 10),
        faceValuePerBond = 1000.0,
        purchaseDate = LocalDate(2023, 2, 10),
        purchasePrice = 980.0,
        paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
        quantityPurchased = 5,
        cusip = "ORIG123",
        notes = "Original notes"
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        updateBondUseCase = UpdateBondUseCase(fakeRepository)

        // Pre-populate repository
        fakeRepository.insertBonds(listOf(originalBond))
    }

    @Test
    fun `invoke updates existing bond in repository`() = runTest {
        val updatedBond = originalBond.copy(
            issuerName = "Updated Issuer",
            quantityPurchased = 8,
            notes = "Updated notes"
        )

        updateBondUseCase(updatedBond)

        val resultBond = fakeRepository.getBondById(originalBond.id)

        assertNotNull(resultBond)
        assertEquals("Updated Issuer", resultBond.issuerName)
        assertEquals(8, resultBond.quantityPurchased)
        assertEquals("Updated notes", resultBond.notes)
        // Ensure other fields remain unchanged
        assertEquals(originalBond.cusip, resultBond.cusip)
        assertEquals(originalBond.couponRate, resultBond.couponRate)
    }

    @Test
    fun `invoke does nothing if bond ID does not exist`() = runTest {
        val nonExistentId = 99L // Non-existent ID
        val nonExistentBond = Bond(
            id = nonExistentId,
            bondType = BondType.CORPORATE,
            issuerName = "Non Existent",
            couponRate = 2.0,
            maturityDate = LocalDate(2029, 1, 1),
            faceValuePerBond = 500.0,
            purchaseDate = LocalDate(2024, 1, 1),
            purchasePrice = 500.0,
            paymentFrequency = PaymentFrequency.ANNUAL,
            quantityPurchased = 2,
            cusip = "NONE999",
            notes = ""
        )

        updateBondUseCase(nonExistentBond)

        // Check that the original bond is still unchanged
        val originalBondAfterAttempt = fakeRepository.getBondById(originalBond.id)
        assertEquals(originalBond, originalBondAfterAttempt)

        // Check that the non-existent bond was not added
        val bonds = fakeRepository.getAllBonds().first()
        assertEquals(1, bonds.size)
        assertEquals(originalBond.id, bonds[0].id)
    }
}
