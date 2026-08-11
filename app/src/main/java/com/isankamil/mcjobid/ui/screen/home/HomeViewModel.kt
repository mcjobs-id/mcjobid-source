package com.isankamil.mcjobid.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ReminderRepository
import com.isankamil.mcjobid.data.repository.SyncManager
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.domain.model.FinancialSummary
import com.isankamil.mcjobid.domain.model.Payment
import com.isankamil.mcjobid.domain.model.Reminder
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.domain.usecase.finance.CalculateFinancialSummaryUseCase
import com.isankamil.mcjobid.domain.usecase.finance.ManageExpenseUseCase
import com.isankamil.mcjobid.domain.usecase.finance.ManagePaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.isankamil.mcjobid.util.Constants
import com.isankamil.mcjobid.util.settingsDataStore
import com.isankamil.mcjobid.util.SettingsKeys
import com.isankamil.mcjobid.util.AppUpdateManager
import com.isankamil.mcjobid.domain.model.AppUpdateInfo

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bookingRepository: BookingRepository,
    private val userProfileRepository: UserProfileRepository,
    private val reminderRepository: ReminderRepository,
    private val syncManager: SyncManager,
    private val calculateFinancialSummaryUseCase: CalculateFinancialSummaryUseCase,
    private val managePaymentUseCase: ManagePaymentUseCase,
    private val manageExpenseUseCase: ManageExpenseUseCase,
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    val isSyncing = syncManager.isSyncing
    val lastSyncTime = syncManager.lastSyncTime
    val syncError = syncManager.syncError
    val isOnline = syncManager.isOnlineFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        syncManager.isOnline()
    )
    // Derived: true if online and last sync timestamp is recent
    val isSynced: StateFlow<Boolean> = combine(
        syncManager.isOnlineFlow,
        syncManager.lastSyncTime
    ) { online, lastSync ->
        online && lastSync != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Pintasan Cepat Dasbor Preferences from DataStore - DEFAULT: FALSE (Non-Aktif)
    val quickActionEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QUICK_ACTION_ENABLED] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val qaAddJobEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_ADD_JOB] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaAddClientEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_ADD_CLIENT] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaAddPaymentEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_ADD_PAYMENT] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaAddExpenseEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_ADD_EXPENSE] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaReminderEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_REMINDER] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaRateCardEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_RATE_CARD] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaInvoiceEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_INVOICE] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaAnalyticsEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_ANALYTICS] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaNotificationsEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_NOTIFICATIONS] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaProfileEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_PROFILE] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaSettingsEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_SETTINGS] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val qaTodoEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { it[SettingsKeys.QA_TODO] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _selectedTimeFilter = MutableStateFlow(BookingRepository.TimeFilter.THIS_MONTH)
    val selectedTimeFilter: StateFlow<BookingRepository.TimeFilter> = _selectedTimeFilter.asStateFlow()
    
    private val _customStartDate = MutableStateFlow<LocalDate?>(null)
    val customStartDate: StateFlow<LocalDate?> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<LocalDate?>(null)
    val customEndDate: StateFlow<LocalDate?> = _customEndDate.asStateFlow()

    private val _financialSummary = MutableStateFlow(FinancialSummary.empty())
    val financialSummary: StateFlow<FinancialSummary> = _financialSummary.asStateFlow()
    
    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings.asStateFlow()

    private val _activeBookings = MutableStateFlow<List<Booking>>(emptyList())
    val activeBookings: StateFlow<List<Booking>> = _activeBookings.asStateFlow()

    private val _allPayments = MutableStateFlow<List<Payment>>(emptyList())
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())

    private val _rawReminders = MutableStateFlow<List<Reminder>>(emptyList())
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
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var pendingLoadSources = 6

    private fun sourceLoaded() {
        pendingLoadSources -= 1
        if (pendingLoadSources <= 0) {
            pendingLoadSources = 0
            _isLoading.value = false
        }
    }
    
    init {
        viewModelScope.launch {
            loadData()
        }
    }
    
    fun selectTimeFilter(filter: BookingRepository.TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedTimeFilter.value = BookingRepository.TimeFilter.CUSTOM_RANGE
    }
    
    private fun loadData() {
        _isLoading.value = true
        pendingLoadSources = 6
        loadAllBookings()
        loadActiveBookings()
        loadPayments()
        loadExpenses()
        loadReminders()
        loadUserProfile()

        val filterStateFlow = combine(
            _selectedTimeFilter,
            _customStartDate,
            _customEndDate
        ) { filter, start, end ->
            Triple(filter, start, end)
        }

        viewModelScope.launch {
            combine(
                _allBookings,
                _allPayments,
                _allExpenses,
                filterStateFlow
            ) { bookings, payments, expenses, filterState ->
                val filter = filterState.first
                val customStart = filterState.second
                val customEnd = filterState.third
                val today = LocalDate.now()
                val filteredBookings = when (filter) {
                    BookingRepository.TimeFilter.TODAY -> {
                        bookings.filter { it.date == today && it.status != Booking.BookingStatus.CANCELLED }
                    }
                    BookingRepository.TimeFilter.THIS_WEEK -> {
                        bookings.filter { 
                            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, it.date)
                            daysBetween in -7..7 && it.status != Booking.BookingStatus.CANCELLED
                        }
                    }
                    BookingRepository.TimeFilter.THIS_MONTH -> {
                        bookings.filter { it.date.year == today.year && it.date.monthValue == today.monthValue && it.status != Booking.BookingStatus.CANCELLED }
                    }
                    BookingRepository.TimeFilter.THIS_YEAR -> {
                        bookings.filter { it.date.year == today.year && it.status != Booking.BookingStatus.CANCELLED }
                    }
                    BookingRepository.TimeFilter.CUSTOM_RANGE -> {
                        if (customStart != null && customEnd != null) {
                            val start = if (customStart.isAfter(customEnd)) customEnd else customStart
                            val end = if (customStart.isAfter(customEnd)) customStart else customEnd
                            bookings.filter { !it.date.isBefore(start) && !it.date.isAfter(end) && it.status != Booking.BookingStatus.CANCELLED }
                        } else {
                            bookings.filter { it.status != Booking.BookingStatus.CANCELLED }
                        }
                    }
                    BookingRepository.TimeFilter.ALL -> bookings.filter { it.status != Booking.BookingStatus.CANCELLED }
                }
                
                val filteredExpenses = when (filter) {
                    BookingRepository.TimeFilter.TODAY -> {
                        expenses.filter { 
                            try {
                                val d = LocalDate.parse(it.date)
                                d == today
                            } catch (e: Exception) { false }
                        }
                    }
                    BookingRepository.TimeFilter.THIS_WEEK -> {
                        expenses.filter { 
                            try {
                                val d = LocalDate.parse(it.date)
                                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, d)
                                daysBetween in -7..7
                            } catch (e: Exception) { false }
                        }
                    }
                    BookingRepository.TimeFilter.THIS_MONTH -> {
                        expenses.filter { 
                            try {
                                val d = LocalDate.parse(it.date)
                                d.year == today.year && d.monthValue == today.monthValue
                            } catch (e: Exception) { false }
                        }
                    }
                    BookingRepository.TimeFilter.THIS_YEAR -> {
                        expenses.filter { 
                            try {
                                val d = LocalDate.parse(it.date)
                                d.year == today.year
                            } catch (e: Exception) { false }
                        }
                    }
                    BookingRepository.TimeFilter.CUSTOM_RANGE -> {
                        if (customStart != null && customEnd != null) {
                            val start = if (customStart.isAfter(customEnd)) customEnd else customStart
                            val end = if (customStart.isAfter(customEnd)) customStart else customEnd
                            expenses.filter { 
                                try {
                                    val d = LocalDate.parse(it.date)
                                    !d.isBefore(start) && !d.isAfter(end)
                                } catch (e: Exception) { false }
                            }
                        } else expenses
                    }
                    BookingRepository.TimeFilter.ALL -> expenses
                }

                val summary = calculateFinancialSummaryUseCase(filteredBookings, payments, filteredExpenses)
                summary.copy(
                    month = if (filter == BookingRepository.TimeFilter.THIS_MONTH) today.format(DateTimeFormatter.ofPattern("yyyy-MM")) else null,
                    year = today.year
                )
            }.collect { summary ->
                _financialSummary.value = summary
            }
        }
    }

    private fun loadPayments() {
        viewModelScope.launch {
            var emittedOnce = false
            try {
                managePaymentUseCase.getAllPayments().collect { payments ->
                    _allPayments.value = payments
                    if (!emittedOnce) {
                        emittedOnce = true
                        sourceLoaded()
                    }
                }
            } catch (e: Exception) {
                sourceLoaded()
            }
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            var emittedOnce = false
            try {
                manageExpenseUseCase.getAllExpenses().collect { expenses ->
                    _allExpenses.value = expenses
                    if (!emittedOnce) {
                        emittedOnce = true
                        sourceLoaded()
                    }
                }
            } catch (e: Exception) {
                sourceLoaded()
            }
        }
    }

    private fun loadAllBookings() {
        viewModelScope.launch {
            var emittedOnce = false
            try {
                bookingRepository.getAllBookings().collect { bookings ->
                    _allBookings.value = bookings
                    if (!emittedOnce) {
                        emittedOnce = true
                        sourceLoaded()
                    }
                }
            } catch (e: Exception) {
                sourceLoaded()
            }
        }
    }

    private fun loadActiveBookings() {
        viewModelScope.launch {
            var emittedOnce = false
            try {
                bookingRepository.getActiveBookings().collect { bookings ->
                    _activeBookings.value = bookings
                    if (!emittedOnce) {
                        emittedOnce = true
                        sourceLoaded()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_muat_agenda_aktif, e.message)
            }
        }
    }

    private fun loadReminders() {
        viewModelScope.launch {
            var emittedOnce = false
            try {
                reminderRepository.getActiveReminders().collect { list ->
                    _rawReminders.value = list
                    if (!emittedOnce) {
                        emittedOnce = true
                        sourceLoaded()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_muat_pengingat, e.message)
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
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            var emittedOnce = false
            try {
                val uid = userProfileRepository.getCurrentUserId()
                userProfileRepository.getUserProfileFlow(uid).collect { profile ->
                    _userProfile.value = profile ?: UserProfile(userId = uid)
                    if (!emittedOnce) {
                        emittedOnce = true
                        sourceLoaded()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_muat_profil, e.message)
            }
        }
    }

    fun dismissReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.dismissReminder(id)
        }
    }

    // --- In-App Self Update (OTA Updater) ---
    val appUpdateInfo: StateFlow<AppUpdateInfo?> = appUpdateManager.observeUpdateInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isUpdateBannerDismissed = MutableStateFlow(false)
    val isUpdateBannerDismissed: StateFlow<Boolean> = _isUpdateBannerDismissed.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _updateErrorMessage = MutableStateFlow<String?>(null)
    val updateErrorMessage: StateFlow<String?> = _updateErrorMessage.asStateFlow()

    private val _showUpdateModal = MutableStateFlow(false)
    val showUpdateModal: StateFlow<Boolean> = _showUpdateModal.asStateFlow()

    fun dismissUpdateBanner() {
        _isUpdateBannerDismissed.value = true
    }

    fun openUpdateModal() {
        _showUpdateModal.value = true
    }

    fun closeUpdateModal() {
        _showUpdateModal.value = false
        _updateErrorMessage.value = null
    }

    fun startDownloadAndInstall(context: Context) {
        val info = appUpdateInfo.value ?: return
        _isDownloadingUpdate.value = true
        _downloadProgress.value = 0
        _updateErrorMessage.value = null

        viewModelScope.launch {
            appUpdateManager.downloadAndInstallApk(
                context = context,
                updateInfo = info,
                onProgress = { progress -> _downloadProgress.value = progress },
                onComplete = {
                    _isDownloadingUpdate.value = false
                    _showUpdateModal.value = false
                },
                onError = { err ->
                    _isDownloadingUpdate.value = false
                    _updateErrorMessage.value = err
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
