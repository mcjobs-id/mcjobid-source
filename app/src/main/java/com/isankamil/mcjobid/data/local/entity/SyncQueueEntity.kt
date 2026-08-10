package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String,
    val entityType: String, // "booking", "client", "payment", "expense", "invoice", "reminder", "checklist"
    val entityId: String,
    val operation: String, // "UPSERT", "DELETE"
    val createdAt: Long = System.currentTimeMillis()
)
