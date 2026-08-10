package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.InvoiceEntity
import java.time.LocalDateTime

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val bookingId: String,
    val issueDate: String,
    val dueDate: String,
    val status: InvoiceStatus = InvoiceStatus.SENT,
    val totalAmount: Long,
    val dpAmount: Long,
    val remainingAmount: Long,
    val notes: String? = null,
    val createdAt: LocalDateTime
) {
    enum class InvoiceStatus(val label: String) {
        DRAFT("Draft"),
        SENT("Terkirim"),
        PARTIALLY_PAID("Sebagian (DP)"),
        PAID("Lunas"),
        CANCELLED("Dibatalkan")
    }

    val isPaid: Boolean get() = status == InvoiceStatus.PAID || remainingAmount <= 0
    val isPartiallyPaid: Boolean get() = status == InvoiceStatus.PARTIALLY_PAID || (dpAmount > 0 && remainingAmount > 0)

    fun toEntity() = InvoiceEntity(
        id = id,
        invoiceNumber = invoiceNumber,
        bookingId = bookingId,
        issueDate = issueDate,
        dueDate = dueDate,
        status = status.name,
        totalAmount = totalAmount,
        dpAmount = dpAmount,
        remainingAmount = remainingAmount,
        notes = notes,
        createdAt = createdAt.toString()
    )

    companion object {
        fun fromEntity(entity: InvoiceEntity) = Invoice(
            id = entity.id,
            invoiceNumber = entity.invoiceNumber,
            bookingId = entity.bookingId,
            issueDate = entity.issueDate,
            dueDate = entity.dueDate,
            status = try { InvoiceStatus.valueOf(entity.status) } catch (e: Exception) { InvoiceStatus.SENT },
            totalAmount = entity.totalAmount,
            dpAmount = entity.dpAmount,
            remainingAmount = entity.remainingAmount,
            notes = entity.notes,
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}

enum class InvoiceTemplate(
    val id: String,
    val title: String,
    val description: String,
    val accentColorHex: String,
    val tag: String
) {
    MODERN_CORPORATE(
        id = "corporate",
        title = "Modern Corporate",
        description = "Desain bersih 2-kolom formal untuk acara perusahaan, instansi & konvensi",
        accentColorHex = "#4F46E5",
        tag = "FORMAL & CORPORATE"
    ),
    LUXURY_ELEGANT(
        id = "luxury",
        title = "Luxury Elegant",
        description = "Surat penawaran & invoice resmi bertema emas untuk wedding & VIP gala",
        accentColorHex = "#B45309",
        tag = "HIGH-END WEDDING"
    ),
    MINIMALIST_CREATIVE(
        id = "creative",
        title = "Minimalist Creative",
        description = "Tampilan kartu modern bold untuk gigs, festival & party",
        accentColorHex = "#059669",
        tag = "GIGS & FESTIVAL"
    )
}
