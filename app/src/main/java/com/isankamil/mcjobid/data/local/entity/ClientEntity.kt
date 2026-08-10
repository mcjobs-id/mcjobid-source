package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val phone: String? = null,
    val email: String? = null,
    val company: String? = null,
    val pic: String? = null,
    val notes: String? = null,
    @get:PropertyName("favorite")
    @set:PropertyName("favorite")
    var isFavorite: Boolean = false,
    @get:PropertyName("archived")
    @set:PropertyName("archived")
    var isArchived: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)
