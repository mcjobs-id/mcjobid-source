package com.isankamil.mcjobid.ui.screen.wizard

import com.isankamil.mcjobid.domain.model.UserProfile

data class WizardState(
    val photoUri: String = "",
    val displayName: String = "",
    val stageName: String = "",
    val phoneNumber: String = "",
    val secondaryPhone: String = "",
    val email: String = "",
    val bio: String = "",
    val city: String = "",
    val areaCoverage: String = "",
    val specialization: String = "",
    val selectedSpecializations: Set<String> = emptySet(),
    val customSpecializations: List<String> = emptyList(),
    val languages: String = "",
    val experienceYears: String = "",
    val baseFeeText: String = "",
    val defaultDpPercentage: Int = 30,
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountHolder: String = "",
    val secondaryBankInfo: String = "",
    val npwpNumber: String = "",
    val instagramHandle: String = "",
    val termsAndConditions: String = ""
) {
    fun getCompletionPercentage(): Int {
        var score = 0
        if (photoUri.isNotBlank()) score += 10
        if (displayName.isNotBlank()) score += 15
        if (stageName.isNotBlank() || displayName.isNotBlank()) score += 10
        if (phoneNumber.isNotBlank()) score += 15
        if (city.isNotBlank()) score += 10
        if (selectedSpecializations.isNotEmpty()) score += 10
        if (baseFeeText.isNotBlank()) score += 15
        if (bankName.isNotBlank() && bankAccountNumber.isNotBlank()) score += 15
        if (instagramHandle.isNotBlank()) score += 5
        if (npwpNumber.isNotBlank()) score += 5
        return score.coerceAtMost(100)
    }
}

sealed class WizardUiState {
    data class Editing(val currentStep: Int = 1, val draft: WizardState = WizardState()) : WizardUiState()
    data class Saving(val currentStep: Int = 5, val draft: WizardState) : WizardUiState()
    data class Success(val profile: UserProfile) : WizardUiState()
    data class Error(val message: String, val currentStep: Int = 5, val draft: WizardState) : WizardUiState()
}
