package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
data class ExpenseEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val bookingId: String = "",
    val category: String = "Transport", // "Transport", "Tol", "Parkir", "Akomodasi", "Konsumsi", "Wardrobe", "Equipment", "Lainnya"
    val amount: Long = 0L,
    val date: String = "", // yyyy-MM-dd
    val note: String? = null,
    val createdAt: String = ""
)
