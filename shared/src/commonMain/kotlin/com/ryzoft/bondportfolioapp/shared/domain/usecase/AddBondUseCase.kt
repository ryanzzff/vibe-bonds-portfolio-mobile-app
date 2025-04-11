package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Use case for adding a new bond to the repository.
 */
class AddBondUseCase(private val repository: BondRepository) {

    /**
     * Executes the use case.
     * @param bond The bond object to add.
     * @throws InvalidBondException if the bond data is invalid.
     */
    suspend operator fun invoke(bond: Bond) {
        // Example Validation:
        if (bond.issuerName.isBlank()) {
            throw InvalidBondException("Issuer name cannot be empty.")
        }
        if (bond.couponRate < 0) {
            throw InvalidBondException("Coupon rate cannot be negative.")
        }
        if (bond.faceValuePerBond <= 0) {
            throw InvalidBondException("Face value must be positive.")
        }
        if (bond.quantityPurchased <= 0) {
            throw InvalidBondException("Quantity must be positive.")
        }
        if (bond.maturityDate < bond.purchaseDate) {
            throw InvalidBondException("Maturity date cannot be before purchase date.")
        }
        // Add more validation as needed...

        // Note: The Bond object passed in probably shouldn't have an ID yet, or it should be 0/null.
        // The repository implementation handles the actual insertion and ID generation.
        repository.addBond(bond)
    }
}

class InvalidBondException(message: String): Exception(message)
