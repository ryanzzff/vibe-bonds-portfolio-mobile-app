package com.ryzoft.bondportfolioapp.shared.domain.repository

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the operations for managing bond data.
 */
interface BondRepository {

    /**
     * Retrieves all bonds from the data source as a Flow.
     * The Flow will emit a new list whenever the underlying data changes.
     */
    fun getAllBonds(): Flow<List<Bond>>

    /**
     * Retrieves a single bond by its unique ID.
     * Returns null if no bond with the given ID is found.
     */
    suspend fun getBondById(id: Long): Bond?

    /**
     * Adds a new bond to the data source.
     * @param bond The bond object to add (ID should typically be ignored or 0).
     */
    suspend fun addBond(bond: Bond)

    /**
     * Updates an existing bond in the data source.
     * @param bond The bond object with updated details (ID must match an existing bond).
     */
    suspend fun updateBond(bond: Bond)

    /**
     * Deletes a bond from the data source by its unique ID.
     */
    suspend fun deleteBond(id: Long)
}
