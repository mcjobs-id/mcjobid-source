package com.isankamil.mcjobid.ui.util

import androidx.compose.ui.graphics.Color
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.theme.*

fun getStatusColor(paymentStatus: Booking.PaymentStatus): Color {
    return when (paymentStatus) {
        Booking.PaymentStatus.TBD -> StatusTBD
        Booking.PaymentStatus.UNPAID -> StatusUnpaid
        Booking.PaymentStatus.PARTIAL -> StatusPartial
        Booking.PaymentStatus.PAID -> StatusPaid
        Booking.PaymentStatus.OVERDUE -> Error
    }
}

