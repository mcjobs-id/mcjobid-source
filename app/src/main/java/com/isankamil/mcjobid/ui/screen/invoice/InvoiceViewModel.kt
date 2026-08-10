package com.isankamil.mcjobid.ui.screen.invoice

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Invoice
import com.isankamil.mcjobid.domain.model.InvoiceTemplate
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.domain.usecase.invoice.ManageInvoiceUseCase
import com.isankamil.mcjobid.util.InvoiceGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val manageInvoiceUseCase: ManageInvoiceUseCase,
    private val bookingRepository: BookingRepository,
    private val userProfileRepository: UserProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingIdParam: String? = savedStateHandle.get<String>("bookingId")

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _invoiceNumber = MutableStateFlow("INV-2026-0001")
    val invoiceNumber: StateFlow<String> = _invoiceNumber.asStateFlow()

    private val _issueDate = MutableStateFlow(LocalDate.now().toString())
    val issueDate: StateFlow<String> = _issueDate.asStateFlow()

    private val _dueDate = MutableStateFlow(LocalDate.now().toString())
    val dueDate: StateFlow<String> = _dueDate.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedTemplate = MutableStateFlow(InvoiceTemplate.MODERN_CORPORATE)
    val selectedTemplate: StateFlow<InvoiceTemplate> = _selectedTemplate.asStateFlow()

    private val _existingInvoice = MutableStateFlow<Invoice?>(null)
    val existingInvoice: StateFlow<Invoice?> = _existingInvoice.asStateFlow()

    private val _invoiceHistory = MutableStateFlow<List<Invoice>>(emptyList())
    val invoiceHistory: StateFlow<List<Invoice>> = _invoiceHistory.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Load User Profile
        viewModelScope.launch {
            val uid = userProfileRepository.getCurrentUserId()
            userProfileRepository.getUserProfileFlow(uid).collect { profile ->
                _userProfile.value = profile
            }
        }

        // Load Invoice History
        viewModelScope.launch {
            manageInvoiceUseCase.getAllInvoices().collect { list ->
                _invoiceHistory.value = list
            }
        }

        // Load Bookings
        viewModelScope.launch {
            bookingRepository.getAllBookings().collect { list ->
                _bookings.value = list
                val currentTargetId = _selectedBooking.value?.id ?: bookingIdParam
                if (!currentTargetId.isNullOrBlank() && currentTargetId != "null") {
                    val target = list.firstOrNull { it.id == currentTargetId }
                    if (target != null) {
                        selectBooking(target)
                    } else if (list.isNotEmpty()) {
                        selectBooking(list.first())
                    }
                } else if (list.isNotEmpty() && _selectedBooking.value == null) {
                    selectBooking(list.first())
                }
            }
        }
    }

    fun selectBooking(booking: Booking) {
        _selectedBooking.value = booking
        _dueDate.value = booking.date.toString()
        _notes.value = booking.note ?: "Pembayaran dapat ditransfer ke rekening tertera. Mohon konfirmasi bukti transfer via WhatsApp."
        _generatedPdfFile.value = null

        viewModelScope.launch {
            val existing = manageInvoiceUseCase.getAllInvoices().firstOrNull()?.firstOrNull { it.bookingId == booking.id }
            _existingInvoice.value = existing

            if (existing != null) {
                _invoiceNumber.value = existing.invoiceNumber
                _issueDate.value = existing.issueDate
                _dueDate.value = existing.dueDate
                if (!existing.notes.isNullOrBlank()) {
                    _notes.value = existing.notes
                }
            } else {
                _invoiceNumber.value = manageInvoiceUseCase.generateInvoiceNumber()
            }
        }
    }

    fun updateIssueDate(newDate: String) {
        _issueDate.value = newDate
    }

    fun selectTemplate(template: InvoiceTemplate) {
        _selectedTemplate.value = template
    }

    fun updateDueDate(newDate: String) {
        _dueDate.value = newDate
    }

    fun updateNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun generateInvoice(context: Context) {
        val target = _selectedBooking.value ?: run {
            _errorMessage.value = context.getString(R.string.err_pilih_job_dulu)
            return
        }

        if (_isLoading.value) return // Prevent double-tap

        _isLoading.value = true
        _errorMessage.value = null

        val generator = InvoiceGenerator(context)
        val num = _invoiceNumber.value
        val issue = _issueDate.value
        val due = _dueDate.value
        val customNote = _notes.value

        generator.generateInvoice(
            booking = target,
            userProfile = _userProfile.value,
            invoiceNumber = num,
            issueDate = issue,
            dueDate = due,
            notes = customNote,
            onSuccess = { file ->
                _generatedPdfFile.value = file
                _isLoading.value = false
                _statusMessage.value = "Invoice $num berhasil dibuat!"

                viewModelScope.launch {
                    val result = manageInvoiceUseCase.createOrUpdateInvoice(
                        bookingId = target.id,
                        invoiceNumber = num,
                        issueDate = issue,
                        dueDate = due,
                        notes = customNote
                    )
                    result.onSuccess { saved ->
                        _existingInvoice.value = saved
                    }.onFailure { err ->
                        _errorMessage.value = context.getString(R.string.err_pdf_tersimpan_sync_gagal, err.message)
                    }
                }
            },
            onError = { err ->
                _isLoading.value = false
                _errorMessage.value = context.getString(R.string.err_buat_pdf, err.message)
            }
        )
    }

    fun shareInvoice(context: Context) {
        val file = _generatedPdfFile.value
        if (file != null && file.exists()) {
            val generator = InvoiceGenerator(context)
            generator.shareInvoice(file)
        } else {
            generateInvoice(context)
        }
    }

    fun viewInvoice(context: Context) {
        val file = _generatedPdfFile.value
        if (file != null && file.exists()) {
            val generator = InvoiceGenerator(context)
            generator.viewInvoice(file)
        } else {
            generateInvoice(context)
        }
    }

    fun previewSamplePdf(template: InvoiceTemplate, context: Context) {
        val generator = InvoiceGenerator(context)
        _isLoading.value = true
        generator.generateSamplePdf(
            template = template,
            onSuccess = { file ->
                _isLoading.value = false
                generator.viewInvoice(file)
            },
            onError = { err ->
                _isLoading.value = false
                _errorMessage.value = "Gagal memuat pratinjau PDF: ${err.message}"
            }
        )
    }

    fun copySummaryToClipboard(context: Context) {
        val target = _selectedBooking.value ?: return
        val inv = _existingInvoice.value ?: Invoice(
            id = "temp",
            invoiceNumber = _invoiceNumber.value,
            bookingId = target.id,
            issueDate = _issueDate.value,
            dueDate = _dueDate.value,
            status = if (target.outstanding == 0L) Invoice.InvoiceStatus.PAID else if (target.dp > 0) Invoice.InvoiceStatus.PARTIALLY_PAID else Invoice.InvoiceStatus.SENT,
            totalAmount = target.fee,
            dpAmount = target.dp,
            remainingAmount = target.outstanding,
            notes = _notes.value,
            createdAt = java.time.LocalDateTime.now()
        )

        val summaryText = manageInvoiceUseCase.generateShareableSummary(inv, target, _userProfile.value)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invoice MC", summaryText)
        clipboard.setPrimaryClip(clip)
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            _isLoading.value = true
            manageInvoiceUseCase.deleteInvoice(invoice)
                .onSuccess {
                    if (_existingInvoice.value?.id == invoice.id) {
                        _existingInvoice.value = null
                        _generatedPdfFile.value = null
                    }
                    _statusMessage.value = "Invoice ${invoice.invoiceNumber} berhasil dihapus."
                }
                .onFailure { err ->
                    _errorMessage.value = context.getString(R.string.err_hapus_invoice, err.message)
                }
            _isLoading.value = false
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
