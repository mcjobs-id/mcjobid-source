package com.isankamil.mcjobid.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Testimonial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class TestimonialRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestoreSyncService: FirestoreSyncService
) {
    private val collection = firestore.collection("testimonials")

    private fun dateOf(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 10, 30, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getSeedTestimonials(): List<Testimonial> {
        return emptyList()
    }

    /**
     * One-shot fetch — used as an emergency fallback.
     * For live updates, use observeTestimonials() instead.
     */
    suspend fun getTestimonials(): Result<List<Testimonial>> {
        val seed = getSeedTestimonials()
        return try {
            val snapshot = collection.get().await()
            val remoteList = snapshot.documents.mapNotNull { doc ->
                try {
                    val item = doc.toObject(Testimonial::class.java)
                    item?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            // Real user reviews (remoteList) ALWAYS placed first (newest to oldest)
            val sortedRemote = remoteList.sortedByDescending { it.createdAt }
            val sortedSeed = seed.sortedByDescending { it.createdAt }
            val combined = (sortedRemote + sortedSeed).distinctBy { it.id }
            Result.success(combined)
        } catch (e: Exception) {
            Result.success(seed.sortedByDescending { it.createdAt })
        }
    }

    /**
     * Real-time Flow of testimonials from Firestore + seed fallback.
     * - Ulasan asli dari user (remoteList) SELALU berada di posisi teratas (urutan pertama).
     * - Ulasan milik akun saat ini otomatis disematkan di posisi puncak.
     * - Otomatis ter-update secara real-time di seluruh perangkat pengguna.
     */
    fun observeTestimonials(currentUserId: String = ""): Flow<List<Testimonial>> {
        val seed = getSeedTestimonials()
        return firestoreSyncService.observeTestimonials()
            .map { remoteList ->
                // Sort remote real reviews newest-first
                val sortedRemote = remoteList.sortedByDescending { it.createdAt }
                // Sort seed showcase reviews newest-first
                val sortedSeed = seed.sortedByDescending { it.createdAt }
                
                // Real user testimonials ALWAYS take precedence at the very top
                val combined = (sortedRemote + sortedSeed).distinctBy { it.id }
                combined.sortedWith(
                    compareByDescending<Testimonial> { it.userId == currentUserId && currentUserId.isNotBlank() }
                        .thenByDescending { !it.id.startsWith("seed_") }
                        .thenByDescending { it.createdAt }
                )
            }
            .catch {
                emit(seed.sortedByDescending { it.createdAt })
            }
    }

    suspend fun addTestimonial(testimonial: Testimonial): Result<Unit> {
        val docId = if (testimonial.id.isNotBlank()) {
            testimonial.id
        } else if (testimonial.userId.isNotBlank()) {
            testimonial.userId
        } else {
            java.util.UUID.randomUUID().toString()
        }
        val now = System.currentTimeMillis()
        val finalTestimonial = testimonial.copy(
            id = docId,
            createdAt = if (testimonial.createdAt <= 0) now else testimonial.createdAt
        )
        return firestoreSyncService.saveTestimonialToFirestore(finalTestimonial)
    }

    /**
     * Hapus testimoni dari Firestore secara permanen (Developer Mode only).
     * Seed testimoni (id diawali "seed_") tidak dapat dihapus karena
     * hanya ada di kode, bukan di Firestore collection.
     */
    suspend fun deleteTestimonial(id: String): Result<Unit> {
        if (id.startsWith("seed_")) return Result.failure(Exception("Seed testimonials cannot be deleted"))
        return try {
            firestoreSyncService.deleteTestimonial(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
