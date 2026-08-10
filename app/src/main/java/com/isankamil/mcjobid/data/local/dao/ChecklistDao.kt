package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists WHERE bookingId = :bookingId ORDER BY sortOrder ASC, createdAt ASC")
    fun getChecklistByBooking(bookingId: String): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE bookingId = :bookingId")
    suspend fun getChecklistListByBooking(bookingId: String): List<ChecklistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistEntity>)

    @Query("UPDATE checklists SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: String, isCompleted: Boolean)

    @Query("SELECT * FROM checklists WHERE id = :id LIMIT 1")
    suspend fun getChecklistItemById(id: String): ChecklistEntity?

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistEntity)

    @Query("DELETE FROM checklists WHERE bookingId = :bookingId")
    suspend fun deleteChecklistForBooking(bookingId: String)

    @Query("DELETE FROM checklists WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("DELETE FROM checklists WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteChecklistsNotInIds(ownerId: String, ids: List<String>)
}
