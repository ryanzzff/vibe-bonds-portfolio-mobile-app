package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository

/**
 * Use case for deleting a bond from the repository by its ID.
 */
class DeleteBondUseCase(private val repository: BondRepository) {

    /**
     * Executes the use case.
     * @param id The ID of the bond to delete.
     * @throws InvalidBondException if the ID is invalid.
     */
    suspend operator fun invoke(id: Long) {
        if (id <= 0) { // Assuming IDs generated by DB are positive
            throw InvalidBondException("Invalid Bond ID for deletion.")
        }
        repository.deleteBond(id)
    }
}
