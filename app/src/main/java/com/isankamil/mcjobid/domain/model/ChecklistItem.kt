package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.ChecklistEntity
import java.time.LocalDateTime

data class ChecklistItem(
    val id: String,
    val bookingId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toEntity() = ChecklistEntity(
        id = id,
        bookingId = bookingId,
        title = title,
        isCompleted = isCompleted,
        sortOrder = sortOrder,
        createdAt = createdAt.toString()
    )

    companion object {
        fun fromEntity(entity: ChecklistEntity) = ChecklistItem(
            id = entity.id,
            bookingId = entity.bookingId,
            title = entity.title,
            isCompleted = entity.isCompleted,
            sortOrder = entity.sortOrder,
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
