package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val notes: String = "",
    val category: String = "PERSIAPAN", // PERSIAPAN, HARI_H, PASCA_EVENT, KARIER, UMUM
    val priority: String = "SEDANG", // TINGGI, SEDANG, RENDAH
    val dueDate: Long = 0L,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val bookingId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
