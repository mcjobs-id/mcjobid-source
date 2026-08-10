package com.isankamil.mcjobid.ui.screen.job

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ChecklistRepository
import com.isankamil.mcjobid.data.repository.ExpenseRepository
import com.isankamil.mcjobid.data.repository.PaymentRepository
import com.isankamil.mcjobid.data.repository.InvoiceRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.ChecklistItem
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.domain.model.Invoice
import com.isankamil.mcjobid.domain.model.Payment
import com.isankamil.mcjobid.domain.usecase.job.BookingStatusMachine
import com.isankamil.mcjobid.util.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bookingRepository: BookingRepository,
    private val paymentRepository: PaymentRepository,
    private val expenseRepository: ExpenseRepository,
    private val checklistRepository: ChecklistRepository,
    private val invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: ""

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _checklist = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val checklist: StateFlow<List<ChecklistItem>> = _checklist.asStateFlow()

    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice.asStateFlow()

    private val _duplicatedBookingId = MutableStateFlow<String?>(null)
    val duplicatedBookingId: StateFlow<String?> = _duplicatedBookingId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        if (bookingId.isNotBlank()) {
            observeDataFlows()
            loadBooking()
        }
    }

    private fun observeDataFlows() {
        viewModelScope.launch {
            paymentRepository.getPaymentsByBooking(bookingId).collect { pList ->
                _payments.value = pList
            }
        }

        viewModelScope.launch {
            expenseRepository.getExpensesByBooking(bookingId).collect { eList ->
                _expenses.value = eList
            }
        }

        viewModelScope.launch {
            checklistRepository.getChecklistByBooking(bookingId).collect { cList ->
                _checklist.value = cList
            }
        }

        viewModelScope.launch {
            invoiceRepository.getInvoiceByBookingIdFlow(bookingId).collect { inv ->
                _invoice.value = inv
            }
        }
    }

    fun loadBooking() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loaded = bookingRepository.getBookingById(bookingId)
                _booking.value = loaded
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_muat_detail_job, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleCompleted() {
        val current = _booking.value ?: return
        viewModelScope.launch {
            val targetStatus = if (current.status == Booking.BookingStatus.COMPLETED) {
                Booking.BookingStatus.CONFIRMED
            } else {
                Booking.BookingStatus.COMPLETED
            }
            BookingStatusMachine.transition(current.status, targetStatus).fold(
                onSuccess = { newStatus ->
                    val updated = current.copy(status = newStatus, updatedAt = LocalDateTime.now())
                    bookingRepository.updateBooking(updated)
                    _booking.value = updated
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun cancelJob() {
        val current = _booking.value ?: return
        viewModelScope.launch {
            BookingStatusMachine.transition(current.status, Booking.BookingStatus.CANCELLED).fold(
                onSuccess = { newStatus ->
                    val updated = current.copy(status = newStatus, updatedAt = LocalDateTime.now())
                    bookingRepository.updateBooking(updated)
                    _booking.value = updated
                    // Cancel all scheduled AlarmManager reminders for this booking
                    try {
                        val scheduler = NotificationScheduler(context)
                        scheduler.cancelBookingReminders(current.id)
                    } catch (_: Exception) {}
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun markJobCompleted() {
        val current = _booking.value ?: return
        viewModelScope.launch {
            BookingStatusMachine.transition(current.status, Booking.BookingStatus.COMPLETED).fold(
                onSuccess = { newStatus ->
                    val updated = current.copy(status = newStatus, updatedAt = LocalDateTime.now())
                    bookingRepository.updateBooking(updated)
                    _booking.value = updated
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun addPayment(amount: Long, date: String, method: String, notes: String) {
        viewModelScope.launch {
            val newPayment = Payment(
                id = "p_${System.currentTimeMillis()}",
                bookingId = bookingId,
                amount = amount,
                paymentDate = date.ifBlank { LocalDate.now().toString() },
                paymentMethod = method,
                notes = notes,
                createdAt = LocalDateTime.now()
            )
            paymentRepository.addPayment(newPayment)
            loadBooking()
        }
    }

    fun addExpense(category: String, amount: Long, date: String, note: String) {
        viewModelScope.launch {
            val newExpense = Expense(
                id = "exp_${System.currentTimeMillis()}",
                bookingId = bookingId,
                category = category,
                amount = amount,
                date = date,
                note = note,
                createdAt = LocalDateTime.now()
            )
            expenseRepository.addExpense(newExpense)
        }
    }

    fun toggleChecklist(item: ChecklistItem) {
        viewModelScope.launch {
            checklistRepository.toggleChecklistItem(item.id, item.isCompleted)
        }
    }

    fun deleteChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            checklistRepository.deleteChecklistItem(item)
        }
    }

    fun addChecklistItem(title: String) {
        viewModelScope.launch {
            checklistRepository.addChecklistItem(bookingId, title)
        }
    }

    fun duplicateJob(newDate: LocalDate) {
        viewModelScope.launch {
            val duplicated = bookingRepository.duplicateJob(bookingId, newDate)
            if (duplicated != null) {
                _duplicatedBookingId.value = duplicated.id
            }
        }
    }

    fun deleteBooking(onSuccess: () -> Unit) {
        val current = _booking.value ?: return
        viewModelScope.launch {
            bookingRepository.deleteBooking(current)
            onSuccess()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearDuplicatedId() {
        _duplicatedBookingId.value = null
    }
}
