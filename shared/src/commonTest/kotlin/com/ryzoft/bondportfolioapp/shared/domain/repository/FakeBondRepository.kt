package com.ryzoft.bondportfolioapp.shared.domain.repository

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake implementation of BondRepository for testing purposes.
 * Uses an in-memory list to simulate database storage.
 */
class FakeBondRepository : BondRepository {

    private val bondsFlow = MutableStateFlow<List<Bond>>(emptyList())
    private var nextId = 1L // Simple ID generation for testing

    override suspend fun addBond(bond: Bond) {
        bondsFlow.update { currentList ->
            // Assign a new ID if it's 0 (or handle potential duplicates if needed)
            val bondWithId = if (bond.id == 0L) bond.copy(id = nextId++) else bond
            // Add or replace bond if ID already exists (simulates upsert behavior)
            val existingIndex = currentList.indexOfFirst { it.id == bondWithId.id }
            if (existingIndex != -1) {
                currentList.toMutableList().apply { set(existingIndex, bondWithId) }
            } else {
                currentList + bondWithId
            }
        }
    }

    override suspend fun updateBond(bond: Bond) {
        bondsFlow.update { currentList ->
            val index = currentList.indexOfFirst { it.id == bond.id }
            if (index != -1) {
                currentList.toMutableList().apply { set(index, bond) }
            } else {
                // Optionally handle error: Cannot update non-existent bond
                currentList // Or throw an exception
            }
        }
    }

    override suspend fun deleteBond(bondId: Long) {
        bondsFlow.update { currentList ->
            currentList.filterNot { it.id == bondId }
        }
    }

    override fun getAllBonds(): Flow<List<Bond>> {
        return bondsFlow
    }

    override suspend fun getBondById(id: Long): Bond? {
        return bondsFlow.value.find { it.id == id }
    }

    // Helper function for tests to directly manipulate the state if needed
    fun insertBonds(bonds: List<Bond>) {
         bondsFlow.value = bonds
         // Update nextId based on the max ID inserted
         nextId = (bonds.maxOfOrNull { it.id } ?: 0L) + 1L
    }

     // Helper function to clear bonds
     fun clearBonds() {
         bondsFlow.value = emptyList()
         nextId = 1L
     }
}
