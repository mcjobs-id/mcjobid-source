package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    @get:PropertyName("userId")
    val userId: String = "",
    
    @get:PropertyName("displayName")
    val name: String? = null,
    
    @get:PropertyName("stageName")
    val stageName: String? = null,
    
    @get:PropertyName("bio")
    val bio: String? = null,
    
    @get:PropertyName("city")
    val city: String? = null,
    
    @get:PropertyName("areaCoverage")
    val areaCoverage: String? = null,
    
    @get:PropertyName("specialization")
    val specialization: String? = null,
    
    @get:PropertyName("languages")
    val languages: String? = null,
    
    @get:PropertyName("experienceYears")
    val experienceYears: String? = null,
    
    @get:PropertyName("photoUri")
    val photoUri: String? = null,
    
    @get:PropertyName("photoUrl")
    val photoUrl: String? = null,
    
    @get:PropertyName("email")
    val email: String? = null,
    
    @get:PropertyName("phoneNumber")
    val phoneNumber: String? = null,
    
    @get:PropertyName("secondaryPhone")
    val secondaryPhone: String? = null,
    
    @get:PropertyName("bankName")
    val bankName: String? = null,
    
    @get:PropertyName("bankAccountNumber")
    val accountNumber: String? = null,
    
    @get:PropertyName("bankAccountHolder")
    val accountName: String? = null,
    
    @get:PropertyName("secondaryBankInfo")
    val secondaryBankInfo: String? = null,
    
    @get:PropertyName("baseFee")
    val baseFee: Long = 0L,
    
    @get:PropertyName("defaultDpPercentage")
    val defaultDpPercentage: Int = 30,
    
    @get:PropertyName("npwpNumber")
    val npwpNumber: String? = null,
    
    @get:PropertyName("instagramHandle")
    val instagramHandle: String? = null,
    
    @get:PropertyName("termsAndConditions")
    val termsAndConditions: String? = null,
    
    @get:PropertyName("profileCompleted")
    val profileCompleted: Boolean = false,
    
    @get:PropertyName("updatedAt")
    val updatedAt: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "displayName" to name,
            "stageName" to stageName,
            "bio" to bio,
            "city" to city,
            "areaCoverage" to areaCoverage,
            "specialization" to specialization,
            "languages" to languages,
            "experienceYears" to experienceYears,
            "photoUri" to photoUri,
            "photoUrl" to (photoUrl ?: photoUri),
            "email" to email,
            "phoneNumber" to phoneNumber,
            "secondaryPhone" to secondaryPhone,
            "bankName" to bankName,
            "bankAccountNumber" to accountNumber,
            "bankAccountHolder" to accountName,
            "secondaryBankInfo" to secondaryBankInfo,
            "baseFee" to baseFee,
            "defaultDpPercentage" to defaultDpPercentage,
            "npwpNumber" to npwpNumber,
            "instagramHandle" to instagramHandle,
            "termsAndConditions" to termsAndConditions,
            "profileCompleted" to profileCompleted,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
    }
}

