package com.isankamil.mcjobid.ui.screen.followup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.domain.model.Booking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowUpViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())

    val unpaidBookings: StateFlow<List<Booking>> = _allBookings.map { bookings ->
        bookings.filter { it.outstanding > 0 && it.status != Booking.BookingStatus.CANCELLED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingBookings: StateFlow<List<Booking>> = _allBookings.map { bookings ->
        bookings.filter {
            it.status == Booking.BookingStatus.CONFIRMED ||
            it.status == Booking.BookingStatus.UPCOMING
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBookings()
    }

    private fun loadBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                bookingRepository.getAllBookings().collect { list ->
                    _allBookings.value = list
                    _isLoading.value = false
                }
            } catch (_: Exception) {
                _isLoading.value = false
            }
        }
    }
}
