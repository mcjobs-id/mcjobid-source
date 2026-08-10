package com.isankamil.mcjobid.domain.usecase.invoice

import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.InvoiceRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Invoice
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.util.Formatter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val bookingRepository: BookingRepository
) {
    fun getAllInvoices(): Flow<List<Invoice>> {
        return invoiceRepository.getAllInvoices()
    }

    fun getInvoiceByBookingId(bookingId: String): Flow<Invoice?> {
        return invoiceRepository.getInvoiceByBookingIdFlow(bookingId)
    }

    suspend fun generateInvoiceNumber(): String {
        return invoiceRepository.generateNextInvoiceNumber()
    }

    suspend fun createOrUpdateInvoice(
        bookingId: String,
        invoiceNumber: String? = null,
        issueDate: String = LocalDate.now().toString(),
        dueDate: String? = null,
        notes: String? = null
    ): Result<Invoice> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("Pilih job terlebih dahulu."))
        }

        val booking = bookingRepository.getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Job dengan ID $bookingId tidak ditemukan."))

        val number = if (!invoiceNumber.isNullOrBlank()) {
            invoiceNumber
        } else {
            invoiceRepository.generateNextInvoiceNumber()
        }

        val resolvedDueDate = dueDate?.takeIf { it.isNotBlank() } ?: booking.date.toString()

        val status = when {
            booking.outstanding <= 0L -> Invoice.InvoiceStatus.PAID
            booking.dp > 0L -> Invoice.InvoiceStatus.PARTIALLY_PAID
            else -> Invoice.InvoiceStatus.SENT
        }

        // Check if an invoice already exists for this booking to preserve ID or create a new one
        val existing = invoiceRepository.getInvoiceByBookingId(bookingId)
        val invoiceId = existing?.id ?: "inv_${bookingId}_${System.currentTimeMillis()}"

        val invoice = Invoice(
            id = invoiceId,
            invoiceNumber = number,
            bookingId = bookingId,
            issueDate = issueDate,
            dueDate = resolvedDueDate,
            status = status,
            totalAmount = booking.fee,
            dpAmount = booking.dp,
            remainingAmount = booking.outstanding,
            notes = notes ?: booking.note,
            createdAt = existing?.createdAt ?: LocalDateTime.now()
        )

        return try {
            invoiceRepository.saveInvoice(invoice)
            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInvoice(invoice: Invoice): Result<Unit> {
        return try {
            invoiceRepository.deleteInvoice(invoice)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInvoiceById(invoiceId: String): Result<Unit> {
        return try {
            invoiceRepository.deleteInvoiceById(invoiceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateShareableSummary(
        invoice: Invoice,
        booking: Booking,
        userProfile: UserProfile?
    ): String {
        val mcName = userProfile?.name ?: "MC Professional"
        val client = booking.client ?: "Personal Client"
        val dateFormatted = try {
            Formatter.formatDate(booking.date)
        } catch (e: Exception) {
            booking.date.toString()
        }

        val statusText = when (invoice.status) {
            Invoice.InvoiceStatus.PAID -> "LUNAS"
            Invoice.InvoiceStatus.PARTIALLY_PAID -> "DP TERBAYAR"
            Invoice.InvoiceStatus.SENT -> "BELUM BAYAR"
            Invoice.InvoiceStatus.DRAFT -> "DRAFT"
            Invoice.InvoiceStatus.CANCELLED -> "DIBATALKAN"
        }

        val bankSection = if (userProfile != null && !userProfile.bankName.isNullOrBlank()) {
            """
            |💳 *PEMBAYARAN DITRANSFER KE:*
            |Bank: ${userProfile.bankName}
            |No. Rekening: ${userProfile.accountNumber ?: "-"}
            |Atas Nama: ${userProfile.accountName ?: userProfile.name}
            """.trimMargin()
        } else {
            "|💳 *PEMBAYARAN:* Silakan hubungi $mcName untuk rekening transfer."
        }

        return """
            |📄 *INVOICE RESMI MC - mcjob.id*
            |No. Invoice: ${invoice.invoiceNumber}
            |Tanggal Terbit: ${invoice.issueDate}
            |Jatuh Tempo: ${invoice.dueDate}
            |Status: [$statusText]
            |
            |👤 *KLIEN / ACARA:*
            |Klien: $client
            |Acara: ${booking.name}
            |Tanggal: $dateFormatted
            |${if (!booking.location.isNullOrBlank()) "Lokasi: ${booking.location}\n" else ""}
            |💰 *RINCIAN BIAYA:*
            |Total Jasa MC: ${Formatter.formatCurrency(invoice.totalAmount)}
            |${if (invoice.dpAmount > 0) "DP Terbayar: ${Formatter.formatCurrency(invoice.dpAmount)}\n" else ""}Sisa Tagihan: *${Formatter.formatCurrency(invoice.remainingAmount)}*
            |
            $bankSection
            |
            |${if (!invoice.notes.isNullOrBlank()) "Catatan: ${invoice.notes}\n" else ""}
            |Terima kasih atas kerja samanya! 🙏
            |Salam, *$mcName*
        """.trimMargin().trim()
    }
}
