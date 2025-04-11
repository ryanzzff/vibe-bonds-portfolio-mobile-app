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
import kotlin.test.assertNotNull

class AddBondUseCaseTest {

    private lateinit var fakeRepository: FakeBondRepository
    private lateinit var addBondUseCase: AddBondUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeBondRepository()
        addBondUseCase = AddBondUseCase(fakeRepository)
    }

    @Test
    fun `invoke adds bond to repository`() = runTest {
        val newBond = Bond(
            id = 0L, // ID will be assigned by fake repo
            issuerName = "Test Corp",
            isin = "US123456AB12",
            cusip = "123456AB1",
            couponRate = 5.0,
            faceValuePerBond = 1000.0,
            quantityPurchased = 10,
            purchasePrice = 99.0,
            purchaseDate = LocalDate(2024, 1, 15),
            maturityDate = LocalDate(2034, 1, 15),
            bondType = BondType.CORPORATE,
            paymentFrequency = PaymentFrequency.SEMI_ANNUAL,
            notes = "Test bond"
        )

        addBondUseCase(newBond)

        val bonds = fakeRepository.getAllBonds().first()
        assertEquals(1, bonds.size)
        val addedBond = bonds.first()
        assertNotNull(addedBond)
        assertEquals("Test Corp", addedBond.issuerName)
        assertEquals(1L, addedBond.id) // Check if ID was assigned
    }
}
