package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getInvoicesByOwnerId(ownerId: String): Flow<List<InvoiceEntity>>

    @Query("DELETE FROM invoices WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getInvoiceByBookingId(bookingId: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    fun getInvoiceByBookingIdFlow(bookingId: String): Flow<InvoiceEntity?>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int

    @Query("SELECT COUNT(*) FROM invoices WHERE ownerId = :ownerId")
    suspend fun getInvoiceCountByOwner(ownerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)

    @Query("DELETE FROM invoices WHERE bookingId = :bookingId")
    suspend fun deleteInvoiceByBookingId(bookingId: String)

    @Query("DELETE FROM invoices WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteInvoicesNotInIds(ownerId: String, ids: List<String>)
}
