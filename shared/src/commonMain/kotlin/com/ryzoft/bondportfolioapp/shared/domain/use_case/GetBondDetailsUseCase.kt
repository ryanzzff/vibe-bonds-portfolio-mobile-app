package com.ryzoft.bondportfolioapp.shared.domain.use_case

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository

/**
 * Use case for retrieving the details of a single bond by its ID.
 */
class GetBondDetailsUseCase(private val repository: BondRepository) {

    /**
     * Executes the use case.
     * @param id The ID of the bond to retrieve.
     * @return The Bond object, or null if not found.
     */
    suspend operator fun invoke(id: Long): Bond? {
        return repository.getBondById(id)
    }
}
