package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE ownerId = :ownerId ORDER BY date DESC, createdAt DESC")
    fun getExpensesByOwnerId(ownerId: String): Flow<List<ExpenseEntity>>

    @Query("DELETE FROM expenses WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("SELECT * FROM expenses WHERE bookingId = :bookingId ORDER BY date DESC")
    fun getExpensesByBooking(bookingId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("SELECT SUM(amount) FROM expenses WHERE bookingId = :bookingId")
    suspend fun getTotalExpensesForBooking(bookingId: String): Long?

    @Query("SELECT SUM(amount) FROM expenses WHERE strftime('%Y-%m', date) = :yearMonth")
    suspend fun getTotalExpensesByMonth(yearMonth: String): Long?

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalExpenses(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)

    @Query("DELETE FROM expenses WHERE bookingId = :bookingId")
    suspend fun deleteExpensesByBooking(bookingId: String)

    @Query("DELETE FROM expenses WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteExpensesNotInIds(ownerId: String, ids: List<String>)
}
