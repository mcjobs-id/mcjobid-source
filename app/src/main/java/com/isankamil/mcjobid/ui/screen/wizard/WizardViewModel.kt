package com.isankamil.mcjobid.ui.screen.wizard

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.util.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WizardViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<WizardUiState>(WizardUiState.Editing(currentStep = 1, draft = WizardState()))
    val uiState: StateFlow<WizardUiState> = _uiState.asStateFlow()

    private val _stepError = MutableStateFlow<String?>(null)
    val stepError: StateFlow<String?> = _stepError.asStateFlow()

    init {
        // Pre-fill existing user profile data
        viewModelScope.launch {
            val currentUserId = profileRepository.getCurrentUserId()
            val existing = profileRepository.fetchProfileFromFirestore(currentUserId) ?: profileRepository.getUserProfile(currentUserId)
            if (existing != null) {
                val specsSet = existing.specialization?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet()
                    ?: emptySet()

                _uiState.value = WizardUiState.Editing(
                    currentStep = 1,
                    draft = WizardState(
                        photoUri = existing.photoUri ?: "",
                        displayName = existing.displayName ?: "",
                        stageName = existing.stageName ?: "",
                        phoneNumber = existing.phoneNumber ?: "",
                        secondaryPhone = existing.secondaryPhone ?: "",
                        email = existing.email ?: profileRepository.getCurrentUserEmail() ?: "",
                        bio = existing.bio ?: "",
                        city = existing.city ?: "",
                        areaCoverage = existing.areaCoverage ?: "",
                        specialization = existing.specialization ?: "",
                        selectedSpecializations = specsSet,
                        languages = existing.languages ?: "",
                        experienceYears = existing.experienceYears ?: "",
                        baseFeeText = if (existing.baseFee > 0) existing.baseFee.toString() else "",
                        defaultDpPercentage = existing.defaultDpPercentage,
                        bankName = existing.bankName ?: "",
                        bankAccountNumber = existing.bankAccountNumber ?: "",
                        bankAccountHolder = existing.bankAccountHolder ?: "",
                        secondaryBankInfo = existing.secondaryBankInfo ?: "",
                        npwpNumber = existing.npwpNumber ?: "",
                        instagramHandle = existing.instagramHandle ?: "",
                        termsAndConditions = existing.termsAndConditions ?: ""
                    )
                )
            }
        }
    }

    fun updateDraft(transform: (WizardState) -> WizardState) {
        _stepError.value = null
        val currentState = _uiState.value
        if (currentState is WizardUiState.Editing) {
            _uiState.value = currentState.copy(draft = transform(currentState.draft))
        } else if (currentState is WizardUiState.Error) {
            _uiState.value = WizardUiState.Editing(currentStep = currentState.currentStep, draft = transform(currentState.draft))
        }
    }

    fun nextStep() {
        val currentState = _uiState.value
        val draft = getDraft(currentState) ?: return
        val step = getStep(currentState)

        _stepError.value = null

        when (step) {
            1 -> {
                if (draft.displayName.trim().length < 2) {
                    _stepError.value = "Nama Lengkap minimal 2 karakter."
                    return
                }
                if (draft.phoneNumber.isNotBlank() && !isValidPhoneNumber(draft.phoneNumber)) {
                    _stepError.value = "Nomor WhatsApp tidak valid (minimal 9 digit angka)."
                    return
                }
                _uiState.value = WizardUiState.Editing(currentStep = 2, draft = draft)
            }
            2 -> {
                if (draft.city.isBlank()) {
                    _stepError.value = "Kota Domisili wajib diisi."
                    return
                }
                _uiState.value = WizardUiState.Editing(currentStep = 3, draft = draft)
            }
            3 -> {
                _uiState.value = WizardUiState.Editing(currentStep = 4, draft = draft)
            }
            4 -> {
                if (draft.bankAccountNumber.isNotBlank() && !draft.bankAccountNumber.all { it.isDigit() || it == '-' || it == ' ' }) {
                    _stepError.value = "Nomor rekening harus berupa angka."
                    return
                }
                _uiState.value = WizardUiState.Editing(currentStep = 5, draft = draft)
            }
        }
    }

    fun prevStep() {
        val currentState = _uiState.value
        val draft = getDraft(currentState) ?: return
        val step = getStep(currentState)
        _stepError.value = null

        if (step > 1) {
            _uiState.value = WizardUiState.Editing(currentStep = step - 1, draft = draft)
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val draft = getDraft(currentState) ?: return

        if (draft.displayName.trim().length < 2) {
            _uiState.value = WizardUiState.Error(
                message = "Nama Lengkap wajib diisi.",
                currentStep = 1,
                draft = draft
            )
            return
        }

        _uiState.value = WizardUiState.Saving(currentStep = 5, draft = draft)

        viewModelScope.launch {
            val uid = profileRepository.getCurrentUserId()
            val baseFeeValue = draft.baseFeeText.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val specsJoined = draft.selectedSpecializations.joinToString(", ")

            // Upload foto profil ke Firebase Storage → URL publik yang persistent
            val photoCloudUrl = if (draft.photoUri.isBlank()) {
                null
            } else if (draft.photoUri.startsWith("https://")) {
                draft.photoUri
            } else {
                com.isankamil.mcjobid.util.FirebaseStorageHelper.uploadProfilePhoto(
                    context = context,
                    userId = uid,
                    sourceUri = draft.photoUri
                ) ?: run {
                    // Fallback ke internal storage jika tidak ada internet
                    FileUtil.saveImageToInternalStorage(context, draft.photoUri.toUri()) ?: draft.photoUri
                }
            }

            val userProfile = UserProfile(
                userId = uid,
                photoUri = photoCloudUrl?.trim()?.ifEmpty { null },
                photoUrl = if (photoCloudUrl?.startsWith("https://") == true) photoCloudUrl else null,
                displayName = draft.displayName.trim(),
                stageName = draft.stageName.trim().ifEmpty { draft.displayName.trim() },
                phoneNumber = draft.phoneNumber.trim().ifEmpty { null },
                secondaryPhone = draft.secondaryPhone.trim().ifEmpty { null },
                email = draft.email.trim().ifEmpty { null },
                bio = draft.bio.trim().ifEmpty { null },
                city = draft.city.trim().ifEmpty { null },
                areaCoverage = draft.areaCoverage.trim().ifEmpty { null },
                specialization = specsJoined.ifEmpty { "Wedding" },
                languages = draft.languages.trim().ifEmpty { null },
                experienceYears = draft.experienceYears.trim().ifEmpty { null },
                baseFee = baseFeeValue,
                defaultDpPercentage = draft.defaultDpPercentage,
                bankName = draft.bankName.trim().ifEmpty { null },
                bankAccountNumber = draft.bankAccountNumber.trim().ifEmpty { null },
                bankAccountHolder = draft.bankAccountHolder.trim().ifEmpty { null },
                secondaryBankInfo = draft.secondaryBankInfo.trim().ifEmpty { null },
                npwpNumber = draft.npwpNumber.trim().ifEmpty { null },
                instagramHandle = draft.instagramHandle.trim().ifEmpty { null },
                termsAndConditions = draft.termsAndConditions.trim().ifEmpty { null },
                profileCompleted = true
            )

            val result = profileRepository.saveProfileToFirebase(userProfile)
            if (result.isSuccess) {
                _uiState.value = WizardUiState.Success(userProfile)
            } else {
                val error = result.exceptionOrNull()
                val message = mapFirebaseError(error)
                _uiState.value = WizardUiState.Error(
                    message = message,
                    currentStep = 5,
                    draft = draft
                )
            }
        }
    }

    private fun getDraft(state: WizardUiState): WizardState? {
        return when (state) {
            is WizardUiState.Editing -> state.draft
            is WizardUiState.Saving -> state.draft
            is WizardUiState.Error -> state.draft
            is WizardUiState.Success -> null
        }
    }

    private fun getStep(state: WizardUiState): Int {
        return when (state) {
            is WizardUiState.Editing -> state.currentStep
            is WizardUiState.Saving -> state.currentStep
            is WizardUiState.Error -> state.currentStep
            is WizardUiState.Success -> 5
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val cleaned = phone.replace("[^0-9+]".toRegex(), "")
        return cleaned.length in 9..15
    }

    private fun mapFirebaseError(throwable: Throwable?): String {
        if (throwable == null) return "Profil belum tersimpan. Coba lagi."
        val msg = throwable.message?.lowercase() ?: ""
        return when {
            msg.contains("permission") || msg.contains("denied") -> "Kamu tidak memiliki izin untuk menyimpan profil."
            msg.contains("network") || msg.contains("unavailable") || msg.contains("connect") -> "Tidak ada koneksi internet. Profil belum tersimpan."
            msg.contains("timeout") -> "Server belum merespons. Coba lagi."
            else -> "Profil belum dapat disimpan (${throwable.localizedMessage ?: "Error"}). Coba lagi."
        }
    }
}
