package com.isankamil.mcjobid.data.repository

import com.isankamil.mcjobid.data.local.dao.TodoDao
import com.isankamil.mcjobid.data.local.entity.TodoEntity
import com.isankamil.mcjobid.data.remote.firebase.FirebaseAuthService
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.TodoCategory
import com.isankamil.mcjobid.domain.model.TodoItem
import com.isankamil.mcjobid.domain.model.TodoPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val firebaseAuthService: FirebaseAuthService
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private fun currentUserId(): String {
        return firebaseAuthService.getCurrentUserId() ?: "local_user"
    }

    fun observeTodos(): Flow<List<TodoItem>> {
        val ownerId = currentUserId()
        return todoDao.observeTodos(ownerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTodos(): List<TodoItem> {
        val ownerId = currentUserId()
        return todoDao.getTodos(ownerId).map { it.toDomain() }
    }

    suspend fun addTodo(
        title: String,
        notes: String = "",
        category: TodoCategory = TodoCategory.PERSIAPAN,
        priority: TodoPriority = TodoPriority.SEDANG,
        dueDate: Long = 0L,
        bookingId: String? = null
    ): Result<TodoItem> {
        return try {
            val ownerId = currentUserId()
            val id = UUID.randomUUID().toString()
            val entity = TodoEntity(
                id = id,
                ownerId = ownerId,
                title = title.trim(),
                notes = notes.trim(),
                category = category.name,
                priority = priority.name,
                dueDate = dueDate,
                isCompleted = false,
                completedAt = null,
                bookingId = bookingId,
                createdAt = System.currentTimeMillis()
            )
            todoDao.insertTodo(entity)

            // Async background Firestore sync
            repositoryScope.launch {
                firestoreSyncService.saveTodoToFirestore(entity)
            }

            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTodo(todo: TodoItem): Result<Unit> {
        return try {
            val ownerId = if (todo.ownerId.isNotBlank()) todo.ownerId else currentUserId()
            val entity = todo.toEntity().copy(ownerId = ownerId)
            todoDao.updateTodo(entity)

            repositoryScope.launch {
                firestoreSyncService.saveTodoToFirestore(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCompletion(todo: TodoItem): Result<Unit> {
        return try {
            val newStatus = !todo.isCompleted
            val completedTime = if (newStatus) System.currentTimeMillis() else null
            todoDao.setCompletion(todo.id, newStatus, completedTime)

            val updated = todo.copy(isCompleted = newStatus, completedAt = completedTime)
            repositoryScope.launch {
                firestoreSyncService.saveTodoToFirestore(updated.toEntity())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTodo(id: String): Result<Unit> {
        return try {
            todoDao.deleteTodoById(id)

            repositoryScope.launch {
                firestoreSyncService.deleteTodoFromFirestore(id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCompletedTodos(): Result<Unit> {
        return try {
            val ownerId = currentUserId()
            val completed = todoDao.getTodos(ownerId).filter { it.isCompleted }
            todoDao.deleteCompletedTodos(ownerId)

            repositoryScope.launch {
                completed.forEach {
                    firestoreSyncService.deleteTodoFromFirestore(it.id)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menambahkan paket checklist tugas standar MC profesional ke daftar To-Do.
     */
    suspend fun applyPredefinedMcTemplates(): Result<Int> {
        return try {
            val ownerId = currentUserId()
            val templates = listOf(
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Konfirmasi rundown final & cue card dengan WO/Klien",
                    notes = "Pastikan urutan acara, durasi per sesi, dan kontak PIC lapangan jelas.",
                    category = TodoCategory.PERSIAPAN.name,
                    priority = TodoPriority.TINGGI.name,
                    dueDate = System.currentTimeMillis() + 86400000L * 2
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Gladi resik & cek pelafalan nama VIP / gelar kehormatan",
                    notes = "Verifikasi ejaan nama keluarga pengantin, pejabat, atau pembicara utama.",
                    category = TodoCategory.PERSIAPAN.name,
                    priority = TodoPriority.TINGGI.name,
                    dueDate = System.currentTimeMillis() + 86400000L * 1
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Fitting jas / gaun & koordinasi dresscode panggung",
                    notes = "Sesuaikan palet warna busana MC dengan tema dekorasi panggung.",
                    category = TodoCategory.PERSIAPAN.name,
                    priority = TodoPriority.SEDANG.name,
                    dueDate = System.currentTimeMillis() + 86400000L * 3
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Sound check wireless mic & tes audio panggung H-1 jam",
                    notes = "Tes kejelasan vokal di panggung tengah, monitor panggung, dan cadangan baterai mic.",
                    category = TodoCategory.HARI_H.name,
                    priority = TodoPriority.TINGGI.name,
                    dueDate = System.currentTimeMillis()
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Briefing cepat dengan tim multimedia, lighting & band",
                    notes = "Sinkronisasi tanda cue lampu, musik opening, dan transisi video.",
                    category = TodoCategory.HARI_H.name,
                    priority = TodoPriority.SEDANG.name,
                    dueDate = System.currentTimeMillis()
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Kirim invoice pelunasan fee MC & kwitansi resmi",
                    notes = "Kirim PDF tagihan dan nomor rekening resmi via WhatsApp klien/EO.",
                    category = TodoCategory.PASCA_EVENT.name,
                    priority = TodoPriority.TINGGI.name,
                    dueDate = System.currentTimeMillis() + 86400000L * 1
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Follow-up ulasan & testimoni dari klien / WO",
                    notes = "Minta feedback kepuasan klien untuk dipublikasikan di feed ulasan MC.",
                    category = TodoCategory.PASCA_EVENT.name,
                    priority = TodoPriority.SEDANG.name,
                    dueDate = System.currentTimeMillis() + 86400000L * 2
                ),
                TodoEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    title = "Update katalog paket harga (Rate Card 2026)",
                    notes = "Perbarui penawaran paket MC Wedding, Corporate, dan Gathering.",
                    category = TodoCategory.KARIER.name,
                    priority = TodoPriority.SEDANG.name,
                    dueDate = System.currentTimeMillis() + 86400000L * 7
                )
            )

            todoDao.insertTodos(templates)

            repositoryScope.launch {
                templates.forEach { firestoreSyncService.saveTodoToFirestore(it) }
            }

            Result.success(templates.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TodoEntity.toDomain() = TodoItem(
        id = id,
        ownerId = ownerId,
        title = title,
        notes = notes,
        category = TodoCategory.fromString(category),
        priority = TodoPriority.fromString(priority),
        dueDate = dueDate,
        isCompleted = isCompleted,
        completedAt = completedAt,
        bookingId = bookingId,
        createdAt = createdAt
    )

    private fun TodoItem.toEntity() = TodoEntity(
        id = id,
        ownerId = ownerId,
        title = title,
        notes = notes,
        category = category.name,
        priority = priority.name,
        dueDate = dueDate,
        isCompleted = isCompleted,
        completedAt = completedAt,
        bookingId = bookingId,
        createdAt = createdAt
    )
}
