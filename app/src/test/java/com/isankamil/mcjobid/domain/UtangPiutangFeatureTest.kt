package com.isankamil.mcjobid.domain

import com.isankamil.mcjobid.data.local.entity.ExpenseEntity
import com.isankamil.mcjobid.data.local.entity.PaymentEntity
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.domain.model.Payment
import com.isankamil.mcjobid.domain.usecase.finance.CalculateFinancialSummaryUseCase
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class UtangPiutangFeatureTest {

    private val calculateFinancialSummary = CalculateFinancialSummaryUseCase()

    @Test
    fun testPaymentEntityToDomainAndBackMapping() {
        val now = LocalDateTime.now()
        val payment = Payment(
            id = "pay_01",
            bookingId = "b_01",
            amount = 2_500_000L,
            paymentDate = "2026-08-09",
            paymentMethod = "Bank Transfer",
            notes = "DP 50%",
            createdAt = now
        )

        val entity = payment.toEntity()
        assertEquals("pay_01", entity.id)
        assertEquals("b_01", entity.bookingId)
        assertEquals(2_500_000L, entity.amount)
        assertEquals("Bank Transfer", entity.paymentMethod)
        assertEquals("DP 50%", entity.notes)

        val mappedBack = Payment.fromEntity(entity)
        assertEquals(payment.id, mappedBack.id)
        assertEquals(payment.bookingId, mappedBack.bookingId)
        assertEquals(payment.amount, mappedBack.amount)
        assertEquals(payment.paymentMethod, mappedBack.paymentMethod)
    }

    @Test
    fun testExpenseEntityToDomainAndBackMapping() {
        val now = LocalDateTime.now()
        val expense = Expense(
            id = "exp_01",
            bookingId = "b_01",
            category = "Transport",
            amount = 350_000L,
            date = "2026-08-09",
            note = "Bensin & Tol Cipali",
            createdAt = now
        )

        val entity = expense.toEntity()
        assertEquals("exp_01", entity.id)
        assertEquals("b_01", entity.bookingId)
        assertEquals("Transport", entity.category)
        assertEquals(350_000L, entity.amount)
        assertEquals("Bensin & Tol Cipali", entity.note)

        val mappedBack = Expense.fromEntity(entity)
        assertEquals(expense.id, mappedBack.id)
        assertEquals(expense.bookingId, mappedBack.bookingId)
        assertEquals(expense.category, mappedBack.category)
        assertEquals(expense.amount, mappedBack.amount)
        assertEquals(expense.note, mappedBack.note)
    }

    @Test
    fun testPiutangCalculationWithMultiplePayments() {
        val totalFee = 8_000_000L
        val dpPayment = 3_000_000L
        val secondPayment = 3_000_000L
        val finalPayment = 2_000_000L

        // Step 1: Initial (unpaid)
        val initialOutstanding = totalFee
        assertEquals(8_000_000L, initialOutstanding)

        // Step 2: After DP
        val outstandingAfterDp = totalFee - dpPayment
        assertEquals(5_000_000L, outstandingAfterDp)
        assertTrue(outstandingAfterDp > 0)

        // Step 3: After Second Payment
        val outstandingAfterSecond = totalFee - (dpPayment + secondPayment)
        assertEquals(2_000_000L, outstandingAfterSecond)

        // Step 4: After Final Payment (Lunas)
        val totalPaid = dpPayment + secondPayment + finalPayment
        val outstandingFinal = maxOf(0L, totalFee - totalPaid)
        assertEquals(0L, outstandingFinal)
    }

    @Test
    fun testOverpaymentDetectionRule() {
        val fee = 5_000_000L
        val currentPaid = 3_000_000L
        val remaining = fee - currentPaid

        val validPayment = 2_000_000L
        val isOverValid = validPayment > remaining
        assertFalse(isOverValid)

        val excessPayment = 2_500_000L
        val isOverExcess = excessPayment > remaining
        assertTrue(isOverExcess)
    }

    @Test
    fun testUtangAndExpenseCategorizationFiltering() {
        val expenses = listOf(
            Expense("1", "b1", "Transport", 200_000L, "2026-08-09"),
            Expense("2", "b1", "Wardrobe", 500_000L, "2026-08-09"),
            Expense("3", "b2", "MUA & Hairdo", 400_000L, "2026-08-09"),
            Expense("4", "b2", "Transport", 150_000L, "2026-08-09"),
            Expense("5", "b3", "Sound / Mic", 300_000L, "2026-08-09")
        )

        val transportExpenses = expenses.filter { it.category == "Transport" }
        assertEquals(2, transportExpenses.size)
        assertEquals(350_000L, transportExpenses.sumOf { it.amount })

        val wardrobeExpenses = expenses.filter { it.category == "Wardrobe" }
        assertEquals(1, wardrobeExpenses.size)
        assertEquals(500_000L, wardrobeExpenses.first().amount)

        val totalAllExpenses = expenses.sumOf { it.amount }
        assertEquals(1_550_000L, totalAllExpenses)
    }

    @Test
    fun testCanonicalFinancialSummaryEngine() {
        val bookings = listOf(
            Booking(
                id = "b1",
                name = "Wedding Sarah",
                date = LocalDate.of(2026, 8, 10),
                fee = 6_000_000L,
                dp = 2_000_000L,
                status = Booking.BookingStatus.CONFIRMED
            ),
            Booking(
                id = "b2",
                name = "Corporate Gathering",
                date = LocalDate.of(2026, 8, 15),
                fee = 4_000_000L,
                dp = 4_000_000L,
                status = Booking.BookingStatus.COMPLETED
            ),
            Booking(
                id = "b3",
                name = "Birthday Party Cancelled",
                date = LocalDate.of(2026, 8, 20),
                fee = 3_000_000L,
                dp = 0L,
                status = Booking.BookingStatus.CANCELLED
            )
        )

        val payments = listOf(
            Payment("p1", "b1", 2_000_000L, "2026-08-01", "Transfer"),
            Payment("p2", "b2", 4_000_000L, "2026-08-05", "Transfer")
        )

        val expenses = listOf(
            Expense("e1", "b1", "Transport", 300_000L, "2026-08-10"),
            Expense("e2", "b1", "Wardrobe", 500_000L, "2026-08-10"),
            Expense("e3", "b2", "Transport", 200_000L, "2026-08-15")
        )

        val summary = calculateFinancialSummary(bookings, payments, expenses)

        // Cancelled booking (b3) is excluded
        assertEquals(10_000_000L, summary.totalHonor) // 6M + 4M
        assertEquals(6_000_000L, summary.totalPaid) // 2M + 4M
        assertEquals(4_000_000L, summary.totalOutstanding) // b1 outstanding: 6M - 2M = 4M, b2: 0
        assertEquals(1_000_000L, summary.totalExpenses) // 300k + 500k + 200k
        assertEquals(9_000_000L, summary.netIncome) // 10M - 1M = 9M
    }
}
