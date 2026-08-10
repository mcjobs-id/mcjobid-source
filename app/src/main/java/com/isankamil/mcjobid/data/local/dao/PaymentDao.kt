package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC, createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE ownerId = :ownerId ORDER BY paymentDate DESC, createdAt DESC")
    fun getPaymentsByOwnerId(ownerId: String): Flow<List<PaymentEntity>>

    @Query("DELETE FROM payments WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("SELECT * FROM payments WHERE bookingId = :bookingId ORDER BY paymentDate DESC")
    fun getPaymentsByBooking(bookingId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE bookingId = :bookingId")
    suspend fun getPaymentsListByBooking(bookingId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: String): PaymentEntity?

    @Query("SELECT SUM(amount) FROM payments WHERE bookingId = :bookingId")
    suspend fun getTotalPaidForBooking(bookingId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: String)

    @Query("DELETE FROM payments WHERE bookingId = :bookingId")
    suspend fun deletePaymentsByBooking(bookingId: String)

    @Query("DELETE FROM payments WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deletePaymentsNotInIds(ownerId: String, ids: List<String>)
}
