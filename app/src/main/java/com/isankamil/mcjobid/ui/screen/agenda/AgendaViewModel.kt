package com.isankamil.mcjobid.ui.screen.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.domain.model.Booking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

import com.isankamil.mcjobid.data.repository.ReminderRepository
import com.isankamil.mcjobid.domain.model.Reminder

enum class JobFilterTab {
    ALL, UPCOMING, TODAY, COMPLETED, UNPAID, CANCELLED
}

enum class CalendarMode {
    MONTH, WEEK, DAY
}

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterTab = MutableStateFlow(JobFilterTab.ALL)
    val selectedFilterTab: StateFlow<JobFilterTab> = _selectedFilterTab.asStateFlow()

    private val _isCalendarView = MutableStateFlow(false)
    val isCalendarView: StateFlow<Boolean> = _isCalendarView.asStateFlow()

    private val _calendarMode = MutableStateFlow(CalendarMode.MONTH)
    val calendarMode: StateFlow<CalendarMode> = _calendarMode.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings.asStateFlow()

    private val _allReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val allReminders: StateFlow<List<Reminder>> = _allReminders.asStateFlow()

    init {
        loadBookings()
        loadReminders()
    }

    private fun loadBookings() {
        viewModelScope.launch {
            bookingRepository.getAllBookings().collect { list ->
                _allBookings.value = list
            }
        }
    }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getActiveReminders().collect { list ->
                _allReminders.value = list
            }
        }
    }

    fun addDateNote(date: LocalDate, title: String, message: String, context: android.content.Context) {
        viewModelScope.launch {
            val noteId = "note_${System.currentTimeMillis()}"
            val newReminder = Reminder(
                id = noteId,
                bookingId = "note_booking",
                reminderType = "DATE_NOTE",
                title = title.ifBlank { "Catatan Tanggal" },
                message = message,
                targetDate = date.toString(),
                isRead = false,
                isDismissed = false,
                createdAt = java.time.LocalDateTime.now()
            )
            reminderRepository.saveReminder(newReminder)

            if (message.isNotBlank()) {
                val scheduler = com.isankamil.mcjobid.util.NotificationScheduler(context)
                scheduler.showNotification(
                    bookingId = "note_booking",
                    bookingName = title.ifBlank { "Catatan Tanggal ${date}" },
                    message = message,
                    notificationId = (System.currentTimeMillis() % 10000).toInt()
                )
            }
        }
    }

    fun dismissReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.dismissReminder(id)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: JobFilterTab) {
        _selectedFilterTab.value = tab
    }

    fun toggleCalendarView() {
        _isCalendarView.value = !_isCalendarView.value
    }

    fun setCalendarMode(mode: CalendarMode) {
        _calendarMode.value = mode
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun setSelectedDate(date: LocalDate?) {
        _selectedDate.value = date
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch {
            bookingRepository.deleteBooking(booking)
        }
    }

    val filteredBookings: StateFlow<List<Booking>> = combine(
        _allBookings,
        _searchQuery,
        _selectedFilterTab
    ) { bookings, query, filterTab ->
        val today = LocalDate.now()
        bookings.filter { booking ->
            val matchesSearch = query.isBlank() ||
                    booking.name.contains(query, ignoreCase = true) ||
                    (booking.client?.contains(query, ignoreCase = true) == true) ||
                    (booking.location?.contains(query, ignoreCase = true) == true)

            val matchesFilter = when (filterTab) {
                JobFilterTab.ALL -> true
                JobFilterTab.UPCOMING -> booking.date.isAfter(today) && booking.status != Booking.BookingStatus.CANCELLED
                JobFilterTab.TODAY -> booking.date == today && booking.status != Booking.BookingStatus.CANCELLED
                JobFilterTab.COMPLETED -> (booking.status == Booking.BookingStatus.COMPLETED || booking.date.isBefore(today)) && booking.status != Booking.BookingStatus.CANCELLED
                JobFilterTab.UNPAID -> booking.outstanding > 0 && booking.status != Booking.BookingStatus.CANCELLED
                JobFilterTab.CANCELLED -> booking.status == Booking.BookingStatus.CANCELLED
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
