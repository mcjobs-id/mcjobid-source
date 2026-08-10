package com.isankamil.mcjobid.data.repository

import com.isankamil.mcjobid.data.local.dao.ReminderDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: com.google.firebase.auth.FirebaseAuth
) {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getActiveReminders(): Flow<List<Reminder>> {
        val uid = getCurrentUserId()
        val rawFlow = if (uid.isNotBlank()) {
            reminderDao.getActiveRemindersByOwner(uid)
        } else {
            reminderDao.getActiveReminders()
        }
        return rawFlow.map { entities ->
            entities.map { Reminder.fromEntity(it) }
        }
    }

    suspend fun saveReminder(reminder: Reminder) {
        val entity = reminder.toEntity().copy(ownerId = getCurrentUserId())
        reminderDao.insertReminder(entity)
        if (!reminder.id.startsWith("sim_") && reminder.bookingId != "sim_booking") {
            firestoreSyncService.saveReminderToFirestore(entity)
        }
    }

    suspend fun deleteSimulatedReminders() {
        reminderDao.deleteSimulatedReminders()
    }

    suspend fun markAsRead(id: String) {
        reminderDao.markAsRead(id)
        firestoreSyncService.updateReminderReadStatus(id, true)
    }

    suspend fun dismissReminder(id: String) {
        reminderDao.dismissReminder(id)
        firestoreSyncService.updateReminderDismissStatus(id, true)
    }
}
