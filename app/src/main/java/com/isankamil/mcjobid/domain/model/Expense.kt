package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.ExpenseEntity
import java.time.LocalDateTime

data class Expense(
    val id: String,
    val bookingId: String,
    val category: String,
    val amount: Long,
    val date: String,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toEntity() = ExpenseEntity(
        id = id,
        bookingId = bookingId,
        category = category,
        amount = amount,
        date = date,
        note = note,
        createdAt = createdAt.toString()
    )

    companion object {
        fun fromEntity(entity: ExpenseEntity) = Expense(
            id = entity.id,
            bookingId = entity.bookingId,
            category = entity.category,
            amount = entity.amount,
            date = entity.date,
            note = entity.note,
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
