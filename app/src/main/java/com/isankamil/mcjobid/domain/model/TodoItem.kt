package com.isankamil.mcjobid.domain.model

data class TodoItem(
    val id: String,
    val ownerId: String = "",
    val title: String,
    val notes: String = "",
    val category: TodoCategory = TodoCategory.PERSIAPAN,
    val priority: TodoPriority = TodoPriority.SEDANG,
    val dueDate: Long = 0L,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val bookingId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TodoCategory(val label: String, val emoji: String, val description: String) {
    PERSIAPAN("Persiapan Event", "🎯", "Briefing, cue card, gladi resik & dresscode"),
    HARI_H("Hari-H Acara", "🎤", "Sound check, koordinasi panggung & pembukaan"),
    PASCA_EVENT("Pasca Acara & Invoice", "💼", "Pelunasan, kwitansi, & follow-up testimoni"),
    KARIER("Karier & Portofolio", "🚀", "Update rate card, photoshoot, & latihan vokal"),
    UMUM("Catatan Umum", "📝", "Tugas mandiri & checklist harian MC");

    companion object {
        fun fromString(value: String): TodoCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PERSIAPAN
        }
    }
}

enum class TodoPriority(val label: String, val colorHex: Long) {
    TINGGI("Prioritas Tinggi", 0xFFEF4444),
    SEDANG("Prioritas Sedang", 0xFFF59E0B),
    RENDAH("Prioritas Rendah", 0xFF10B981);

    companion object {
        fun fromString(value: String): TodoPriority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SEDANG
        }
    }
}
