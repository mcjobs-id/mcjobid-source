package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.RateCardEntity
import java.time.LocalDateTime

data class RateCard(
    val id: String = "",
    val ownerId: String = "",
    val category: String = "Wedding", // "Wedding", "Corporate", "Private Event", "Government", "Other"
    val title: String = "",
    val price: Long = 0L,
    val durationHours: Double = 3.0,
    val description: String = "",
    val inclusions: List<String> = emptyList(),
    val addOns: List<String> = emptyList(),
    val terms: String = "",
    val isDefault: Boolean = false,
    val createdAt: String = LocalDateTime.now().toString()
) {
    fun toEntity(): RateCardEntity {
        return RateCardEntity(
            id = id,
            ownerId = ownerId,
            category = category,
            title = title,
            price = price,
            durationHours = durationHours,
            description = description,
            inclusionsJson = inclusions.joinToString("|||"),
            addOnsJson = addOns.joinToString("|||"),
            terms = terms,
            isDefault = isDefault,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: RateCardEntity): RateCard {
            val inclusionsList = if (entity.inclusionsJson.isBlank()) emptyList() 
            else entity.inclusionsJson.split("|||").filter { it.isNotBlank() }
            
            val addOnsList = if (entity.addOnsJson.isBlank()) emptyList() 
            else entity.addOnsJson.split("|||").filter { it.isNotBlank() }

            return RateCard(
                id = entity.id,
                ownerId = entity.ownerId,
                category = entity.category,
                title = entity.title,
                price = entity.price,
                durationHours = entity.durationHours,
                description = entity.description,
                inclusions = inclusionsList,
                addOns = addOnsList,
                terms = entity.terms,
                isDefault = entity.isDefault,
                createdAt = entity.createdAt
            )
        }
    }
}
