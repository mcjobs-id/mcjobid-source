package com.isankamil.mcjobid.domain.model

import com.isankamil.mcjobid.data.local.entity.ClientEntity
import java.time.LocalDateTime

data class Client(
    val id: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val company: String? = null,
    val pic: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toEntity() = ClientEntity(
        id = id,
        name = name,
        phone = phone,
        email = email,
        company = company,
        pic = pic,
        notes = notes,
        isFavorite = isFavorite,
        isArchived = isArchived,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )

    companion object {
        fun fromEntity(entity: ClientEntity) = Client(
            id = entity.id,
            name = entity.name,
            phone = entity.phone,
            email = entity.email,
            company = entity.company,
            pic = entity.pic,
            notes = entity.notes,
            isFavorite = entity.isFavorite,
            isArchived = entity.isArchived,
            createdAt = try { LocalDateTime.parse(entity.createdAt) } catch (e: Exception) { LocalDateTime.now() },
            updatedAt = try { LocalDateTime.parse(entity.updatedAt) } catch (e: Exception) { LocalDateTime.now() }
        )
    }
}
