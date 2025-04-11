package com.ryzoft.bondportfolioapp.shared.domain.util

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.pow

/**
 * Utility class for calculating various bond yield metrics.
 */
object YieldCalculator {

    /**
     * Calculate the weighted average coupon rate of a portfolio of bonds.
     * Weighted by face value - weights each bond's coupon rate by its total face value.
     *
     * @param bonds The list of bonds in the portfolio
     * @return The weighted average coupon rate as a percentage (e.g., 4.5 for 4.5%)
     */
    fun calculateAverageCouponRate(bonds: List<Bond>): Double {
        if (bonds.isEmpty()) return 0.0

        var totalFaceValue = 0.0
        var weightedCouponSum = 0.0

        bonds.forEach { bond ->
            val bondFaceValue = bond.faceValuePerBond * bond.quantityPurchased
            totalFaceValue += bondFaceValue
            weightedCouponSum += bond.couponRate * bondFaceValue
        }

        return if (totalFaceValue > 0) weightedCouponSum / totalFaceValue else 0.0
    }

    /**
     * Calculate the weighted average current yield of a portfolio of bonds.
     * Current yield = (Annual Interest) / (Current Market Price)
     * For simplicity, we use the purchase price as the current price.
     * Weighted by total investment amount.
     *
     * @param bonds The list of bonds in the portfolio
     * @return The weighted average current yield as a percentage (e.g., 4.2 for 4.2%)
     */
    fun calculateAverageCurrentYield(bonds: List<Bond>): Double {
        if (bonds.isEmpty()) return 0.0

        var totalInvestment = 0.0
        var weightedYieldSum = 0.0

        bonds.forEach { bond ->
            val currentYield = calculateCurrentYield(bond)
            val investment = bond.purchasePrice * bond.quantityPurchased
            totalInvestment += investment
            weightedYieldSum += currentYield * investment
        }

        return if (totalInvestment > 0) weightedYieldSum / totalInvestment else 0.0
    }

    /**
     * Calculate the current yield of a single bond.
     * Current yield = (Annual Interest) / (Current Price)
     *
     * @param bond The bond to calculate yield for
     * @return The current yield as a percentage
     */
    fun calculateCurrentYield(bond: Bond): Double {
        if (bond.purchasePrice <= 0) return 0.0

        // Annual interest = face value * coupon rate
        val annualInterest = bond.faceValuePerBond * (bond.couponRate / 100.0)
        
        // Current yield = annual interest / purchase price
        return (annualInterest / bond.purchasePrice) * 100.0
    }

    /**
     * Calculate the weighted average yield to maturity (YTM) of a portfolio of bonds.
     * Weighted by total investment amount.
     *
     * @param bonds The list of bonds in the portfolio
     * @return The weighted average YTM as a percentage (e.g., 5.1 for 5.1%)
     */
    fun calculateAverageYTM(bonds: List<Bond>): Double {
        if (bonds.isEmpty()) return 0.0

        var totalInvestment = 0.0
        var weightedYTMSum = 0.0

        bonds.forEach { bond ->
            val ytm = calculateYTM(bond)
            val investment = bond.purchasePrice * bond.quantityPurchased
            totalInvestment += investment
            weightedYTMSum += ytm * investment
        }

        return if (totalInvestment > 0) weightedYTMSum / totalInvestment else 0.0
    }

    /**
     * Calculate the yield to maturity (YTM) of a single bond using an iterative approximation.
     * Uses a numerical method (bisection) to find the discount rate that makes the present value
     * of all future cash flows equal to the purchase price.
     *
     * @param bond The bond to calculate YTM for
     * @return The YTM as a percentage
     */
    fun calculateYTM(bond: Bond): Double {
        // Get today's date
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Check if the bond has already matured
        if (bond.maturityDate <= today) return 0.0
        
        // Calculate time to maturity in years
        val yearsToMaturity = calculateYearsToMaturity(today, bond.maturityDate)
        if (yearsToMaturity <= 0) return 0.0
        
        // For zero coupon bonds, we can calculate YTM directly
        if (bond.couponRate == 0.0) {
            // YTM = (FV/PV)^(1/n) - 1 where n is years to maturity
            val ytm = (bond.faceValuePerBond / bond.purchasePrice).pow(1.0 / yearsToMaturity) - 1.0
            return ytm * 100.0
        }
        
        // For coupon-paying bonds, use an iterative approach (bisection method)
        return bisectionYTM(bond, yearsToMaturity) * 100.0
    }
    
    /**
     * Calculate years to maturity, accounting for partial years.
     */
    private fun calculateYearsToMaturity(today: LocalDate, maturityDate: LocalDate): Double {
        // Calculate the period between today and maturity
        val period = today.periodUntil(maturityDate)
        
        // Convert to years with months as a fraction
        return period.years + period.months / 12.0 + period.days / 365.0
    }
    
    /**
     * Use bisection method to solve for YTM.
     * @return YTM as a decimal (not percentage)
     */
    private fun bisectionYTM(bond: Bond, yearsToMaturity: Double): Double {
        // Initial guesses for YTM
        var lowerBound = 0.0001 // 0.01%
        var upperBound = 1.0    // 100%
        
        // Bond parameters
        val faceValue = bond.faceValuePerBond
        val purchasePrice = bond.purchasePrice
        val couponRate = bond.couponRate / 100.0 // Convert to decimal
        val couponPayment = faceValue * couponRate
        
        // Payments per year - simplification: use 1 for annual, 2 for semi-annual
        val paymentsPerYear = when(bond.paymentFrequency) {
            com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency.ANNUAL -> 1
            com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency.SEMI_ANNUAL -> 2
            com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency.QUARTERLY -> 4
            com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency.MONTHLY -> 12
            com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency.ZERO_COUPON -> 1 // Shouldn't reach here due to check above
        }
        
        // Coupon payment per period
        val periodCouponPayment = couponPayment / paymentsPerYear
        
        // Total number of periods
        val totalPeriods = yearsToMaturity * paymentsPerYear
        
        // Bisection method to find YTM
        val epsilon = 0.0001 // Convergence criterion
        var midYTM: Double
        
        // Maximum iterations to prevent infinite loops
        val maxIterations = 100
        var iteration = 0
        
        while ((upperBound - lowerBound) > epsilon && iteration < maxIterations) {
            midYTM = (lowerBound + upperBound) / 2.0
            
            // Calculate net present value at midYTM
            val npv = calculateNPV(periodCouponPayment, faceValue, purchasePrice, midYTM / paymentsPerYear, totalPeriods)
            
            if (npv > 0) {
                // If NPV is positive, YTM is too low
                lowerBound = midYTM
            } else {
                // If NPV is negative, YTM is too high
                upperBound = midYTM
            }
            
            iteration++
        }
        
        return (lowerBound + upperBound) / 2.0
    }
    
    /**
     * Calculate Net Present Value (NPV) for a given YTM.
     */
    private fun calculateNPV(couponPerPeriod: Double, faceValue: Double, price: Double, ytmPerPeriod: Double, periods: Double): Double {
        var npv = -price // Initial cash flow (negative as it's an outflow)
        
        // Add present value of each coupon payment
        for (i in 1..periods.toInt()) {
            npv += couponPerPeriod / (1 + ytmPerPeriod).pow(i)
        }
        
        // Add present value of face value at maturity
        npv += faceValue / (1 + ytmPerPeriod).pow(periods)
        
        return npv
    }
} 