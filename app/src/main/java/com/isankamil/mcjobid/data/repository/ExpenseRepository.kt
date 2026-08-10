package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.dao.ExpenseDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth
) {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getAllExpenses(): Flow<List<Expense>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            expenseDao.getExpensesByOwnerId(uid).map { list ->
                list.map { Expense.fromEntity(it) }
            }
        } else {
            expenseDao.getAllExpenses().map { list ->
                list.map { Expense.fromEntity(it) }
            }
        }
    }

    fun getExpensesByBooking(bookingId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByBooking(bookingId).map { list ->
            list.map { Expense.fromEntity(it) }
        }
    }

    suspend fun getExpenseById(id: String): Expense? {
        return expenseDao.getExpenseById(id)?.let { Expense.fromEntity(it) }
    }

    suspend fun getTotalExpensesForBooking(bookingId: String): Long {
        return expenseDao.getTotalExpensesForBooking(bookingId) ?: 0L
    }

    suspend fun getTotalExpensesByMonth(yearMonth: String): Long {
        return expenseDao.getTotalExpensesByMonth(yearMonth) ?: 0L
    }

    suspend fun getTotalExpenses(): Long {
        return expenseDao.getTotalExpenses() ?: 0L
    }

    suspend fun addExpense(expense: Expense) {
        val entity = expense.toEntity().copy(ownerId = getCurrentUserId())
        expenseDao.insertExpense(entity)
        firestoreSyncService.saveExpenseToFirestore(entity)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
        firestoreSyncService.deleteExpenseFromFirestore(expense.id)
    }

    suspend fun deleteExpenseById(id: String) {
        expenseDao.deleteExpenseById(id)
        firestoreSyncService.deleteExpenseFromFirestore(id)
    }
}
