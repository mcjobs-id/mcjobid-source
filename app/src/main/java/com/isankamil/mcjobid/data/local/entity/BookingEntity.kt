package com.isankamil.mcjobid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey
    val id: String = "", // timestamp epoch ms as string
    val ownerId: String = "", // Firebase Auth UID
    val name: String = "", // Nama Acara (wajib)
    val client: String? = null, // Nama Klien (opsional)
    val clientId: String? = null, // ID Klien terhubung
    val category: String? = "Wedding", // Kategori Acara
    val date: String = "", // format "yyyy-MM-dd" (wajib)
    val start: String? = null, // "HH:mm" (opsional)
    val end: String? = null, // "HH:mm" (opsional)
    val loc: String? = null, // Lokasi acara
    val address: String? = null, // Alamat lengkap
    val dresscode: String? = null, // Dresscode
    val theme: String? = null, // Tema acara
    val mcType: String? = "Single", // Single / Duet
    val language: String? = "Bahasa Indonesia", // Bahasa
    val audience: String? = null, // Est. Jumlah audience
    val specialRequest: String? = null, // Request khusus
    val pic: String? = null, // Kontak PIC / WO
    val fee: Long = 0, // Total honor (Rupiah)
    val dp: Long = 0, // Terbayar / DP (Rupiah)
    val note: String? = null, // Catatan tambahan
    val status: String = "confirmed", // "draft" | "confirmed" | "upcoming" | "today" | "completed" | "cancelled" | "active" | "done"
    val createdAt: String = "", // ISO 8601
    val updatedAt: String = "" // ISO 8601
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "client" to client,
            "clientId" to clientId,
            "category" to category,
            "date" to date,
            "start" to start,
            "end" to end,
            "loc" to loc,
            "address" to address,
            "dresscode" to dresscode,
            "theme" to theme,
            "mcType" to mcType,
            "language" to language,
            "audience" to audience,
            "specialRequest" to specialRequest,
            "pic" to pic,
            "fee" to fee,
            "dp" to dp,
            "note" to note,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
