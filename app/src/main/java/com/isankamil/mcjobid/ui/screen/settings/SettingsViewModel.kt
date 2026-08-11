package com.isankamil.mcjobid.ui.screen.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.data.repository.SyncManager
import com.isankamil.mcjobid.data.remote.firebase.FirebaseAuthService
import com.isankamil.mcjobid.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.util.settingsDataStore
import com.isankamil.mcjobid.util.SettingsKeys
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val firebaseAuthService: FirebaseAuthService,
    private val syncManager: SyncManager,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // Real-time auth state
    val isLoggedIn: StateFlow<Boolean> = firebaseAuthService.authStateFlow
        .map { it is com.isankamil.mcjobid.domain.model.AuthUiState.Authenticated }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), firebaseAuthService.isUserLoggedIn)

    // Persisted preferences backed by DataStore
    val reminderEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.REMINDER_ENABLED] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appLanguage: StateFlow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.APP_LANGUAGE] ?: "id" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "id")

    val pinEnabled: StateFlow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.APP_PIN_ENABLED] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pinCode: StateFlow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.APP_PIN_CODE] ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val pinTimeoutMinutes: StateFlow<Int> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.PIN_TIMEOUT_MINUTES] ?: 5 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val securityBackupKey: StateFlow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.SECURITY_BACKUP_KEY] ?: "MCJOB2026" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MCJOB2026")

    val appTheme: StateFlow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[SettingsKeys.APP_THEME] ?: "system" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val reminderDays: StateFlow<Set<Int>> = context.settingsDataStore.data
        .map { preferences ->
            val raw = preferences[SettingsKeys.REMINDER_DAYS_SET]
            if (!raw.isNullOrBlank()) {
                raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } else {
                // Migrate from legacy single-Int key if exists
                val legacy = preferences[SettingsKeys.EVENT_REMINDER_DAYS]
                if (legacy != null) setOf(legacy) else setOf(1)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf(1))

    // Pintasan Cepat Dasbor (Quick Floating Action) - DEFAULT: FALSE (Non-Aktif)
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

    // Sync status: real-time check
    val isSynced: StateFlow<Boolean> = combine(syncManager.isOnlineFlow, isLoggedIn) { isOnline, loggedIn ->
        isOnline && loggedIn
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastSyncFormatted: StateFlow<String> = syncManager.lastSyncTime.map { timestamp ->
        if (timestamp != null && timestamp > 0) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.forLanguageTag("id-ID"))
            "Terakhir sinkron: ${sdf.format(java.util.Date(timestamp))}"
        } else {
            "Cloud Sync Aktif (Room & Firestore)"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cloud Sync Aktif (Room & Firestore)")

    // Form fields
    private val _bankName = MutableStateFlow("")
    val bankName: StateFlow<String> = _bankName.asStateFlow()
    
    private val _accountNumber = MutableStateFlow("")
    val accountNumber: StateFlow<String> = _accountNumber.asStateFlow()
    
    private val _accountName = MutableStateFlow("")
    val accountName: StateFlow<String> = _accountName.asStateFlow()
    
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = userProfileRepository.getCurrentUserId()
            userProfileRepository.getUserProfileFlow(userId).collect { profile ->
                val validProfile = profile ?: UserProfile(userId = userId)
                _userProfile.value = validProfile
                
                // Only populate form if it's the first time
                if (_bankName.value.isBlank()) {
                    populateForm(validProfile)
                }
                _isLoading.value = false
            }
        }
    }
    
    private fun populateForm(profile: UserProfile) {
        _bankName.value = profile.bankName ?: ""
        _accountNumber.value = profile.accountNumber ?: ""
        _accountName.value = profile.accountName ?: ""
        _phoneNumber.value = profile.phoneNumber ?: ""
    }
    
    fun updateBankName(value: String) { _bankName.value = value }
    fun updateAccountNumber(value: String) { _accountNumber.value = value }
    fun updateAccountName(value: String) { _accountName.value = value }
    fun updatePhoneNumber(value: String) { _phoneNumber.value = value }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.REMINDER_ENABLED] = enabled
            }
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.APP_LANGUAGE] = lang
            }
            com.isankamil.mcjobid.util.LocaleHelper.applyLocale(context, lang)
            _successMessage.value = if (lang == "id") "Bahasa aplikasi diubah ke Bahasa Indonesia" else "App language changed to English"
        }
    }

    fun setPinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.APP_PIN_ENABLED] = enabled
            }
            if (!enabled) {
                _successMessage.value = "Kunci PIN aplikasi dinonaktifkan"
            }
        }
    }

    fun setPinCode(code: String) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.APP_PIN_CODE] = code
                preferences[SettingsKeys.APP_PIN_ENABLED] = true
            }
            _successMessage.value = "PIN keamanan 4-digit berhasil disimpan & diaktifkan"
        }
    }

    fun setPinTimeoutMinutes(minutes: Int) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.PIN_TIMEOUT_MINUTES] = minutes
            }
            val label = when (minutes) {
                0 -> "Kunci seketika saat keluar aplikasi"
                else -> "Toleransi kunci otomatis diatur ke $minutes menit"
            }
            _successMessage.value = label
        }
    }

    fun setSecurityBackupKey(key: String) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.SECURITY_BACKUP_KEY] = key
            }
            _successMessage.value = "Kunci Cadangan Keamanan berhasil diperbarui"
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.APP_THEME] = theme
            }
            _successMessage.value = "Tema aplikasi berhasil diperbarui"
        }
    }

    /**
     * Toggle satu hari dari set aktif.
     * Minimal 1 hari harus tetap aktif (tidak bisa kosong).
     */
    fun toggleReminderDay(day: Int) {
        viewModelScope.launch {
            val current = reminderDays.value
            val updated = if (current.contains(day)) {
                if (current.size <= 1) current // Jangan hapus kalau hanya tersisa 1
                else current - day
            } else {
                current + day
            }
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.REMINDER_DAYS_SET] = updated.sorted().joinToString(",")
            }
        }
    }

    /**
     * Tambahkan hari kustom ke set (misal H-10, H-14).
     * Hanya terima angka 1..30.
     */
    fun addCustomReminderDay(day: Int) {
        if (day < 0 || day > 30) return
        viewModelScope.launch {
            val updated = reminderDays.value + day
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.REMINDER_DAYS_SET] = updated.sorted().joinToString(",")
            }
        }
    }

    /**
     * Hapus hari kustom dari set.
     */
    fun removeCustomReminderDay(day: Int) {
        viewModelScope.launch {
            val current = reminderDays.value
            if (current.size <= 1) return@launch
            val updated = current - day
            context.settingsDataStore.edit { preferences ->
                preferences[SettingsKeys.REMINDER_DAYS_SET] = updated.sorted().joinToString(",")
            }
        }
    }

    fun forceSyncNow() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                syncManager.forceSync()
                _successMessage.value = "Sinkronisasi data dengan cloud berhasil"
            } catch (e: Exception) {
                _errorMessage.value = "Gagal sinkronisasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = firebaseAuthService.getCurrentUserId() ?: userProfileRepository.getCurrentUserId()
                userProfileRepository.createOrUpdateUserProfile(
                    userId = userId,
                    name = firebaseAuthService.getCurrentUserName() ?: _userProfile.value?.displayName,
                    email = firebaseAuthService.getCurrentUserEmail() ?: _userProfile.value?.email,
                    photoUri = _userProfile.value?.photoUri, // Maintain existing photoUri
                    bankName = _bankName.value.ifBlank { null },
                    accountNumber = _accountNumber.value.ifBlank { null },
                    accountName = _accountName.value.ifBlank { null },
                    phoneNumber = _phoneNumber.value.ifBlank { null },
                    profileCompleted = true
                )
                _successMessage.value = "Profil berhasil diperbarui"
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_simpan_profil, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePhotoUri(photoUri: String) {
        viewModelScope.launch {
            val uid = userProfileRepository.getCurrentUserId()
            val current = _userProfile.value ?: UserProfile(userId = uid)

            // Jika URI sudah berupa HTTPS (sudah di cloud), tidak perlu upload ulang
            val cloudUrl = if (photoUri.startsWith("https://")) {
                photoUri
            } else {
                // Upload ke Firebase Storage → dapatkan URL publik yang persistent
                com.isankamil.mcjobid.util.FirebaseStorageHelper.uploadProfilePhoto(
                    context = context,
                    userId = uid,
                    sourceUri = photoUri
                ) ?: run {
                    // Fallback: simpan ke internal storage jika upload gagal (offline)
                    com.isankamil.mcjobid.util.FileUtil.saveImageToInternalStorage(
                        context,
                        android.net.Uri.parse(photoUri)
                    ) ?: photoUri
                }
            }

            val updated = current.copy(
                photoUri = cloudUrl,
                photoUrl = if (cloudUrl.startsWith("https://")) cloudUrl else current.photoUrl
            )
            userProfileRepository.saveProfileToFirebase(updated)
            _userProfile.value = updated
            _successMessage.value = "Foto profil berhasil diperbarui"
        }
    }


    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = firebaseAuthService.getCurrentUserId()
                userId?.let { syncManager.clearAllUserData(it) }
                
                firebaseAuthService.signOut()
                _userProfile.value = null
                resetForm()
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_logout_gagal, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun resetForm() {
        _bankName.value = ""
        _accountNumber.value = ""
        _accountName.value = ""
        _phoneNumber.value = ""
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
    
    fun setQuickActionEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QUICK_ACTION_ENABLED] = enabled } }
    }

    fun setQaAddJobEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_ADD_JOB] = enabled } }
    }

    fun setQaAddClientEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_ADD_CLIENT] = enabled } }
    }

    fun setQaAddPaymentEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_ADD_PAYMENT] = enabled } }
    }

    fun setQaAddExpenseEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_ADD_EXPENSE] = enabled } }
    }

    fun setQaReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_REMINDER] = enabled } }
    }

    fun setQaRateCardEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_RATE_CARD] = enabled } }
    }

    fun setQaInvoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_INVOICE] = enabled } }
    }

    fun setQaAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_ANALYTICS] = enabled } }
    }

    fun setQaNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_NOTIFICATIONS] = enabled } }
    }

    fun setQaProfileEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_PROFILE] = enabled } }
    }

    fun setQaSettingsEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_SETTINGS] = enabled } }
    }

    fun setQaTodoEnabled(enabled: Boolean) {
        viewModelScope.launch { context.settingsDataStore.edit { it[SettingsKeys.QA_TODO] = enabled } }
    }

    fun selectAllQuickActions(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit {
                it[SettingsKeys.QA_ADD_JOB] = enabled
                it[SettingsKeys.QA_ADD_CLIENT] = enabled
                it[SettingsKeys.QA_ADD_PAYMENT] = enabled
                it[SettingsKeys.QA_ADD_EXPENSE] = enabled
                it[SettingsKeys.QA_REMINDER] = enabled
                it[SettingsKeys.QA_RATE_CARD] = enabled
                it[SettingsKeys.QA_INVOICE] = enabled
                it[SettingsKeys.QA_ANALYTICS] = enabled
                it[SettingsKeys.QA_NOTIFICATIONS] = enabled
                it[SettingsKeys.QA_PROFILE] = enabled
                it[SettingsKeys.QA_SETTINGS] = enabled
                it[SettingsKeys.QA_TODO] = enabled
            }
        }
    }
}


