package com.isankamil.mcjobid.ui.screen.testimonial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.util.Log
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.repository.TestimonialRepository
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.domain.model.Testimonial
import com.isankamil.mcjobid.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TestimonialViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: TestimonialRepository,
    private val userProfileRepository: UserProfileRepository,
    private val firebaseAuthService: com.isankamil.mcjobid.data.remote.firebase.FirebaseAuthService
) : ViewModel() {

    private val _testimonials = MutableStateFlow<List<Testimonial>>(emptyList())
    val testimonials: StateFlow<List<Testimonial>> = _testimonials.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _submitStatus = MutableStateFlow<Result<Unit>?>(null)
    val submitStatus: StateFlow<Result<Unit>?> = _submitStatus.asStateFlow()

    /** Developer Mode — hanya bisa diaktifkan dari dalam app via PIN rahasia */
    private val _isDeveloperMode = MutableStateFlow(false)
    val isDeveloperMode: StateFlow<Boolean> = _isDeveloperMode.asStateFlow()

    private val _deleteStatus = MutableStateFlow<String?>(null)
    val deleteStatus: StateFlow<String?> = _deleteStatus.asStateFlow()

    val currentUserId: String
        get() = firebaseAuthService.getCurrentUserId() ?: ""

    /** Ulasan milik user saat ini jika sudah pernah kirim (1 ulasan per akun) */
    val myTestimonial: StateFlow<Testimonial?> = _testimonials
        .map { list ->
            val uid = currentUserId
            if (uid.isNotBlank()) list.find { it.userId == uid } else null
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Job for the live observer — cancelled when ViewModel is cleared
    private var observerJob: Job? = null

    init {
        viewModelScope.launch {
            val uid = currentUserId
            if (uid.isNotBlank()) {
                val profile = userProfileRepository.getUserProfile(uid)
                _currentUserProfile.value = profile
            }
        }
        startObservingTestimonials()
    }

    /**
     * Starts a persistent real-time observer on the testimonials collection.
     * This Flow keeps running until the ViewModel is destroyed, pushing any
     * new testimonial (from any user on any device) directly into the UI state.
     */
    private fun startObservingTestimonials() {
        observerJob?.cancel()
        observerJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val uid = currentUserId
                repository.observeTestimonials(uid).collect { testimonialList ->
                    _testimonials.value = testimonialList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("TestimonialViewModel", "observeTestimonials error: ${e.message}")
                _errorMessage.value = context.getString(R.string.err_muat_testimoni, e.message)
                _isLoading.value = false
            }
        }
    }

    /**
     * Manual reload — called from UI "Coba Lagi" button on error state.
     * Restarts the live observer from scratch.
     */
    fun loadTestimonials() {
        startObservingTestimonials()
    }

    fun submitTestimonial(
        userName: String,
        rating: Int,
        review: String,
        suggestion: String,
        customPhotoUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            // Pastikan sesi Firebase Auth aktif agar token auth valid dan lolos Firebase Security Rules
            val authUid = firebaseAuthService.ensureAuthenticated() ?: currentUserId
            val uid = authUid.ifBlank { "mc_${UUID.randomUUID().toString().take(8)}" }
            val existing = myTestimonial.value
            val profile = _currentUserProfile.value ?: userProfileRepository.getUserProfile(uid)
            val finalPhotoUrl = customPhotoUrl ?: existing?.photoUrl ?: profile?.photoUrl ?: profile?.photoUri
            
            val testimonial = Testimonial(
                id = existing?.id ?: uid,
                userId = uid,
                userName = userName.trim(),
                avatarResId = existing?.avatarResId,
                photoUrl = finalPhotoUrl,
                rating = rating,
                review = review.trim(),
                suggestion = suggestion.trim(),
                createdAt = System.currentTimeMillis()
            )

            // Optimistic instant update in UI so user immediately sees their review at the top
            val currentList = _testimonials.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.userId == uid || (it.id.isNotBlank() && it.id == testimonial.id) }
            if (existingIndex >= 0) {
                currentList[existingIndex] = testimonial
            } else {
                currentList.add(0, testimonial)
            }
            _testimonials.value = currentList.distinctBy { it.id }.sortedWith(
                compareByDescending<Testimonial> { it.userId == uid }
                    .thenByDescending { !it.id.startsWith("seed_") }
                    .thenByDescending { it.createdAt }
            )

            val result = repository.addTestimonial(testimonial)
            _submitStatus.value = result
            _isLoading.value = false
        }
    }

    fun clearSubmitStatus() {
        _submitStatus.value = null
    }

    // ─── Developer Mode ────────────────────────────────────────────────────────

    /** Aktifkan Developer Mode setelah PIN diverifikasi di SettingsScreen */
    fun enterDeveloperMode() {
        _isDeveloperMode.value = true
    }

    /** Nonaktifkan Developer Mode saat panel ditutup */
    fun exitDeveloperMode() {
        _isDeveloperMode.value = false
        _deleteStatus.value = null
    }

    /**
     * Hapus testimoni dari Firestore (Developer Mode only).
     * Real-time listener otomatis update di semua device.
     */
    fun deleteTestimonial(id: String) {
        if (!_isDeveloperMode.value) return
        viewModelScope.launch {
            val result = repository.deleteTestimonial(id)
            _deleteStatus.value = if (result.isSuccess) "Testimoni berhasil dihapus"
                                  else "Gagal menghapus: ${result.exceptionOrNull()?.message}"
        }
    }

    fun clearDeleteStatus() {
        _deleteStatus.value = null
    }
}
