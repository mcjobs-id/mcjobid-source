package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.PaymentEntity
import java.time.LocalDateTime

data class Payment(
    val id: String,
    val bookingId: String,
    val amount: Long,
    val paymentDate: String,
    val paymentMethod: String,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toEntity() = PaymentEntity(
        id = id,
        bookingId = bookingId,
        amount = amount,
        paymentDate = paymentDate,
        paymentMethod = paymentMethod,
        notes = notes,
        createdAt = createdAt.toString()
    )

    companion object {
        fun fromEntity(entity: PaymentEntity) = Payment(
            id = entity.id,
            bookingId = entity.bookingId,
            amount = entity.amount,
            paymentDate = entity.paymentDate,
            paymentMethod = entity.paymentMethod,
            notes = entity.notes,
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
