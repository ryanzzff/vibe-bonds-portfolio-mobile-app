package com.ryzoft.bondportfolioapp.shared.domain.util

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

/**
 * Utility class for calculating interest payments for bonds.
 */
object InterestCalculator {

    /**
     * Calculates all future interest payments for a bond until maturity.
     *
     * @param bond The bond to calculate payments for
     * @return List of future interest payments sorted by date
     */
    fun calculateFuturePayments(bond: Bond): List<InterestPayment> {
        // Zero coupon bonds don't have periodic interest payments
        if (bond.paymentFrequency == PaymentFrequency.ZERO_COUPON) {
            return emptyList()
        }

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val paymentDates = calculatePaymentDates(bond, today)
        
        // Calculate payment amount (coupon payment per period)
        val paymentAmount = calculatePaymentAmount(bond)
        
        // Create InterestPayment objects for each date
        return paymentDates.map { date ->
            InterestPayment(
                bondId = bond.id,
                paymentDate = date,
                amount = paymentAmount
            )
        }
    }
    
    /**
     * Calculates payment dates from purchase date (or today if purchase date is in the past)
     * until maturity date.
     *
     * @param bond The bond
     * @param referenceDate The reference date (typically today's date)
     * @return List of payment dates sorted chronologically
     */
    private fun calculatePaymentDates(bond: Bond, referenceDate: LocalDate): List<LocalDate> {
        // If the payment frequency is ZERO_COUPON, return empty list
        if (bond.paymentFrequency == PaymentFrequency.ZERO_COUPON) {
            return emptyList()
        }
        
        // Determine the number of payments per year based on frequency
        val paymentsPerYear = when (bond.paymentFrequency) {
            PaymentFrequency.ANNUAL -> 1
            PaymentFrequency.SEMI_ANNUAL -> 2
            PaymentFrequency.QUARTERLY -> 4
            PaymentFrequency.MONTHLY -> 12
            PaymentFrequency.ZERO_COUPON -> 0 // Should never reach here due to check above
        }
        
        // Determine the increment in months between payments
        val monthsPerPayment = 12 / paymentsPerYear
        
        // Start from the later of purchase date or reference date (today)
        val startDate = if (bond.purchaseDate > referenceDate) bond.purchaseDate else referenceDate
        
        // Find the first payment date after start date
        // First find where in the payment cycle we are
        var firstPaymentDate = findNextPaymentDate(bond, startDate, monthsPerPayment)
        
        // Generate all payment dates until maturity
        val paymentDates = mutableListOf<LocalDate>()
        var currentDate = firstPaymentDate
        
        while (currentDate <= bond.maturityDate) {
            paymentDates.add(currentDate)
            currentDate = currentDate.plus(monthsPerPayment, DateTimeUnit.MONTH)
        }
        
        return paymentDates
    }
    
    /**
     * Finds the next payment date after the reference date.
     *
     * @param bond The bond
     * @param referenceDate The reference date
     * @param monthsPerPayment Number of months between payments
     * @return The next payment date
     */
    private fun findNextPaymentDate(bond: Bond, referenceDate: LocalDate, monthsPerPayment: Int): LocalDate {
        // Start by finding how many months from purchase date to reference date
        val purchaseYear = bond.purchaseDate.year
        val purchaseMonth = bond.purchaseDate.monthNumber
        
        val referenceYear = referenceDate.year
        val referenceMonth = referenceDate.monthNumber
        
        val monthDiff = (referenceYear - purchaseYear) * 12 + (referenceMonth - purchaseMonth)
        
        // How many complete payment periods have passed
        val completedPaymentPeriods = monthDiff / monthsPerPayment
        
        // Calculate next payment date
        val nextPaymentPeriod = completedPaymentPeriods + 1
        val monthsToAdd = nextPaymentPeriod * monthsPerPayment
        
        // Calculate next payment date from purchase date
        var nextPaymentDate = bond.purchaseDate.plus(monthsToAdd, DateTimeUnit.MONTH)
        
        // If the next payment date is still before the reference date, move one period forward
        if (nextPaymentDate <= referenceDate) {
            nextPaymentDate = nextPaymentDate.plus(monthsPerPayment, DateTimeUnit.MONTH)
        }
        
        return nextPaymentDate
    }
    
    /**
     * Calculates the amount of each interest payment.
     *
     * @param bond The bond
     * @return The payment amount
     */
    private fun calculatePaymentAmount(bond: Bond): Double {
        val paymentsPerYear = when (bond.paymentFrequency) {
            PaymentFrequency.ANNUAL -> 1
            PaymentFrequency.SEMI_ANNUAL -> 2
            PaymentFrequency.QUARTERLY -> 4
            PaymentFrequency.MONTHLY -> 12
            PaymentFrequency.ZERO_COUPON -> return 0.0 // Zero coupon bonds don't pay periodic interest
        }
        
        // Calculate payment amount using the formula:
        // face_value_per_bond * quantity_purchased * (coupon_rate / 100) / payments_per_year
        val amount = bond.faceValuePerBond * bond.quantityPurchased * (bond.couponRate / 100.0) / paymentsPerYear
        
        // Round to 2 decimal places to handle floating point imprecision
        return round(amount * 100) / 100
    }
} 