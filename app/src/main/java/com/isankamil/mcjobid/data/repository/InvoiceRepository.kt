package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.dao.InvoiceDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Invoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth
) {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getAllInvoices(): Flow<List<Invoice>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            invoiceDao.getInvoicesByOwnerId(uid).map { entities ->
                entities.map { Invoice.fromEntity(it) }
            }
        } else {
            invoiceDao.getAllInvoices().map { entities ->
                entities.map { Invoice.fromEntity(it) }
            }
        }
    }

    fun getInvoiceByBookingIdFlow(bookingId: String): Flow<Invoice?> {
        return invoiceDao.getInvoiceByBookingIdFlow(bookingId).map { entity ->
            entity?.let { Invoice.fromEntity(it) }
        }
    }

    suspend fun getInvoiceById(id: String): Invoice? {
        return invoiceDao.getInvoiceById(id)?.let { Invoice.fromEntity(it) }
    }

    suspend fun getInvoiceByBookingId(bookingId: String): Invoice? {
        return invoiceDao.getInvoiceByBookingId(bookingId)?.let { Invoice.fromEntity(it) }
    }

    suspend fun generateNextInvoiceNumber(): String {
        val uid = getCurrentUserId()
        val count = if (uid.isNotBlank()) {
            invoiceDao.getInvoiceCountByOwner(uid) + 1
        } else {
            invoiceDao.getInvoiceCount() + 1
        }
        val currentYear = java.time.LocalDate.now().year
        return "INV-$currentYear-${count.toString().padStart(4, '0')}"
    }

    suspend fun saveInvoice(invoice: Invoice) {
        val entity = invoice.toEntity().copy(ownerId = getCurrentUserId())
        invoiceDao.insertInvoice(entity)
        firestoreSyncService.saveInvoiceToFirestore(entity)
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoice(invoice.toEntity())
        firestoreSyncService.deleteInvoiceFromFirestore(invoice.id)
    }

    suspend fun deleteInvoiceById(id: String) {
        invoiceDao.deleteInvoiceById(id)
        firestoreSyncService.deleteInvoiceFromFirestore(id)
    }
}
