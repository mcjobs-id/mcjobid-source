package com.isankamil.mcjobid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isankamil.mcjobid.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncTask(task: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE ownerId = :ownerId ORDER BY createdAt ASC")
    suspend fun getPendingTasks(ownerId: String): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("DELETE FROM sync_queue WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)
}
