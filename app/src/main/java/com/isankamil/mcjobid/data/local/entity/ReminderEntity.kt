package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val bookingId: String = "",
    val title: String = "",
    val message: String = "",
    val reminderType: String = "H-1", // "H-14", "H-7", "H-3", "H-1", "TODAY", "H+1", "PAYMENT_OVERDUE"
    val targetDate: String = "", // yyyy-MM-dd
    val isRead: Boolean = false,
    val isDismissed: Boolean = false,
    val createdAt: String = ""
)
