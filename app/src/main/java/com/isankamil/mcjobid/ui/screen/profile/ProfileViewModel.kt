package com.isankamil.mcjobid.ui.screen.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ClientRepository
import com.isankamil.mcjobid.data.repository.SyncManager
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.util.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.isankamil.mcjobid.util.Constants

data class ProfileStats(
    val totalEvents: Int = 0,
    val totalClients: Int = 0,
    val totalRevenue: Long = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val bookingRepository: BookingRepository,
    private val clientRepository: ClientRepository,
    private val syncManager: SyncManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _stats = MutableStateFlow(ProfileStats())
    val stats: StateFlow<ProfileStats> = _stats.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val uid = userProfileRepository.getCurrentUserId()
            userProfileRepository.getUserProfileFlow(uid).collect { profile ->
                _userProfile.value = profile ?: UserProfile(userId = uid)
            }
        }

        viewModelScope.launch {
            combine(
                bookingRepository.getAllBookings(),
                clientRepository.getAllClients()
            ) { bookings, clients ->
                ProfileStats(
                    totalEvents = bookings.count { it.status != com.isankamil.mcjobid.domain.model.Booking.BookingStatus.CANCELLED },
                    totalClients = clients.size,
                    totalRevenue = bookings.filter { it.status != com.isankamil.mcjobid.domain.model.Booking.BookingStatus.CANCELLED }.sumOf { it.fee }
                )
            }.collect { s ->
                _stats.value = s
            }
        }
    }

    fun saveProfile(
        name: String,
        stageName: String? = null,
        bio: String,
        city: String,
        areaCoverage: String? = null,
        specialization: String,
        languages: String? = null,
        experienceYears: String? = null,
        phone: String,
        secondaryPhone: String? = null,
        email: String,
        bankName: String,
        accountNumber: String,
        accountName: String,
        secondaryBankInfo: String? = null,
        baseFee: Long = 0L,
        defaultDpPercentage: Int = 30,
        npwpNumber: String? = null,
        instagramHandle: String? = null,
        termsAndConditions: String? = null
    ) {
        viewModelScope.launch {
            val result = userProfileRepository.createOrUpdateUserProfile(
                name = name.trim().ifEmpty { null },
                stageName = stageName?.trim()?.ifEmpty { null },
                bio = bio.trim().ifEmpty { null },
                city = city.trim().ifEmpty { null },
                areaCoverage = areaCoverage?.trim()?.ifEmpty { null },
                specialization = specialization.trim().ifEmpty { null },
                languages = languages?.trim()?.ifEmpty { null },
                experienceYears = experienceYears?.trim()?.ifEmpty { null },
                phoneNumber = phone.trim().ifEmpty { null },
                secondaryPhone = secondaryPhone?.trim()?.ifEmpty { null },
                email = email.trim().ifEmpty { null },
                bankName = bankName.trim().ifEmpty { null },
                accountNumber = accountNumber.trim().ifEmpty { null },
                accountName = accountName.trim().ifEmpty { null },
                secondaryBankInfo = secondaryBankInfo?.trim()?.ifEmpty { null },
                baseFee = baseFee,
                defaultDpPercentage = defaultDpPercentage,
                npwpNumber = npwpNumber?.trim()?.ifEmpty { null },
                instagramHandle = instagramHandle?.trim()?.ifEmpty { null },
                termsAndConditions = termsAndConditions?.trim()?.ifEmpty { null },
                profileCompleted = true
            )

            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
                _statusMessage.value = "Profil MC & Rekening berhasil diperbarui!"
            } else {
                val error = result.exceptionOrNull()?.message ?: "Terjadi kesalahan saat menyimpan."
                _statusMessage.value = "Gagal menyimpan: $error. Data tetap tersimpan di lokal."
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
                    FileUtil.saveImageToInternalStorage(context, android.net.Uri.parse(photoUri)) ?: photoUri
                }
            }

            val updated = current.copy(
                photoUri = cloudUrl,
                photoUrl = if (cloudUrl.startsWith("https://")) cloudUrl else current.photoUrl
            )
            val result = userProfileRepository.saveProfileToFirebase(updated)

            if (result.isSuccess) {
                _userProfile.value = updated
                _statusMessage.value = "Foto profil berhasil diperbarui!"
            } else {
                _statusMessage.value = "Gagal menyinkronkan foto ke server, tetapi foto lokal diperbarui."
                _userProfile.value = updated
            }
        }
    }

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deletionError = MutableStateFlow<String?>(null)
    val deletionError: StateFlow<String?> = _deletionError.asStateFlow()

    private val _isAccountDeleted = MutableStateFlow(false)
    val isAccountDeleted: StateFlow<Boolean> = _isAccountDeleted.asStateFlow()

    fun deleteAccountPermanently() {
        viewModelScope.launch {
            _isDeleting.value = true
            _deletionError.value = null
            
            val result = userProfileRepository.deleteAccountPermanently()
            
            _isDeleting.value = false
            if (result.isSuccess) {
                _isAccountDeleted.value = true
            } else {
                val error = result.exceptionOrNull()
                _deletionError.value = when {
                    error is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
                    error?.message?.contains("recent authentication", ignoreCase = true) == true ||
                    error?.message?.contains("sensitive", ignoreCase = true) == true ||
                    error?.message?.contains("Log in again", ignoreCase = true) == true ||
                    error?.message?.contains("RECENT_LOGIN", ignoreCase = true) == true || 
                    error?.message?.contains("re-authenticate", ignoreCase = true) == true -> 
                        "Demi keamanan akun, proses ini memerlukan verifikasi sesi login terbaru. Silakan Keluar (Logout) terlebih dahulu, lalu Login kembali untuk melanjutkan penghapusan akun."
                    error?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                        "Gagal menghapus data di server. Silakan coba login ulang dan ulangi proses."
                    else -> error?.message ?: "Gagal menghapus akun. Silakan coba lagi nanti."
                }
            }
        }
    }

    fun clearDeletionError() {
        _deletionError.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
