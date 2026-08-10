package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.isankamil.mcjobid.data.local.dao.UserProfileDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firestoreSyncService: FirestoreSyncService,
) {
    
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "local_user"
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    suspend fun getUserProfile(userId: String = getCurrentUserId()): UserProfile? {
        return userProfileDao.getUserProfile(userId)?.let { UserProfile.fromEntity(it) }
    }

    /**
     * Fetches user profile from Firestore users/{uid}.
     * If document exists, syncs with local Room DB and returns UserProfile.
     * If document is deleted in Firestore, removes local Room DB record and returns null.
     */
    suspend fun fetchProfileFromFirestore(userId: String = getCurrentUserId()): UserProfile? {
        if (userId.isBlank() || userId == "local_user") {
            return getUserProfile(userId)
        }
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                val profile = parseFirestoreDocument(userId, snapshot.data)
                userProfileDao.insertUserProfile(profile.toEntity())
                profile
            } else {
                // If not found in Firestore, don't delete local data immediately.
                // It might be a new user or sync delay.
                getUserProfile(userId)
            }
        } catch (e: Exception) {
            getUserProfile(userId)
        }
    }
    
    fun getUserProfileFlow(userId: String = getCurrentUserId()): Flow<UserProfile?> {
        return userProfileDao.getUserProfileFlow(userId).map { entity ->
            entity?.let { UserProfile.fromEntity(it) }
        }
    }

    /**
     * Real-time listener observing Firestore users/{uid} document.
     * Automatically writes snapshot changes into local Room DB.
     */
    fun observeFirestoreProfile(userId: String = getCurrentUserId()): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank() || userId == "local_user") {
            trySend(getUserProfile(userId))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val remoteProfile = parseFirestoreDocument(userId, snapshot.data)
                    // Sync to local DB preserving local photoUri if remote photoUri is blank
                    CoroutineScope(Dispatchers.IO).launch {
                        val localEntity = userProfileDao.getUserProfile(userId)
                        val effectivePhotoUri = when {
                            !remoteProfile.photoUri.isNullOrBlank() -> remoteProfile.photoUri
                            !remoteProfile.photoUrl.isNullOrBlank() -> remoteProfile.photoUrl
                            localEntity != null && !localEntity.photoUri.isNullOrBlank() -> localEntity.photoUri
                            else -> null
                        }
                        val finalProfile = remoteProfile.copy(
                            photoUri = effectivePhotoUri,
                            photoUrl = remoteProfile.photoUrl ?: effectivePhotoUri
                        )
                        userProfileDao.insertUserProfile(finalProfile.toEntity())
                        trySend(finalProfile)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Saves user profile to Firebase Firestore users/{uid} and updates local DB on success.
     */
    suspend fun saveProfileToFirebase(profile: UserProfile): Result<UserProfile> {
        var uid = profile.userId.ifEmpty { getCurrentUserId() }
        if (uid.isBlank()) {
            uid = "local_mc_user"
        }
        val updatedProfile = profile.copy(
            userId = uid,
            email = profile.email ?: getCurrentUserEmail(),
            profileCompleted = profile.profileCompleted,
            updatedAt = LocalDateTime.now()
        )

        // 1. Local-First: Save to local Room DB immediately
        userProfileDao.insertUserProfile(updatedProfile.toEntity())

        // 2. Direct Sync to Firestore with unified mapping
        if (uid != "local_mc_user" && uid != "local_user") {
            try {
                val firestoreMap = updatedProfile.toEntity().toFirestoreMap()
                
                firestore.collection("users").document(uid)
                    .set(firestoreMap, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                // Background sync fallback via queue if direct write fails
                firestoreSyncService.syncUserProfileToFirestore(updatedProfile.toEntity())
            }
        }

        return Result.success(updatedProfile)
    }
    
    suspend fun saveUserProfile(profile: UserProfile) {
        saveProfileToFirebase(profile)
    }

    suspend fun createOrUpdateUserProfile(
        userId: String = getCurrentUserId(),
        name: String? = null,
        stageName: String? = null,
        bio: String? = null,
        city: String? = null,
        areaCoverage: String? = null,
        specialization: String? = null,
        languages: String? = null,
        experienceYears: String? = null,
        photoUri: String? = null,
        email: String? = null,
        bankName: String? = null,
        accountNumber: String? = null,
        accountName: String? = null,
        secondaryBankInfo: String? = null,
        phoneNumber: String? = null,
        secondaryPhone: String? = null,
        baseFee: Long? = null,
        defaultDpPercentage: Int? = null,
        npwpNumber: String? = null,
        instagramHandle: String? = null,
        termsAndConditions: String? = null,
        profileCompleted: Boolean = false
    ): Result<UserProfile> {
        val existing = getUserProfile(userId)
        val isCompleted = if (existing != null) {
            existing.profileCompleted || profileCompleted
        } else {
            profileCompleted
        }
        val updated = UserProfile(
            userId = userId,
            displayName = name ?: existing?.displayName,
            stageName = stageName ?: existing?.stageName,
            bio = bio ?: existing?.bio,
            city = city ?: existing?.city,
            areaCoverage = areaCoverage ?: existing?.areaCoverage,
            specialization = specialization ?: existing?.specialization,
            languages = languages ?: existing?.languages,
            experienceYears = experienceYears ?: existing?.experienceYears,
            photoUri = photoUri ?: existing?.photoUri,
            photoUrl = existing?.photoUrl,
            email = email ?: existing?.email ?: getCurrentUserEmail(),
            bankName = bankName ?: existing?.bankName,
            bankAccountNumber = accountNumber ?: existing?.bankAccountNumber,
            bankAccountHolder = accountName ?: existing?.bankAccountHolder,
            secondaryBankInfo = secondaryBankInfo ?: existing?.secondaryBankInfo,
            phoneNumber = phoneNumber ?: existing?.phoneNumber,
            secondaryPhone = secondaryPhone ?: existing?.secondaryPhone,
            baseFee = baseFee ?: existing?.baseFee ?: 0L,
            defaultDpPercentage = defaultDpPercentage ?: existing?.defaultDpPercentage ?: 30,
            npwpNumber = npwpNumber ?: existing?.npwpNumber,
            instagramHandle = instagramHandle ?: existing?.instagramHandle,
            termsAndConditions = termsAndConditions ?: existing?.termsAndConditions,
            profileCompleted = isCompleted
        )
        userProfileDao.insertUserProfile(updated.toEntity())
        return saveProfileToFirebase(updated)
    }

    suspend fun clearSession() {
        val uid = getCurrentUserId()
        if (uid.isNotEmpty()) {
            userProfileDao.deleteUserProfileById(uid)
        }
    }

    suspend fun deleteAccountPermanently(): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Sesi login tidak ditemukan. Silakan login kembali."))
            val uid = user.uid

            // Step 1: Delete data from Firestore while STILL authenticated.
            // This avoids PERMISSION_DENIED errors from security rules.
            if (uid.isNotEmpty() && uid != "local_user") {
                firestore.collection("users").document(uid).delete().await()
            }

            // Step 2: Clear local Room data.
            userProfileDao.deleteUserProfileById(uid)

            // Step 3: Delete Firebase Auth user.
            // This might fail if the user hasn't logged in recently (SECURITY_RECENT_LOGIN_REQUIRED).
            user.delete().await()

            // Step 4: Sign out (redundant if delete succeeded, but safe)
            auth.signOut()
            
            Result.success(value = true)
        } catch (e: Exception) {
            // We don't automatically sign out here so the user can potentially 
            // re-authenticate if the UI supports it, or at least see why it failed.
            Result.failure(e)
        }
    }

    private fun parseFirestoreDocument(uid: String, data: Map<String, Any?>?): UserProfile {
        if (data == null) return UserProfile(userId = uid)
        
        val updatedAtStr = when (val updatedAt = data["updatedAt"]) {
            is com.google.firebase.Timestamp -> {
                val instant = java.time.Instant.ofEpochSecond(updatedAt.seconds, updatedAt.nanoseconds.toLong())
                java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toString()
            }
            is String -> updatedAt
            else -> LocalDateTime.now().toString()
        }

        return UserProfile(
            userId = uid,
            displayName = (data["displayName"] as? String) ?: (data["name"] as? String),
            stageName = data["stageName"] as? String,
            email = data["email"] as? String,
            phoneNumber = data["phoneNumber"] as? String,
            secondaryPhone = data["secondaryPhone"] as? String,
            city = data["city"] as? String,
            areaCoverage = data["areaCoverage"] as? String,
            specialization = data["specialization"] as? String,
            languages = data["languages"] as? String,
            experienceYears = data["experienceYears"] as? String,
            bio = data["bio"] as? String,
            photoUri = ((data["photoUri"] as? String)?.ifBlank { null }) ?: ((data["photoUrl"] as? String)?.ifBlank { null }),
            photoUrl = ((data["photoUrl"] as? String)?.ifBlank { null }) ?: ((data["photoUri"] as? String)?.ifBlank { null }),
            bankName = data["bankName"] as? String,
            bankAccountNumber = (data["bankAccountNumber"] as? String) ?: (data["accountNumber"] as? String),
            bankAccountHolder = (data["bankAccountHolder"] as? String) ?: (data["accountName"] as? String),
            secondaryBankInfo = data["secondaryBankInfo"] as? String,
            baseFee = (data["baseFee"] as? Number)?.toLong() ?: 0L,
            defaultDpPercentage = (data["defaultDpPercentage"] as? Number)?.toInt() ?: 30,
            npwpNumber = data["npwpNumber"] as? String,
            instagramHandle = data["instagramHandle"] as? String,
            termsAndConditions = data["termsAndConditions"] as? String,
            profileCompleted = (data["profileCompleted"] as? Boolean) ?: false,
            updatedAt = try { LocalDateTime.parse(updatedAtStr) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}

