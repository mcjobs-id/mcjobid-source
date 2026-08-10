package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.McJobIdDatabase
import com.isankamil.mcjobid.data.local.dao.BookingDao
import com.isankamil.mcjobid.data.local.dao.InvoiceDao
import com.isankamil.mcjobid.data.local.dao.PaymentDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val database: McJobIdDatabase,
    private val paymentDao: PaymentDao,
    private val bookingDao: BookingDao,
    private val invoiceDao: InvoiceDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth
) {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getAllPayments(): Flow<List<Payment>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            paymentDao.getPaymentsByOwnerId(uid).map { entities ->
                entities.map { Payment.fromEntity(it) }
            }
        } else {
            paymentDao.getAllPayments().map { entities ->
                entities.map { Payment.fromEntity(it) }
            }
        }
    }

    fun getPaymentsByBooking(bookingId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByBooking(bookingId).map { entities ->
            entities.map { Payment.fromEntity(it) }
        }
    }

    suspend fun getPaymentById(id: String): Payment? {
        return paymentDao.getPaymentById(id)?.let { Payment.fromEntity(it) }
    }

    suspend fun addPayment(payment: Payment) {
        val uid = getCurrentUserId()
        val entity = payment.toEntity().copy(ownerId = uid)
        
        // Use Room transaction to ensure atomicity of payment + booking.dp update
        database.withTransaction {
            val booking = bookingDao.getBookingById(payment.bookingId)
            
            // Seed initial DP payment if booking.dp > 0 but payments table was empty or missing initial DP
            if (booking != null && booking.dp > 0) {
                val existingPayments = paymentDao.getPaymentsListByBooking(payment.bookingId)
                val sumExisting = existingPayments.sumOf { it.amount }
                if (sumExisting < booking.dp && existingPayments.none { it.id.startsWith("pay_init_") }) {
                    val initPayment = com.isankamil.mcjobid.data.local.entity.PaymentEntity(
                        id = "pay_init_${booking.id}",
                        ownerId = uid,
                        bookingId = booking.id,
                        amount = booking.dp,
                        paymentDate = booking.date,
                        paymentMethod = "Bank Transfer",
                        notes = "Pembayaran DP Awal",
                        createdAt = booking.createdAt
                    )
                    paymentDao.insertPayment(initPayment)
                    try { firestoreSyncService.savePaymentToFirestore(initPayment) } catch (_: Exception) {}
                }
            }

            paymentDao.insertPayment(entity)

            // Recalculate total paid DP for booking
            if (booking != null) {
                val totalPaid = paymentDao.getTotalPaidForBooking(payment.bookingId) ?: 0L
                val isFullyPaid = totalPaid >= booking.fee && booking.fee > 0
                val targetStatus = if (isFullyPaid) "completed" else booking.status
                val updatedBooking = booking.copy(
                    ownerId = uid,
                    dp = totalPaid,
                    status = targetStatus,
                    updatedAt = LocalDateTime.now().toString()
                )
                bookingDao.updateBooking(updatedBooking)

                // Jika sudah LUNAS (fee - totalPaid <= 0), otomatis dismiss & hapus reminder pelunasan
                if (isFullyPaid || (booking.fee - totalPaid) <= 0) {
                    val reminderDao = database.reminderDao()
                    val paymentReminders = reminderDao.getRemindersListByBooking(payment.bookingId)
                        .filter { it.reminderType == "PAYMENT" || it.reminderType == "PAYMENT_OVERDUE" }
                    reminderDao.dismissPaymentRemindersByBooking(payment.bookingId)
                    reminderDao.deletePaymentRemindersByBooking(payment.bookingId)
                    paymentReminders.forEach { r ->
                        try { firestoreSyncService.deleteReminderFromFirestore(r.id) } catch (_: Exception) {}
                    }
                }

                // Sync Invoice: update dpAmount, remainingAmount, and status
                val invoice = invoiceDao.getInvoiceByBookingId(payment.bookingId)
                if (invoice != null) {
                    val newRemaining = maxOf(0L, invoice.totalAmount - totalPaid)
                    val newStatus = when {
                        totalPaid >= invoice.totalAmount -> "PAID"
                        totalPaid > 0 -> "PARTIAL"
                        else -> invoice.status
                    }
                    val updatedInvoice = invoice.copy(
                        dpAmount = totalPaid,
                        remainingAmount = newRemaining,
                        status = newStatus
                    )
                    invoiceDao.updateInvoice(updatedInvoice)
                    try {
                        firestoreSyncService.saveInvoiceToFirestore(updatedInvoice)
                    } catch (_: Exception) {}
                }
            }
        }

        // Sync to Firestore (fire-and-forget; Room already has the truth)
        try {
            firestoreSyncService.savePaymentToFirestore(entity)
            val booking = bookingDao.getBookingById(payment.bookingId)
            if (booking != null) {
                firestoreSyncService.saveBookingToFirestore(booking)
            }
        } catch (_: Exception) {}
    }

    suspend fun deletePayment(payment: Payment) {
        val uid = getCurrentUserId()
        
        database.withTransaction {
            paymentDao.deletePayment(payment.toEntity())

            val booking = bookingDao.getBookingById(payment.bookingId)
            if (booking != null) {
                val totalPaid = paymentDao.getTotalPaidForBooking(payment.bookingId) ?: 0L
                val updatedBooking = booking.copy(
                    ownerId = uid,
                    dp = totalPaid,
                    updatedAt = LocalDateTime.now().toString()
                )
                bookingDao.updateBooking(updatedBooking)

                // Sync Invoice after payment removal
                val invoice = invoiceDao.getInvoiceByBookingId(payment.bookingId)
                if (invoice != null) {
                    val newRemaining = maxOf(0L, invoice.totalAmount - totalPaid)
                    val newStatus = when {
                        totalPaid >= invoice.totalAmount -> "PAID"
                        totalPaid > 0 -> "PARTIAL"
                        else -> "DRAFT"
                    }
                    val updatedInvoice = invoice.copy(
                        dpAmount = totalPaid,
                        remainingAmount = newRemaining,
                        status = newStatus
                    )
                    invoiceDao.updateInvoice(updatedInvoice)
                    try { firestoreSyncService.saveInvoiceToFirestore(updatedInvoice) } catch (_: Exception) {}
                }
            }
        }

        try {
            firestoreSyncService.deletePaymentFromFirestore(payment.id)
            val booking = bookingDao.getBookingById(payment.bookingId)
            if (booking != null) {
                firestoreSyncService.saveBookingToFirestore(booking)
            }
        } catch (_: Exception) {}
    }

    suspend fun deletePaymentById(paymentId: String, bookingId: String) {
        val uid = getCurrentUserId()

        database.withTransaction {
            paymentDao.deletePaymentById(paymentId)

            val booking = bookingDao.getBookingById(bookingId)
            if (booking != null) {
                val totalPaid = paymentDao.getTotalPaidForBooking(bookingId) ?: 0L
                val updatedBooking = booking.copy(
                    ownerId = uid,
                    dp = totalPaid,
                    updatedAt = LocalDateTime.now().toString()
                )
                bookingDao.updateBooking(updatedBooking)

                // Sync Invoice after payment removal
                val invoice = invoiceDao.getInvoiceByBookingId(bookingId)
                if (invoice != null) {
                    val newRemaining = maxOf(0L, invoice.totalAmount - totalPaid)
                    val newStatus = when {
                        totalPaid >= invoice.totalAmount -> "PAID"
                        totalPaid > 0 -> "PARTIAL"
                        else -> "DRAFT"
                    }
                    val updatedInvoice = invoice.copy(
                        dpAmount = totalPaid,
                        remainingAmount = newRemaining,
                        status = newStatus
                    )
                    invoiceDao.updateInvoice(updatedInvoice)
                    try { firestoreSyncService.saveInvoiceToFirestore(updatedInvoice) } catch (_: Exception) {}
                }
            }
        }

        try {
            firestoreSyncService.deletePaymentFromFirestore(paymentId)
            val booking = bookingDao.getBookingById(bookingId)
            if (booking != null) {
                firestoreSyncService.saveBookingToFirestore(booking)
            }
        } catch (_: Exception) {}
    }
}
