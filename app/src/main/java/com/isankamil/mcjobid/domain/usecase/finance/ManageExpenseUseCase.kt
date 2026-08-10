package com.isankamil.mcjobid.domain.usecase.finance

import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ExpenseRepository
import com.isankamil.mcjobid.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val bookingRepository: BookingRepository
) {
    fun getAllExpenses(): Flow<List<Expense>> {
        return expenseRepository.getAllExpenses()
    }

    fun getExpensesByBooking(bookingId: String): Flow<List<Expense>> {
        return expenseRepository.getExpensesByBooking(bookingId)
    }

    suspend fun addExpense(
        bookingId: String,
        category: String,
        amount: Long,
        date: String = LocalDate.now().toString(),
        note: String? = null
    ): Result<Expense> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Nominal pengeluaran harus lebih besar dari Rp0."))
        }

        if (category.isBlank()) {
            return Result.failure(IllegalArgumentException("Pilih kategori pengeluaran."))
        }

        val booking = bookingRepository.getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Job tidak ditemukan."))

        val expenseId = "exp_${bookingId}_${System.currentTimeMillis()}"
        val expense = Expense(
            id = expenseId,
            bookingId = bookingId,
            category = category,
            amount = amount,
            date = date.ifBlank { LocalDate.now().toString() },
            note = note,
            createdAt = LocalDateTime.now()
        )

        return try {
            expenseRepository.addExpense(expense)
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expense: Expense): Result<Unit> {
        return try {
            expenseRepository.deleteExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpenseById(id: String): Result<Unit> {
        return try {
            expenseRepository.deleteExpenseById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
