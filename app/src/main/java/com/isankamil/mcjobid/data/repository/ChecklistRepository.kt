package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.dao.ChecklistDao
import com.isankamil.mcjobid.data.local.entity.ChecklistEntity
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.ChecklistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepository @Inject constructor(
    private val checklistDao: ChecklistDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth
) {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getChecklistByBooking(bookingId: String): Flow<List<ChecklistItem>> {
        return checklistDao.getChecklistByBooking(bookingId).map { list ->
            list.map { ChecklistItem.fromEntity(it) }
        }
    }

    suspend fun toggleChecklistItem(id: String, currentStatus: Boolean) {
        checklistDao.updateCompletionStatus(id, !currentStatus)
        checklistDao.getChecklistItemById(id)?.let {
            firestoreSyncService.saveChecklistItemToFirestore(it)
        }
    }

    suspend fun addChecklistItem(bookingId: String, title: String) {
        val now = LocalDateTime.now()
        val item = ChecklistEntity(
            id = "chk_${System.currentTimeMillis()}_${(100..999).random()}",
            ownerId = getCurrentUserId(),
            bookingId = bookingId,
            title = title,
            isCompleted = false,
            sortOrder = 99,
            createdAt = now.toString()
        )
        checklistDao.insertChecklistItem(item)
        firestoreSyncService.saveChecklistItemToFirestore(item)
    }

    suspend fun deleteChecklistItem(item: ChecklistItem) {
        val entity = item.toEntity().copy(ownerId = getCurrentUserId())
        checklistDao.deleteChecklistItem(entity)
        firestoreSyncService.deleteChecklistItemFromFirestore(item.id)
    }

    suspend fun seedDefaultChecklistForBooking(bookingId: String) {
        val now = LocalDateTime.now().toString()
        val uid = getCurrentUserId()
        val defaults = listOf(
            "Konfirmasi PIC / Wedding Organizer",
            "Cek rundown acara & skenario",
            "Cek dresscode & wardrobe MC",
            "Cek lokasi & rute perjalanan venue",
            "Siapkan script & cue card MC"
        )

        val entities = defaults.mapIndexed { index, title ->
            ChecklistEntity(
                id = "chk_${bookingId}_$index",
                ownerId = uid,
                bookingId = bookingId,
                title = title,
                isCompleted = false,
                sortOrder = index,
                createdAt = now
            )
        }

        checklistDao.insertChecklistItems(entities)
        entities.forEach { firestoreSyncService.saveChecklistItemToFirestore(it) }
    }
}
