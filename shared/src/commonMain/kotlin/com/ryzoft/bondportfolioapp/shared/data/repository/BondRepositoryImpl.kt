package com.ryzoft.bondportfolioapp.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ryzoft.bondportfolioapp.db.BondPortfolioDB
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.ryzoft.bondportfolioapp.db.Bonds as DbBond // Alias to avoid name clash

class BondRepositoryImpl(
    db: BondPortfolioDB
) : BondRepository {

    private val queries = db.bondQueries

    override fun getAllBonds(): Flow<List<Bond>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbBonds ->
                dbBonds.map { it.toDomainModel() }
            }
    }

    override suspend fun getBondById(id: Long): Bond? {
        return withContext(Dispatchers.Default) {
            queries.selectById(id).executeAsOneOrNull()?.toDomainModel()
        }
    }

    override suspend fun addBond(bond: Bond) {
        withContext(Dispatchers.Default) {
            queries.insertBond(
                name = bond.name,
                isin = bond.isin,
                cusip = bond.cusip,
                issuerName = bond.issuerName,
                bondType = bond.bondType,
                purchaseDate = bond.purchaseDate,
                maturityDate = bond.maturityDate,
                faceValuePerBond = bond.faceValuePerBond,
                purchasePrice = bond.purchasePrice,
                quantityPurchased = bond.quantityPurchased.toLong(),
                couponRate = bond.couponRate,
                paymentFrequency = bond.paymentFrequency,
                currency = bond.currency,
                notes = bond.notes
            )
        }
    }

    override suspend fun updateBond(bond: Bond) {
        withContext(Dispatchers.Default) {
            queries.updateBond(
                id = bond.id,
                name = bond.name,
                isin = bond.isin,
                cusip = bond.cusip,
                issuerName = bond.issuerName,
                bondType = bond.bondType,
                purchaseDate = bond.purchaseDate,
                maturityDate = bond.maturityDate,
                faceValuePerBond = bond.faceValuePerBond,
                purchasePrice = bond.purchasePrice,
                quantityPurchased = bond.quantityPurchased.toLong(),
                couponRate = bond.couponRate,
                paymentFrequency = bond.paymentFrequency,
                currency = bond.currency,
                notes = bond.notes
            )
        }
    }

    override suspend fun deleteBond(id: Long) {
        withContext(Dispatchers.Default) {
            queries.deleteBond(id)
        }
    }

    // --- Mapper Functions --- //

    private fun DbBond.toDomainModel(): Bond {
        return Bond(
            id = this.id,
            name = this.name,
            isin = this.isin,
            cusip = this.cusip,
            issuerName = this.issuerName,
            bondType = this.bondType, // Adapters handle the conversion
            purchaseDate = this.purchaseDate,
            maturityDate = this.maturityDate,
            faceValuePerBond = this.faceValuePerBond,
            purchasePrice = this.purchasePrice,
            quantityPurchased = this.quantityPurchased.toInt(), // Convert Long back to Int
            couponRate = this.couponRate,
            paymentFrequency = this.paymentFrequency,
            currency = this.currency,
            notes = this.notes
        )
    }
}
