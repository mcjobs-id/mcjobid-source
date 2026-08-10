package com.isankamil.mcjobid.ui.screen.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.remote.firebase.FirebaseAuthService
import com.isankamil.mcjobid.data.repository.SyncManager
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.isankamil.mcjobid.R
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val userProfileRepository: UserProfileRepository,
    private val syncManager: SyncManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(firebaseAuthService.isUserLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow(firebaseAuthService.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()
    
    private val _hasSeenOnboarding = MutableStateFlow(false)
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()
    
    init {
        checkAuthState()
        checkOnboardingStatus()
    }
    
    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                _hasSeenOnboarding.value = preferences[ONBOARDING_COMPLETED_KEY] ?: false
            }
        }
    }
    
    fun setOnboardingSeen() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED_KEY] = true
            }
            _hasSeenOnboarding.value = true
        }
    }
    
    fun hasSeenOnboarding(): Boolean {
        return _hasSeenOnboarding.value
    }
    
    private fun checkAuthState() {
        _isLoggedIn.value = firebaseAuthService.isUserLoggedIn
        _currentUser.value = firebaseAuthService.currentUser
    }
    
    private val _registrationSuccessMessage = MutableStateFlow<String?>(null)
    val registrationSuccessMessage: StateFlow<String?> = _registrationSuccessMessage.asStateFlow()

    fun clearRegistrationSuccessMessage() {
        _registrationSuccessMessage.value = null
    }

    private fun isValidStrictEmail(email: String): Boolean {
        val clean = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches()) return false
        val domain = clean.substringAfter('@', "")
        if (domain.isBlank() || !domain.contains('.')) return false
        val tld = domain.substringAfterLast('.', "")
        if (tld.length < 2) return false
        return true
    }

    fun loginWithEmail(email: String, pass: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            _errorMessage.value = context.getString(R.string.err_field_wajib)
            return
        }
        if (!isValidStrictEmail(cleanEmail)) {
            _errorMessage.value = context.getString(R.string.err_email_invalid)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = firebaseAuthService.signInWithEmail(cleanEmail, pass)
                result.fold(
                    onSuccess = { user ->
                        // Always fetch fresh profile from Firestore on login
                        // to ensure photoUri/photoUrl is always up-to-date after reinstall
                        try {
                            val remoteProfile = userProfileRepository.fetchProfileFromFirestore(user.uid)
                            if (remoteProfile == null) {
                                // fallback: cek lokal jika Firestore tidak menemukan dokumen
                                userProfileRepository.getUserProfile(user.uid)
                            }
                        } catch (e: Exception) {
                            // Offline — fallback ke local cache jika ada
                            try { userProfileRepository.getUserProfile(user.uid) } catch (_: Exception) {}
                        }
                        
                        _isLoggedIn.value = true
                        _currentUser.value = user
                    },
                    onFailure = { error ->
                        _isLoggedIn.value = false
                        _errorMessage.value = mapAuthError(error)
                    }
                )
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _errorMessage.value = mapAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerWithEmail(email: String, pass: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            _errorMessage.value = context.getString(R.string.err_field_wajib)
            return
        }
        if (!isValidStrictEmail(cleanEmail)) {
            _errorMessage.value = context.getString(R.string.err_email_invalid)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = firebaseAuthService.signUpWithEmail(cleanEmail, pass)
                result.fold(
                    onSuccess = { user ->
                        try {
                            userProfileRepository.createOrUpdateUserProfile(
                                userId = user.uid,
                                name = user.displayName ?: cleanEmail.substringBefore("@"),
                                email = user.email ?: cleanEmail,
                                profileCompleted = false
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("AuthViewModel", "Profile creation warning: ${e.message}")
                        }

                        // Direct access upon creation as requested by user
                        _isLoggedIn.value = true
                        _currentUser.value = user
                    },
                    onFailure = { error ->
                        _isLoggedIn.value = false
                        _errorMessage.value = mapAuthError(error, isRegister = true)
                    }
                )
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _errorMessage.value = mapAuthError(e, isRegister = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userProfileRepository.clearSession()
                firebaseAuthService.signOut()
                _isLoggedIn.value = false
                _currentUser.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.err_logout_gagal, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() = signOut()

    private fun mapAuthError(throwable: Throwable?, isRegister: Boolean = false): String {
        if (throwable == null) return context.getString(R.string.err_generik)
        val msg = throwable.message?.lowercase() ?: ""
        
        if (isRegister) {
            return when {
                throwable is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                msg.contains("email-already-in-use") || msg.contains("already in use") || msg.contains("already exists") || msg.contains("collision") ->
                    context.getString(R.string.err_email_terdaftar)
                throwable is com.google.firebase.auth.FirebaseAuthWeakPasswordException ||
                msg.contains("weak-password") || msg.contains("weak password") || msg.contains("at least 6 characters") ->
                    context.getString(R.string.err_password_lemah)
                msg.contains("badly formatted") || msg.contains("invalid-email") ->
                    context.getString(R.string.err_email_invalid)
                msg.contains("network") || msg.contains("timeout") || msg.contains("connect") ->
                    context.getString(R.string.err_jaringan)
                else -> context.getString(R.string.err_pendaftaran_gagal)
            }
        }

        return when {
            throwable is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
            msg.contains("email-already-in-use") || msg.contains("already in use") || msg.contains("already exists") ->
                context.getString(R.string.err_email_terdaftar)
            msg.contains("user-not-found") || msg.contains("no user record") ->
                context.getString(R.string.err_akun_tidak_ditemukan)
            msg.contains("badly formatted") || msg.contains("invalid-email") ->
                context.getString(R.string.err_email_invalid)
            msg.contains("password") || msg.contains("credential") || msg.contains("incorrect") || msg.contains("malformed") || msg.contains("expired") || msg.contains("wrong") ->
                context.getString(R.string.err_password_salah)
            msg.contains("network") || msg.contains("timeout") || msg.contains("connect") ->
                context.getString(R.string.err_jaringan)
            msg.contains("too-many-requests") ->
                context.getString(R.string.err_terlalu_banyak_percobaan)
            msg.contains("user-disabled") ->
                context.getString(R.string.err_akun_dinonaktifkan)
            else -> context.getString(R.string.err_auth_gagal)
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }

    fun loginError(message: String) {
        _errorMessage.value = message
    }

    private val _resetPasswordStatus = MutableStateFlow<String?>(null)
    val resetPasswordStatus: StateFlow<String?> = _resetPasswordStatus.asStateFlow()

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            _resetPasswordStatus.value = context.getString(R.string.err_reset_email_kosong)
            return
        }
        if (!isValidStrictEmail(cleanEmail)) {
            _resetPasswordStatus.value = context.getString(R.string.err_email_invalid)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = firebaseAuthService.sendPasswordReset(cleanEmail)
                result.fold(
                    onSuccess = {
                        _resetPasswordStatus.value = context.getString(R.string.status_reset_terkirim, cleanEmail)
                    },
                    onFailure = {
                        _resetPasswordStatus.value = context.getString(R.string.err_reset_gagal, mapAuthError(it))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResetPasswordStatus() {
        _resetPasswordStatus.value = null
    }

    private val _confirmResetStatus = MutableStateFlow<Result<String>?>(null)
    val confirmResetStatus: StateFlow<Result<String>?> = _confirmResetStatus.asStateFlow()

    fun confirmPasswordReset(oobCode: String, newPass: String) {
        val cleanPass = newPass.trim()
        if (cleanPass.isBlank() || cleanPass.length < 6) {
            _errorMessage.value = context.getString(R.string.err_password_lemah)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = firebaseAuthService.confirmPasswordReset(oobCode, cleanPass)
                result.fold(
                    onSuccess = {
                        _confirmResetStatus.value = Result.success("Kata sandi berhasil diperbarui! Silakan masuk dengan kata sandi baru Anda.")
                    },
                    onFailure = { error ->
                        _errorMessage.value = "Gagal memperbarui kata sandi: ${error.message}"
                        _confirmResetStatus.value = Result.failure(error)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearConfirmResetStatus() {
        _confirmResetStatus.value = null
    }
    
    fun isUserLoggedIn(): Boolean {
        return firebaseAuthService.isUserLoggedIn
    }
}
