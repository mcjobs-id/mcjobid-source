package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookingId"])]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val bookingId: String = "",
    val amount: Long = 0L,
    val paymentDate: String = "", // format "yyyy-MM-dd" or ISO
    val paymentMethod: String = "Bank Transfer", // "Bank Transfer", "Cash", "E-Wallet", etc.
    val notes: String? = null,
    val createdAt: String = ""
)
