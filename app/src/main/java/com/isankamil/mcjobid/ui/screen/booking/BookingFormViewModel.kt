package com.isankamil.mcjobid.ui.screen.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import android.content.Context
import com.isankamil.mcjobid.R
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ClientRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Client
import com.isankamil.mcjobid.domain.usecase.job.ManageJobUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class BookingFormViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bookingRepository: BookingRepository,
    private val clientRepository: ClientRepository,
    private val manageJobUseCase: ManageJobUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String? = savedStateHandle.get<String>("bookingId")
    private val prefillClientName: String? = savedStateHandle.get<String>("clientName")
    private val prefillCategory: String? = savedStateHandle.get<String>("category")
    private val prefillJobName: String? = savedStateHandle.get<String>("jobName")
    private val prefillFee: String? = savedStateHandle.get<String>("fee")
    private val prefillNotes: String? = savedStateHandle.get<String>("notes")

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _createdBookingId = MutableStateFlow<String?>(null)
    val createdBookingId: StateFlow<String?> = _createdBookingId.asStateFlow()

    private val _conflictingBookings = MutableStateFlow<List<Booking>>(emptyList())
    val conflictingBookings: StateFlow<List<Booking>> = _conflictingBookings.asStateFlow()

    private val _showConflictDialog = MutableStateFlow(false)
    val showConflictDialog: StateFlow<Boolean> = _showConflictDialog.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    // Form fields
    val name = MutableStateFlow("")
    val category = MutableStateFlow("Wedding")
    val date = MutableStateFlow<LocalDate?>(LocalDate.now())
    val startTime = MutableStateFlow("19:00")
    val endTime = MutableStateFlow("22:00")
    val location = MutableStateFlow("")
    val address = MutableStateFlow("")

    val clientName = MutableStateFlow("")
    val selectedClientId = MutableStateFlow<String?>(null)
    val clientPhone = MutableStateFlow("")
    val clientEmail = MutableStateFlow("")
    val clientCompany = MutableStateFlow("")
    val pic = MutableStateFlow("")

    val dresscode = MutableStateFlow("")
    val theme = MutableStateFlow("")
    val mcType = MutableStateFlow("Single")
    val language = MutableStateFlow("Bahasa Indonesia")
    val audience = MutableStateFlow("")
    val specialRequest = MutableStateFlow("")

    val fee = MutableStateFlow("")
    val dp = MutableStateFlow("")
    val paymentDate = MutableStateFlow(LocalDate.now().toString())
    val paymentMethod = MutableStateFlow("Bank Transfer")
    val note = MutableStateFlow("")

    init {
        viewModelScope.launch {
            clientRepository.getAllClients().collect { list ->
                _clients.value = list
            }
        }

        bookingId?.let { id ->
            if (id != "null" && id.isNotBlank()) {
                loadBooking(id)
                _isEditMode.value = true
            } else {
                if (!prefillClientName.isNullOrBlank()) {
                    selectedClientId.value = null
                    clientName.value = prefillClientName
                }
                if (!prefillJobName.isNullOrBlank()) {
                    try {
                        name.value = java.net.URLDecoder.decode(prefillJobName, "UTF-8")
                    } catch (_: Exception) {
                        name.value = prefillJobName
                    }
                }
                if (!prefillCategory.isNullOrBlank()) {
                    try {
                        category.value = java.net.URLDecoder.decode(prefillCategory, "UTF-8")
                    } catch (_: Exception) {
                        category.value = prefillCategory
                    }
                }
                if (!prefillFee.isNullOrBlank() && prefillFee != "0") {
                    fee.value = prefillFee
                }
                if (!prefillNotes.isNullOrBlank()) {
                    try {
                        note.value = java.net.URLDecoder.decode(prefillNotes, "UTF-8")
                    } catch (_: Exception) {
                        note.value = prefillNotes
                    }
                }
            }
        }
    }

    private fun loadBooking(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loaded = bookingRepository.getBookingById(id)
                loaded?.let { b ->
                    _booking.value = b
                    name.value = b.name
                    category.value = b.category
                    date.value = b.date
                    startTime.value = b.start ?: "19:00"
                    endTime.value = b.end ?: "22:00"
                    location.value = b.location ?: ""
                    address.value = b.address ?: ""
                    clientName.value = b.client ?: ""
                    selectedClientId.value = b.clientId
                    pic.value = b.pic ?: ""
                    dresscode.value = b.dresscode ?: ""
                    theme.value = b.theme ?: ""
                    mcType.value = b.mcType ?: "Single"
                    language.value = b.language ?: "Bahasa Indonesia"
                    audience.value = b.audience ?: ""
                    specialRequest.value = b.specialRequest ?: ""
                    fee.value = if (b.fee > 0) b.fee.toString() else ""
                    dp.value = if (b.dp > 0) b.dp.toString() else ""
                    note.value = b.note ?: ""
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_muat_detail_job, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectExistingClient(client: Client) {
        selectedClientId.value = client.id
        clientName.value = client.name
        clientPhone.value = client.phone ?: ""
        clientCompany.value = client.company ?: ""
        if (!client.pic.isNullOrBlank()) {
            pic.value = client.pic
        }
    }

    fun hasUnsavedChanges(): Boolean {
        if (_isEditMode.value) return false
        return name.value.isNotBlank() || clientName.value.isNotBlank() || location.value.isNotBlank() || fee.value.isNotBlank()
    }

    fun setDate(newDate: LocalDate) {
        date.value = newDate
        clearFieldError("date")
    }

    fun setStartTime(time: String) {
        startTime.value = time
        clearFieldError("startTime")
    }

    fun setEndTime(time: String) {
        endTime.value = time
        clearFieldError("endTime")
    }

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    private val _firstErrorField = MutableStateFlow<String?>(null)
    val firstErrorField: StateFlow<String?> = _firstErrorField.asStateFlow()

    fun clearFieldError(fieldKey: String) {
        if (_fieldErrors.value.containsKey(fieldKey)) {
            val updated = _fieldErrors.value.toMutableMap()
            updated.remove(fieldKey)
            _fieldErrors.value = updated
        }
    }

    fun submitForm(forceSave: Boolean = false) {
        val errors = mutableMapOf<String, String>()

        if (name.value.trim().isBlank()) {
            errors["name"] = "Nama acara wajib diisi."
        }
        if (clientName.value.trim().isBlank()) {
            errors["clientName"] = "Pilih atau isi nama klien terlebih dahulu."
        }
        val targetDate = date.value
        if (targetDate == null) {
            errors["date"] = "Pilih tanggal acara."
        }
        if (startTime.value.trim().isBlank()) {
            errors["startTime"] = "Masukkan jam mulai."
        }
        if (endTime.value.trim().isBlank()) {
            errors["endTime"] = "Masukkan jam selesai."
        }
        if (location.value.trim().isBlank()) {
            errors["location"] = "Nama lokasi / venue wajib diisi."
        }
        if (address.value.trim().isBlank()) {
            errors["address"] = "Alamat lengkap venue wajib diisi untuk titik peta & Google Calendar."
        }

        var sTimeParsed: LocalTime? = null
        var eTimeParsed: LocalTime? = null

        if (startTime.value.isNotBlank()) {
            try {
                sTimeParsed = LocalTime.parse(startTime.value.trim())
            } catch (e: Exception) {
                errors["startTime"] = "Format jam mulai tidak valid (HH:mm, contoh: 19:00)."
            }
        }

        if (endTime.value.isNotBlank()) {
            try {
                eTimeParsed = LocalTime.parse(endTime.value.trim())
            } catch (e: Exception) {
                errors["endTime"] = "Format jam selesai tidak valid (HH:mm, contoh: 22:00)."
            }
        }

        if (sTimeParsed != null && eTimeParsed != null) {
            if (!sTimeParsed.isBefore(eTimeParsed)) {
                errors["endTime"] = "Waktu selesai harus lebih dari waktu mulai."
            }
        }

        val cleanFeeStr = fee.value.replace(Regex("[^0-9]"), "")
        val feeVal = cleanFeeStr.toLongOrNull() ?: 0L

        val cleanDpStr = dp.value.replace(Regex("[^0-9]"), "")
        val dpVal = cleanDpStr.toLongOrNull() ?: 0L

        if (fee.value.isNotBlank() && feeVal < 0) {
            errors["fee"] = "Honor tidak boleh kurang dari Rp0."
        }

        if (feeVal > 0 && dpVal > feeVal) {
            errors["dp"] = "DP tidak boleh melebihi total honor."
        }

        if (errors.isNotEmpty()) {
            _fieldErrors.value = errors
            _firstErrorField.value = errors.keys.first()
            return
        } else {
            _fieldErrors.value = emptyMap()
            _firstErrorField.value = null
        }

        viewModelScope.launch {
            // Conflict Check (Strict check for both New & Edit modes)
            if (!forceSave && targetDate != null) {
                val excludeId = if (_isEditMode.value) _booking.value?.id ?: "" else ""
                val conflicts = bookingRepository.checkScheduleConflict(
                    date = targetDate,
                    newStartStr = startTime.value.trim(),
                    newEndStr = endTime.value.trim(),
                    excludeId = excludeId
                )
                if (conflicts.isNotEmpty()) {
                    _conflictingBookings.value = conflicts
                    _showConflictDialog.value = true
                    return@launch
                }
            }

            _isLoading.value = true
            try {
                // Auto-create client if clientName is typed and not linked
                var resolvedClientId = selectedClientId.value
                val cName = clientName.value.trim().ifBlank { "Personal Client" }

                if (resolvedClientId == null && cName.isNotBlank()) {
                    val existing = _clients.value.firstOrNull { it.name.trim().equals(cName, ignoreCase = true) }
                    if (existing != null) {
                        resolvedClientId = existing.id
                    } else {
                        val newCId = "c_${System.currentTimeMillis()}"
                        val newClient = Client(
                            id = newCId,
                            name = cName,
                            phone = clientPhone.value.trim().ifBlank { null },
                            company = clientCompany.value.trim().ifBlank { null },
                            pic = pic.value.trim().ifBlank { null }
                        )
                        clientRepository.saveClient(newClient)
                        resolvedClientId = newCId
                    }
                }

                val currentBooking = _booking.value
                if (_isEditMode.value && currentBooking != null) {
                    val updated = currentBooking.copy(
                        name = name.value.trim(),
                        category = category.value,
                        date = targetDate ?: LocalDate.now(),
                        start = startTime.value.trim(),
                        end = endTime.value.trim(),
                        location = location.value.trim().ifBlank { null },
                        address = address.value.trim().ifBlank { null },
                        client = cName,
                        clientId = resolvedClientId,
                        pic = pic.value.trim().ifBlank { null },
                        dresscode = dresscode.value.ifBlank { null },
                        theme = theme.value.ifBlank { null },
                        mcType = mcType.value,
                        language = language.value,
                        audience = audience.value.ifBlank { null },
                        specialRequest = specialRequest.value.ifBlank { null },
                        fee = feeVal,
                        dp = dpVal,
                        note = note.value.ifBlank { null },
                        updatedAt = LocalDateTime.now()
                    )
                    val result = manageJobUseCase.updateJob(updated)
                    if (result.isSuccess) {
                        _createdBookingId.value = updated.id
                    } else {
                        throw result.exceptionOrNull() ?: Exception("Gagal mengupdate job")
                    }
                } else {
                    val result = manageJobUseCase.createJob(
                        name = name.value,
                        client = cName,
                        clientId = resolvedClientId,
                        category = category.value,
                        date = targetDate ?: LocalDate.now(),
                        start = startTime.value,
                        end = endTime.value,
                        location = location.value.ifBlank { null },
                        address = address.value.ifBlank { null },
                        dresscode = dresscode.value.ifBlank { null },
                        theme = theme.value.ifBlank { null },
                        mcType = mcType.value,
                        language = language.value,
                        audience = audience.value.ifBlank { null },
                        specialRequest = specialRequest.value.ifBlank { null },
                        pic = pic.value.ifBlank { null },
                        fee = feeVal,
                        dp = dpVal,
                        note = note.value.ifBlank { null }
                    )
                    if (result.isSuccess) {
                        _createdBookingId.value = result.getOrNull()?.id
                    } else {
                        throw result.exceptionOrNull() ?: Exception("Gagal menyimpan job")
                    }
                }
                _showConflictDialog.value = false
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_simpan_job, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissConflictDialog() {
        _showConflictDialog.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
