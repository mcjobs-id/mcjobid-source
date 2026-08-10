package com.isankamil.mcjobid.domain.model

data class Testimonial(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val avatarResId: Int? = null,
    val photoUrl: String? = null,
    val rating: Int = 0,
    val review: String = "",
    val suggestion: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
