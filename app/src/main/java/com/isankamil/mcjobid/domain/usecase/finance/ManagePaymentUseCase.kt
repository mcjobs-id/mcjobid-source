package com.isankamil.mcjobid.domain.usecase.finance

import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.PaymentRepository
import com.isankamil.mcjobid.domain.model.Payment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagePaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository
) {
    fun getAllPayments(): Flow<List<Payment>> {
        return paymentRepository.getAllPayments()
    }

    fun getPaymentsByBooking(bookingId: String): Flow<List<Payment>> {
        return paymentRepository.getPaymentsByBooking(bookingId)
    }

    suspend fun addPayment(
        bookingId: String,
        amount: Long,
        paymentDate: String = LocalDate.now().toString(),
        paymentMethod: String = "Bank Transfer",
        notes: String = ""
    ): Result<Payment> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Masukkan nominal pembayaran lebih besar dari Rp0."))
        }

        val booking = bookingRepository.getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Job tidak ditemukan."))

        val paymentId = "p_${bookingId}_${System.currentTimeMillis()}"
        val payment = Payment(
            id = paymentId,
            bookingId = bookingId,
            amount = amount,
            paymentDate = paymentDate.ifBlank { LocalDate.now().toString() },
            paymentMethod = paymentMethod,
            notes = notes,
            createdAt = LocalDateTime.now()
        )

        return try {
            paymentRepository.addPayment(payment)
            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePayment(payment: Payment): Result<Unit> {
        return try {
            paymentRepository.deletePayment(payment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePaymentById(paymentId: String, bookingId: String): Result<Unit> {
        return try {
            paymentRepository.deletePaymentById(paymentId, bookingId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
