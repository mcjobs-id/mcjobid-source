package com.isankamil.mcjobid.domain.usecase.finance

import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.domain.model.FinancialSummary
import com.isankamil.mcjobid.domain.model.Payment
import javax.inject.Inject

/**
 * Single Canonical Calculation Engine for financial figures across MCJOBID.
 * Guarantees zero divergence between Dashboard, Finance Screen, Invoice, and Client CRM.
 */
class CalculateFinancialSummaryUseCase @Inject constructor() {

    operator fun invoke(
        bookings: List<Booking>,
        payments: List<Payment>,
        expenses: List<Expense>
    ): FinancialSummary {
        val activeBookings = bookings.filter { it.status != Booking.BookingStatus.CANCELLED }

        val totalHonor = activeBookings.sumOf { it.fee }

        val paymentsByJob = payments.groupBy { it.bookingId }

        var totalPaid = 0L
        var totalOutstanding = 0L

        activeBookings.forEach { booking ->
            val jobPayments = paymentsByJob[booking.id]?.sumOf { it.amount } ?: booking.dp
            totalPaid += jobPayments
            val jobOutstanding = maxOf(0L, booking.fee - jobPayments)
            totalOutstanding += jobOutstanding
        }

        val totalExpense = expenses.sumOf { it.amount }
        val netIncome = totalHonor - totalExpense

        return FinancialSummary(
            totalHonor = totalHonor,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            totalExpenses = totalExpense,
            netIncome = netIncome
        )
    }
}
