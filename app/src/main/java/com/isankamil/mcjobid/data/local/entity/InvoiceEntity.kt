package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["bookingId"]),
        Index(value = ["ownerId"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val invoiceNumber: String = "", // e.g. INV-2026-0001
    val bookingId: String = "",
    val issueDate: String = "", // yyyy-MM-dd
    val dueDate: String = "", // yyyy-MM-dd
    val status: String = "DRAFT", // "DRAFT", "SENT", "PARTIALLY_PAID", "PAID"
    val totalAmount: Long = 0L,
    val dpAmount: Long = 0L,
    val remainingAmount: Long = 0L,
    val notes: String? = null,
    val createdAt: String = ""
)
