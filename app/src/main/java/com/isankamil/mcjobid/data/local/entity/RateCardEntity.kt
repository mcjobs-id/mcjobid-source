package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rate_cards")
data class RateCardEntity(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val category: String = "Wedding", // "Wedding", "Corporate", "Private Event", "Government", "Other"
    val title: String = "",
    val price: Long = 0L,
    val durationHours: Double = 3.0,
    val description: String = "",
    val inclusionsJson: String = "[]", // JSON list string of inclusion checklist
    val addOnsJson: String = "[]",     // JSON list string of add-ons
    val terms: String = "",            // Syarat & Ketentuan
    val isDefault: Boolean = false,
    val createdAt: String = ""
)
