package com.isankamil.mcjobid.ui.screen.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ReminderRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _rawReminders = MutableStateFlow<List<Reminder>>(emptyList())
    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())

    val reminders: StateFlow<List<Reminder>> = combine(
        _rawReminders,
        _allBookings
    ) { rawList, bookings ->
        val bookingMap = bookings.associateBy { it.id }
        val now = java.time.LocalDateTime.now()
        val expiredOrFulfilledIds = mutableListOf<String>()

        val filtered = rawList.filter { reminder ->
            val booking = bookingMap[reminder.bookingId]
            if (booking == null) {
                if (reminder.bookingId.isBlank() || reminder.id.startsWith("sim_") || reminder.bookingId == "sim_booking") {
                    true
                } else {
                    expiredOrFulfilledIds.add(reminder.id)
                    false
                }
            } else {
                val isPaymentType = reminder.reminderType == "PAYMENT" || reminder.reminderType == "PAYMENT_OVERDUE"

                // 1. Jika job sudah LUNAS (outstanding <= 0), sembunyikan reminder pelunasan
                if (isPaymentType && (booking.outstanding <= 0 || booking.paymentStatus == Booking.PaymentStatus.PAID)) {
                    expiredOrFulfilledIds.add(reminder.id)
                    return@filter false
                }

                // 2. Jika job sudah COMPLETED / CANCELLED, sembunyikan reminder persiapan acara
                if (!isPaymentType && (booking.status == Booking.BookingStatus.COMPLETED || booking.status == Booking.BookingStatus.CANCELLED)) {
                    expiredOrFulfilledIds.add(reminder.id)
                    return@filter false
                }

                // 3. Cek apakah jam & detik penyelesaian job sudah berlalu
                val jobEndDateTime = getBookingEndDateTime(booking)
                if (now.isAfter(jobEndDateTime)) {
                    expiredOrFulfilledIds.add(reminder.id)
                    return@filter false
                }

                true
            }
        }

        if (expiredOrFulfilledIds.isNotEmpty()) {
            viewModelScope.launch {
                expiredOrFulfilledIds.forEach { id ->
                    reminderRepository.dismissReminder(id)
                }
            }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _simulatedReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val simulatedReminders: StateFlow<List<Reminder>> = _simulatedReminders.asStateFlow()

    init {
        viewModelScope.launch {
            reminderRepository.getActiveReminders().collect { list ->
                _rawReminders.value = list
            }
        }
        viewModelScope.launch {
            bookingRepository.getAllBookings().collect { list ->
                _allBookings.value = list
            }
        }
    }

    private fun getBookingEndDateTime(booking: Booking): java.time.LocalDateTime {
        val endStr = booking.end?.trim()
        if (!endStr.isNullOrBlank()) {
            try {
                val cleanStr = endStr.replace(".", ":").replace(" WIB", "", ignoreCase = true).trim()
                val parts = cleanStr.split(":")
                if (parts.size >= 2) {
                    val hour = parts[0].toIntOrNull()
                    val min = parts[1].toIntOrNull()
                    if (hour != null && min != null) {
                        return booking.date.atTime(hour.coerceIn(0, 23), min.coerceIn(0, 59), 59)
                    }
                }
            } catch (_: Exception) {}
        }
        return booking.date.atTime(23, 59, 59)
    }

    val hasSimulatedReminders: StateFlow<Boolean> = reminders.map { list ->
        list.any { it.id.startsWith("sim_") || it.bookingId == "sim_booking" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun dismissReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.dismissReminder(id)
        }
    }

    fun clearSimulations() {
        viewModelScope.launch {
            reminderRepository.deleteSimulatedReminders()
        }
    }

    fun createSimulatedReminder(
        reminderType: String,
        title: String,
        message: String,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            val simulatedId = "sim_${System.currentTimeMillis()}"
            val todayDate = java.time.LocalDate.now().toString()
            val newReminder = Reminder(
                id = simulatedId,
                bookingId = "sim_booking",
                reminderType = reminderType,
                title = title,
                message = message,
                targetDate = todayDate,
                isRead = false,
                isDismissed = false,
                createdAt = java.time.LocalDateTime.now()
            )
            
            reminderRepository.saveReminder(newReminder)

            // Trigger real status bar push notification test
            val scheduler = com.isankamil.mcjobid.util.NotificationScheduler(context)
            scheduler.showNotification(
                bookingId = "sim_booking",
                bookingName = title,
                message = message,
                notificationId = (System.currentTimeMillis() % 10000).toInt()
            )
        }
    }
}
