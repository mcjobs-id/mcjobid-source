package com.isankamil.mcjobid.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth
) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Memastikan ada sesi Firebase Auth aktif (baik login email maupun sesi anonim).
     * Dibutuhkan agar request ke Firestore memenuhi aturan Security Rules isAuthenticated().
     */
    suspend fun ensureAuthenticated(): String? {
        val current = auth.currentUser
        if (current != null) return current.uid
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuthService", "ensureAuthenticated error: ${e.message}", e)
            null
        }
    }

    val authStateFlow: Flow<com.isankamil.mcjobid.domain.model.AuthUiState> = kotlinx.coroutines.flow.callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(com.isankamil.mcjobid.domain.model.AuthUiState.Authenticated(user.uid, user.email))
            } else {
                trySend(com.isankamil.mcjobid.domain.model.AuthUiState.Unauthenticated)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Akun tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Gagal membuat akun"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmPasswordReset(oobCode: String, newPass: String): Result<Unit> {
        return try {
            auth.confirmPasswordReset(oobCode, newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordResetCode(oobCode: String): Result<String> {
        return try {
            val email = auth.verifyPasswordResetCode(oobCode).await()
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun reloadUser(): Result<Unit> {
        return try {
            auth.currentUser?.reload()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
    
    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}

