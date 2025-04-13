package com.ryzoft.bondportfolioapp.shared.domain.util

import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
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
                bondName = bond.name ?: bond.issuerName, // Use bond name if available, fallback to issuer name
                paymentDate = date,
                amount = paymentAmount
            )
        }
    }
    
    /**
     * Calculates payment dates from reference date until maturity date.
     * Payment schedule is based on standard market payment cycles, not purchase date.
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
        
        // Find the first payment date after reference date
        var firstPaymentDate = findNextPaymentDate(bond.maturityDate, referenceDate, monthsPerPayment)
        
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
     * Uses standard bond market payment cycles, independent of purchase date.
     *
     * @param maturityDate The bond's maturity date
     * @param referenceDate The reference date
     * @param monthsPerPayment Number of months between payments
     * @return The next payment date
     */
    private fun findNextPaymentDate(maturityDate: LocalDate, referenceDate: LocalDate, monthsPerPayment: Int): LocalDate {
        // Standard bond market payment cycles are calculated backward from maturity date
        // For example, if a bond matures on June 15, 2030, and pays semi-annually,
        // payment dates would be June 15 and December 15 of each year
        
        val maturityMonth = maturityDate.monthNumber
        val maturityDay = maturityDate.dayOfMonth
        
        // Calculate how many months to go backward from the maturity date to find the payment cycle
        var currentMonth = maturityMonth
        var currentYear = maturityDate.year
        
        // Find the most recent payment date at or before the reference date
        while (LocalDate(currentYear, currentMonth, maturityDay) > referenceDate) {
            currentMonth -= monthsPerPayment
            while (currentMonth <= 0) {
                currentMonth += 12
                currentYear--
            }
        }
        
        // Find the next payment date after the reference date
        var nextPaymentMonth = currentMonth
        var nextPaymentYear = currentYear
        
        do {
            nextPaymentMonth += monthsPerPayment
            while (nextPaymentMonth > 12) {
                nextPaymentMonth -= 12
                nextPaymentYear++
            }
        } while (LocalDate(nextPaymentYear, nextPaymentMonth, maturityDay) <= referenceDate)
        
        return LocalDate(nextPaymentYear, nextPaymentMonth, maturityDay)
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
        // face_value_per_bond * quantity_purchased * coupon_rate / payments_per_year
        val amount = bond.faceValuePerBond * bond.quantityPurchased * bond.couponRate / paymentsPerYear
        
        // Round to 2 decimal places to handle floating point imprecision
        return round(amount * 100) / 100
    }
}