package com.isankamil.mcjobid

import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Booking.BookingStatus
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.domain.model.Payment
import com.isankamil.mcjobid.domain.usecase.finance.CalculateFinancialSummaryUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class CalculateFinancialSummaryUseCaseTest {

    private val useCase = CalculateFinancialSummaryUseCase()

    @Test
    fun testFinancialCalculationExcludesCancelledJobs() {
        val bookings = listOf(
            Booking(
                id = "1",
                name = "Wedding A",
                date = LocalDate.now(),
                fee = 5_000_000L,
                dp = 2_000_000L,
                status = BookingStatus.CONFIRMED
            ),
            Booking(
                id = "2",
                name = "Wedding B",
                date = LocalDate.now(),
                fee = 3_000_000L,
                dp = 0L,
                status = BookingStatus.CANCELLED
            )
        )

        val payments = listOf(
            Payment(
                id = "p1",
                bookingId = "1",
                amount = 2_000_000L,
                paymentDate = "2026-08-09",
                paymentMethod = "Transfer",
                createdAt = LocalDateTime.now()
            )
        )

        val expenses = listOf(
            Expense(
                id = "e1",
                bookingId = "1",
                amount = 500_000L,
                category = "Transport",
                date = "2026-08-09"
            )
        )

        val summary = useCase(bookings, payments, expenses)

        assertEquals(5_000_000L, summary.totalHonor)
        assertEquals(2_000_000L, summary.totalPaid)
        assertEquals(3_000_000L, summary.totalOutstanding)
        assertEquals(500_000L, summary.totalExpenses)
        assertEquals(4_500_000L, summary.netIncome)
    }
}
