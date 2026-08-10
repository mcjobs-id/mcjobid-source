package com.isankamil.mcjobid.domain.usecase.job

import com.isankamil.mcjobid.domain.model.Booking.BookingStatus

/**
 * Domain State Machine enforcing valid Job Status transitions.
 * Prevents illegal manual status overrides.
 */
object BookingStatusMachine {

    fun isValidTransition(current: BookingStatus, target: BookingStatus): Boolean {
        if (current == target) return true

        return when (current) {
            BookingStatus.DRAFT -> target in setOf(
                BookingStatus.CONFIRMED,
                BookingStatus.UPCOMING,
                BookingStatus.CANCELLED
            )
            BookingStatus.CONFIRMED, BookingStatus.UPCOMING -> target in setOf(
                BookingStatus.TODAY,
                BookingStatus.ACTIVE,
                BookingStatus.COMPLETED,
                BookingStatus.CANCELLED
            )
            BookingStatus.TODAY -> target in setOf(
                BookingStatus.ACTIVE,
                BookingStatus.COMPLETED,
                BookingStatus.CANCELLED,
                BookingStatus.CONFIRMED
            )
            BookingStatus.ACTIVE -> target in setOf(
                BookingStatus.COMPLETED,
                BookingStatus.CANCELLED,
                BookingStatus.TODAY,
                BookingStatus.CONFIRMED
            )
            BookingStatus.COMPLETED -> target in setOf(
                BookingStatus.CONFIRMED,
                BookingStatus.TODAY
            )
            BookingStatus.CANCELLED -> target in setOf(
                BookingStatus.CONFIRMED,
                BookingStatus.DRAFT
            )
        }
    }

    fun transition(current: BookingStatus, target: BookingStatus): Result<BookingStatus> {
        return if (isValidTransition(current, target)) {
            Result.success(target)
        } else {
            Result.failure(
                IllegalArgumentException("Transisi status tidak valid dari '${current.name}' ke '${target.name}'")
            )
        }
    }
}
