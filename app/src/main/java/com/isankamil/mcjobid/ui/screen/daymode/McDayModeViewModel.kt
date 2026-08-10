package com.isankamil.mcjobid.ui.screen.daymode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ChecklistRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.ChecklistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class McDayModeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val checklistRepository: ChecklistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: ""

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()

    private val _checklist = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val checklist: StateFlow<List<ChecklistItem>> = _checklist.asStateFlow()

    init {
        if (bookingId.isNotBlank()) {
            viewModelScope.launch {
                val b = bookingRepository.getBookingById(bookingId)
                _booking.value = b

                checklistRepository.getChecklistByBooking(bookingId).collect { list ->
                    _checklist.value = list
                }
            }
        }
    }

    fun toggleChecklist(item: ChecklistItem) {
        viewModelScope.launch {
            checklistRepository.toggleChecklistItem(item.id, item.isCompleted)
        }
    }
}
