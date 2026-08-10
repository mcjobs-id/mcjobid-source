package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklists",
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
data class ChecklistEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val bookingId: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: String = ""
)
