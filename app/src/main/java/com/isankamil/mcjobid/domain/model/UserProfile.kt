package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.UserProfileEntity
import java.time.LocalDateTime

data class UserProfile(
    val userId: String,
    val displayName: String? = null,
    val stageName: String? = null,
    val bio: String? = null,
    val city: String? = null,
    val areaCoverage: String? = null,
    val specialization: String? = null,
    val languages: String? = null,
    val experienceYears: String? = null,
    val photoUri: String? = null,
    val photoUrl: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val secondaryPhone: String? = null,
    val bankName: String? = null,
    val bankAccountNumber: String? = null,
    val bankAccountHolder: String? = null,
    val secondaryBankInfo: String? = null,
    val baseFee: Long = 0L,
    val defaultDpPercentage: Int = 30,
    val npwpNumber: String? = null,
    val instagramHandle: String? = null,
    val termsAndConditions: String? = null,
    val profileCompleted: Boolean = false,
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    val name: String? get() = displayName // Alias for backward compatibility
    val accountNumber: String? get() = bankAccountNumber // Alias for backward compatibility
    val accountName: String? get() = bankAccountHolder // Alias for backward compatibility

    fun isProfileComplete(): Boolean = profileCompleted && !displayName.isNullOrBlank()

    fun toEntity() = UserProfileEntity(
        userId = userId,
        name = displayName,
        stageName = stageName,
        bio = bio,
        city = city,
        areaCoverage = areaCoverage,
        specialization = specialization,
        languages = languages,
        experienceYears = experienceYears,
        photoUri = photoUri,
        photoUrl = photoUrl ?: photoUri,
        email = email,
        phoneNumber = phoneNumber,
        secondaryPhone = secondaryPhone,
        bankName = bankName,
        accountNumber = bankAccountNumber,
        accountName = bankAccountHolder,
        secondaryBankInfo = secondaryBankInfo,
        baseFee = baseFee,
        defaultDpPercentage = defaultDpPercentage,
        npwpNumber = npwpNumber,
        instagramHandle = instagramHandle,
        termsAndConditions = termsAndConditions,
        profileCompleted = profileCompleted,
        updatedAt = updatedAt.toString()
    )
    
    companion object {
        fun fromEntity(entity: UserProfileEntity) = UserProfile(
            userId = entity.userId,
            displayName = entity.name,
            stageName = entity.stageName,
            bio = entity.bio,
            city = entity.city,
            areaCoverage = entity.areaCoverage,
            specialization = entity.specialization,
            languages = entity.languages,
            experienceYears = entity.experienceYears,
            photoUri = entity.photoUri,
            photoUrl = entity.photoUrl,
            email = entity.email,
            phoneNumber = entity.phoneNumber,
            secondaryPhone = entity.secondaryPhone,
            bankName = entity.bankName,
            bankAccountNumber = entity.accountNumber,
            bankAccountHolder = entity.accountName,
            secondaryBankInfo = entity.secondaryBankInfo,
            baseFee = entity.baseFee,
            defaultDpPercentage = entity.defaultDpPercentage,
            npwpNumber = entity.npwpNumber,
            instagramHandle = entity.instagramHandle,
            termsAndConditions = entity.termsAndConditions,
            profileCompleted = entity.profileCompleted,
            updatedAt = try { LocalDateTime.parse(entity.updatedAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
