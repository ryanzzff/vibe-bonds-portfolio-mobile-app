package com.ryzoft.bondportfolioapp.shared.domain.usecase

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.YieldType
import com.ryzoft.bondportfolioapp.shared.domain.repository.BondRepository
import com.ryzoft.bondportfolioapp.shared.domain.util.YieldCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for calculating the average yield of a bond portfolio based on the specified yield type.
 */
class CalculateAverageYieldUseCase(private val repository: BondRepository) {

    /**
     * Executes the use case.
     * @param type The type of yield to calculate (coupon rate, current yield, or YTM)
     * @return A Flow emitting the calculated average yield as a percentage
     */
    operator fun invoke(type: YieldType): Flow<Double> {
        return repository.getAllBonds()
            .map { bonds ->
                if (bonds.isEmpty()) return@map 0.0

                when (type) {
                    YieldType.COUPON_RATE -> YieldCalculator.calculateAverageCouponRate(bonds)
                    YieldType.CURRENT_YIELD -> YieldCalculator.calculateAverageCurrentYield(bonds)
                    YieldType.YIELD_TO_MATURITY -> YieldCalculator.calculateAverageYTM(bonds)
                }
            }
    }
    
    /**
     * Calculate all yield metrics at once.
     * @return A Flow emitting a map with all yield metrics
     */
    fun calculateAllYields(): Flow<Map<YieldType, Double>> {
        return repository.getAllBonds()
            .map { bonds ->
                if (bonds.isEmpty()) {
                    return@map YieldType.values().associateWith { 0.0 }
                }
                
                mapOf(
                    YieldType.COUPON_RATE to YieldCalculator.calculateAverageCouponRate(bonds),
                    YieldType.CURRENT_YIELD to YieldCalculator.calculateAverageCurrentYield(bonds),
                    YieldType.YIELD_TO_MATURITY to YieldCalculator.calculateAverageYTM(bonds)
                )
            }
    }
} 