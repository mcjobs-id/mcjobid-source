package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.BookingEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class Booking(
    val id: String,
    val ownerId: String = "",
    val name: String,
    val client: String? = null,
    val clientId: String? = null,
    val category: String = "Wedding",
    val date: LocalDate,
    val start: String? = null,
    val end: String? = null,
    val location: String? = null,
    val address: String? = null,
    val dresscode: String? = null,
    val theme: String? = null,
    val mcType: String? = "Single",
    val language: String? = "Bahasa Indonesia",
    val audience: String? = null,
    val specialRequest: String? = null,
    val pic: String? = null,
    val fee: Long = 0,
    val dp: Long = 0,
    val note: String? = null,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    enum class BookingStatus {
        DRAFT, CONFIRMED, UPCOMING, TODAY, ACTIVE, COMPLETED, CANCELLED
    }
    
    enum class PaymentStatus {
        TBD, UNPAID, PARTIAL, PAID, OVERDUE
    }
    
    val paymentStatus: PaymentStatus
        get() = when {
            fee == 0L && dp == 0L -> PaymentStatus.TBD
            dp >= fee && fee > 0L -> PaymentStatus.PAID
            date.isBefore(LocalDate.now()) && dp < fee && fee > 0L -> PaymentStatus.OVERDUE
            dp > 0L && dp < fee -> PaymentStatus.PARTIAL
            dp == 0L && fee > 0L -> PaymentStatus.UNPAID
            else -> PaymentStatus.TBD
        }
    
    val outstanding: Long
        get() = maxOf(0L, fee - dp)
    
    val isEventDone: Boolean
        get() = status == BookingStatus.COMPLETED || 
                date.atTime(23, 59, 59).isBefore(LocalDateTime.now())
    
    fun toEntity() = BookingEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        client = client,
        clientId = clientId,
        category = category,
        date = date.toString(),
        start = start,
        end = end,
        loc = location,
        address = address,
        dresscode = dresscode,
        theme = theme,
        mcType = mcType,
        language = language,
        audience = audience,
        specialRequest = specialRequest,
        pic = pic,
        fee = fee,
        dp = dp,
        note = note,
        status = status.name.lowercase(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
    
    companion object {
        fun fromEntity(entity: BookingEntity) = Booking(
            id = entity.id,
            ownerId = entity.ownerId,
            name = entity.name,
            client = entity.client,
            clientId = entity.clientId,
            category = entity.category ?: "Wedding",
            date = try { LocalDate.parse(entity.date) } catch (e: Exception) { LocalDate.now() },
            start = entity.start,
            end = entity.end,
            location = entity.loc,
            address = entity.address,
            dresscode = entity.dresscode,
            theme = entity.theme,
            mcType = entity.mcType ?: "Single",
            language = entity.language ?: "Bahasa Indonesia",
            audience = entity.audience,
            specialRequest = entity.specialRequest,
            pic = entity.pic,
            fee = entity.fee,
            dp = entity.dp,
            note = entity.note,
            status = when (entity.status.lowercase()) {
                "done", "completed" -> BookingStatus.COMPLETED
                "cancelled" -> BookingStatus.CANCELLED
                "draft" -> BookingStatus.DRAFT
                "today" -> BookingStatus.TODAY
                "upcoming" -> BookingStatus.UPCOMING
                "active" -> BookingStatus.ACTIVE
                else -> BookingStatus.CONFIRMED
            },
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() },
            updatedAt = try { LocalDateTime.parse(entity.updatedAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
