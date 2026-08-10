package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isDismissed = 0 ORDER BY targetDate ASC, createdAt DESC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE ownerId = :ownerId AND isDismissed = 0 ORDER BY targetDate ASC, createdAt DESC")
    fun getActiveRemindersByOwner(ownerId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE bookingId = :bookingId")
    fun getRemindersByBooking(bookingId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE bookingId = :bookingId")
    suspend fun getRemindersListByBooking(bookingId: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE reminders SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissReminder(id: String)

    @Query("UPDATE reminders SET isDismissed = 1 WHERE bookingId = :bookingId AND (reminderType = 'PAYMENT' OR reminderType = 'PAYMENT_OVERDUE')")
    suspend fun dismissPaymentRemindersByBooking(bookingId: String)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE bookingId = :bookingId")
    suspend fun deleteRemindersByBooking(bookingId: String)

    @Query("DELETE FROM reminders WHERE bookingId = :bookingId AND (reminderType = 'PAYMENT' OR reminderType = 'PAYMENT_OVERDUE')")
    suspend fun deletePaymentRemindersByBooking(bookingId: String)

    @Query("DELETE FROM reminders WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("DELETE FROM reminders WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteRemindersNotInIds(ownerId: String, ids: List<String>)

    @Query("DELETE FROM reminders WHERE bookingId = 'sim_booking' OR id LIKE 'sim_%'")
    suspend fun deleteSimulatedReminders()
}
