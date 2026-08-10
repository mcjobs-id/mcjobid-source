package com.isankamil.mcjobid.domain.usecase.job

import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Booking.BookingStatus
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class ManageJobUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend fun createJob(
        name: String,
        client: String?,
        clientId: String?,
        category: String,
        date: LocalDate,
        start: String?,
        end: String?,
        location: String?,
        address: String?,
        dresscode: String?,
        theme: String?,
        mcType: String?,
        language: String?,
        audience: String?,
        specialRequest: String?,
        pic: String?,
        fee: Long,
        dp: Long,
        note: String?
    ): Result<Booking> {
        return try {
            val booking = bookingRepository.createNewBooking(
                name = name,
                client = client,
                clientId = clientId,
                category = category,
                date = date,
                start = start,
                end = end,
                location = location,
                address = address,
                dresscode = dresscode,
                theme = theme,
                mcType = mcType,
                language = language,
                audience = audience,
                specialRequest = specialRequest,
                pic = pic,
                fee = maxOf(0L, fee),
                dp = maxOf(0L, dp),
                note = note
            )
            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJob(booking: Booking): Result<Booking> {
        return try {
            bookingRepository.updateBooking(booking)
            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJobStatus(bookingId: String, targetStatus: BookingStatus): Result<Booking> {
        val currentBooking = bookingRepository.getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Job tidak ditemukan"))

        val transitionResult = BookingStatusMachine.transition(currentBooking.status, targetStatus)
        if (transitionResult.isFailure) {
            return Result.failure(transitionResult.exceptionOrNull() ?: IllegalStateException("Transisi status tidak valid"))
        }

        val updated = currentBooking.copy(
            status = targetStatus,
            updatedAt = LocalDateTime.now()
        )
        bookingRepository.updateBooking(updated)
        return Result.success(updated)
    }

    suspend fun deleteJob(booking: Booking): Result<Unit> {
        return try {
            bookingRepository.deleteBooking(booking)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
