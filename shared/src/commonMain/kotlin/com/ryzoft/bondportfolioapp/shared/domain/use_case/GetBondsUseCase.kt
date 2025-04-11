package com.ryzoft.bondportfolioapp.shared.domain.use_case

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving all bonds from the repository.
 */
class GetBondsUseCase(private val repository: BondRepository) {

    /**
     * Executes the use case.
     * @return A Flow emitting the list of all bonds.
     */
    operator fun invoke(): Flow<List<Bond>> {
        return repository.getAllBonds()
    }
}
