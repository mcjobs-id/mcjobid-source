package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.ReminderEntity
import java.time.LocalDateTime

data class Reminder(
    val id: String,
    val bookingId: String,
    val title: String,
    val message: String,
    val reminderType: String,
    val targetDate: String,
    val isRead: Boolean = false,
    val isDismissed: Boolean = false,
    val createdAt: LocalDateTime
) {
    fun toEntity() = ReminderEntity(
        id = id,
        bookingId = bookingId,
        title = title,
        message = message,
        reminderType = reminderType,
        targetDate = targetDate,
        isRead = isRead,
        isDismissed = isDismissed,
        createdAt = createdAt.toString()
    )

    companion object {
        fun fromEntity(entity: ReminderEntity) = Reminder(
            id = entity.id,
            bookingId = entity.bookingId,
            title = entity.title,
            message = entity.message,
            reminderType = entity.reminderType,
            targetDate = entity.targetDate,
            isRead = entity.isRead,
            isDismissed = entity.isDismissed,
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
