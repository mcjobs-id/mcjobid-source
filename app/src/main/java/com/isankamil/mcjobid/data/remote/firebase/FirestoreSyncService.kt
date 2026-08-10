package com.isankamil.mcjobid.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.isankamil.mcjobid.data.local.dao.BookingDao
import com.isankamil.mcjobid.data.local.dao.UserProfileDao
import com.isankamil.mcjobid.data.local.dao.SyncQueueDao
import com.isankamil.mcjobid.data.local.entity.SyncQueueEntity
import com.isankamil.mcjobid.domain.model.Testimonial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.isankamil.mcjobid.data.local.entity.BookingEntity
import com.isankamil.mcjobid.data.local.entity.ChecklistEntity
import com.isankamil.mcjobid.data.local.entity.ClientEntity
import com.isankamil.mcjobid.data.local.entity.ExpenseEntity
import com.isankamil.mcjobid.data.local.entity.InvoiceEntity
import com.isankamil.mcjobid.data.local.entity.PaymentEntity
import com.isankamil.mcjobid.data.local.entity.ReminderEntity
import com.isankamil.mcjobid.data.local.entity.UserProfileEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val bookingDao: BookingDao,
    private val userProfileDao: UserProfileDao,
    private val syncQueueDao: SyncQueueDao
) {
    
    private val bookingsCollection = firestore.collection("bookings")
    private val usersCollection = firestore.collection("users")
    private val clientsCollection = firestore.collection("clients")
    private val paymentsCollection = firestore.collection("payments")
    private val expensesCollection = firestore.collection("expenses")
    private val invoicesCollection = firestore.collection("invoices")
    private val remindersCollection = firestore.collection("reminders")
    private val checklistsCollection = firestore.collection("checklists")
    private val testimonialsCollection = firestore.collection("testimonials")

    // --- Bookings ---
    
    suspend fun saveBookingToFirestore(booking: BookingEntity): Result<Unit> { return try { if (booking.ownerId.isNotBlank()) { bookingsCollection.document(booking.id).set(booking, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = booking.ownerId, entityType = "booking", entityId = booking.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun deleteBookingFromFirestore(bookingId: String, ownerId: String = ""): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "booking", entityId = bookingId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllBookingsFromFirestore(userId: String): Result<List<BookingEntity>> {
        return try {
            val snapshot = bookingsCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(BookingEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Clients ---

    suspend fun saveClientToFirestore(client: ClientEntity): Result<Unit> { return try { if (client.ownerId.isNotBlank()) { clientsCollection.document(client.id).set(client, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = client.ownerId, entityType = "client", entityId = client.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun deleteClientFromFirestore(clientId: String, ownerId: String = ""): Result<Unit> {
        return try {
            clientsCollection.document(clientId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "client", entityId = clientId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllClientsFromFirestore(userId: String): Result<List<ClientEntity>> {
        return try {
            val snapshot = clientsCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(ClientEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Payments ---

    suspend fun savePaymentToFirestore(payment: PaymentEntity): Result<Unit> { return try { if (payment.ownerId.isNotBlank()) { paymentsCollection.document(payment.id).set(payment, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = payment.ownerId, entityType = "payment", entityId = payment.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun deletePaymentFromFirestore(paymentId: String, ownerId: String = ""): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "payment", entityId = paymentId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllPaymentsFromFirestore(userId: String): Result<List<PaymentEntity>> {
        return try {
            val snapshot = paymentsCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(PaymentEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Expenses ---

    suspend fun saveExpenseToFirestore(expense: ExpenseEntity): Result<Unit> { return try { if (expense.ownerId.isNotBlank()) { expensesCollection.document(expense.id).set(expense, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = expense.ownerId, entityType = "expense", entityId = expense.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun deleteExpenseFromFirestore(expenseId: String, ownerId: String = ""): Result<Unit> {
        return try {
            expensesCollection.document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "expense", entityId = expenseId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllExpensesFromFirestore(userId: String): Result<List<ExpenseEntity>> {
        return try {
            val snapshot = expensesCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(ExpenseEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Invoices ---

    suspend fun saveInvoiceToFirestore(invoice: InvoiceEntity): Result<Unit> { return try { if (invoice.ownerId.isNotBlank()) { invoicesCollection.document(invoice.id).set(invoice, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = invoice.ownerId, entityType = "invoice", entityId = invoice.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun deleteInvoiceFromFirestore(invoiceId: String, ownerId: String = ""): Result<Unit> {
        return try {
            invoicesCollection.document(invoiceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "invoice", entityId = invoiceId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllInvoicesFromFirestore(userId: String): Result<List<InvoiceEntity>> {
        return try {
            val snapshot = invoicesCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(InvoiceEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Reminders ---

    suspend fun saveReminderToFirestore(reminder: ReminderEntity): Result<Unit> { return try { if (reminder.ownerId.isNotBlank()) { remindersCollection.document(reminder.id).set(reminder, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = reminder.ownerId, entityType = "reminder", entityId = reminder.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun updateReminderReadStatus(reminderId: String, isRead: Boolean): Result<Unit> {
        return try {
            remindersCollection.document(reminderId).update("isRead", isRead).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminderDismissStatus(reminderId: String, isDismissed: Boolean): Result<Unit> {
        return try {
            remindersCollection.document(reminderId).update("isDismissed", isDismissed).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminderFromFirestore(reminderId: String, ownerId: String = ""): Result<Unit> {
        return try {
            remindersCollection.document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "reminder", entityId = reminderId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllRemindersFromFirestore(userId: String): Result<List<ReminderEntity>> {
        return try {
            val snapshot = remindersCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(ReminderEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Checklists ---

    suspend fun saveChecklistItemToFirestore(item: ChecklistEntity): Result<Unit> { return try { if (item.ownerId.isNotBlank()) { checklistsCollection.document(item.id).set(item, SetOptions.merge()).await() }; Result.success(Unit) } catch (e: Exception) { CoroutineScope(Dispatchers.IO).launch { syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = item.ownerId, entityType = "checklist", entityId = item.id, operation = "UPSERT")) }; Result.failure(e) } }

    suspend fun deleteChecklistItemFromFirestore(itemId: String, ownerId: String = ""): Result<Unit> {
        return try {
            checklistsCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (ownerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = ownerId, entityType = "checklist", entityId = itemId, operation = "DELETE"))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchAllChecklistItemsFromFirestore(userId: String): Result<List<ChecklistEntity>> {
        return try {
            val snapshot = checklistsCollection.whereEqualTo("ownerId", userId).get().await()
            val list = snapshot.toObjects(ChecklistEntity::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- User Profile ---
    
    suspend fun syncUserProfileToFirestore(profile: UserProfileEntity): Result<Unit> { 
        return try { 
            usersCollection.document(profile.userId).set(profile.toFirestoreMap(), SetOptions.merge()).await(); 
            Result.success(Unit) 
        } catch (e: Exception) { 
            CoroutineScope(Dispatchers.IO).launch { 
                syncQueueDao.insertSyncTask(SyncQueueEntity(ownerId = profile.userId, entityType = "userprofile", entityId = profile.userId, operation = "UPSERT")) 
            }; 
            Result.failure(e) 
        } 
    }
    
    suspend fun syncUserProfileFromFirestore(userId: String): Result<UserProfileEntity?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            Result.success(snapshot.toObject(UserProfileEntity::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Real-Time Observers ---

    fun observeBookings(userId: String): Flow<List<BookingEntity>> = callbackFlow {
        val listener = bookingsCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(BookingEntity::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeClients(userId: String): Flow<List<ClientEntity>> = callbackFlow {
        val listener = clientsCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(ClientEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observePayments(userId: String): Flow<List<PaymentEntity>> = callbackFlow {
        val listener = paymentsCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(PaymentEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeExpenses(userId: String): Flow<List<ExpenseEntity>> = callbackFlow {
        val listener = expensesCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(ExpenseEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeInvoices(userId: String): Flow<List<InvoiceEntity>> = callbackFlow {
        val listener = invoicesCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(InvoiceEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeReminders(userId: String): Flow<List<ReminderEntity>> = callbackFlow {
        val listener = remindersCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(ReminderEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeChecklists(userId: String): Flow<List<ChecklistEntity>> = callbackFlow {
        val listener = checklistsCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(ChecklistEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    /**
     * Real-time observer for the global testimonials collection.
     * Testimonials are NOT filtered by ownerId — they are shared across all users.
     * Any new testimonial written by any authenticated user will be pushed here instantly.
     */
    fun observeTestimonials(): Flow<List<Testimonial>> = callbackFlow {
        val listener = testimonialsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreSyncService", "observeTestimonials error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            Testimonial(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "",
                                avatarResId = doc.getLong("avatarResId")?.toInt(),
                                photoUrl = doc.getString("photoUrl"),
                                rating = doc.getLong("rating")?.toInt() ?: 5,
                                review = doc.getString("review") ?: "",
                                suggestion = doc.getString("suggestion") ?: "",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreSyncService", "Error parsing testimonial doc ${doc.id}", e)
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveTestimonialToFirestore(testimonial: Testimonial): Result<Unit> {
        return try {
            val docId = if (testimonial.userId.isNotBlank()) testimonial.userId 
            else if (testimonial.id.isNotBlank()) testimonial.id 
            else testimonialsCollection.document().id

            val map = mapOf(
                "id" to docId,
                "userId" to testimonial.userId,
                "ownerId" to testimonial.userId,
                "userName" to testimonial.userName,
                "avatarResId" to testimonial.avatarResId,
                "photoUrl" to testimonial.photoUrl,
                "rating" to testimonial.rating,
                "review" to testimonial.review,
                "suggestion" to testimonial.suggestion,
                "createdAt" to testimonial.createdAt
            )
            testimonialsCollection.document(docId).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreSyncService", "saveTestimonialToFirestore error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Hapus testimoni dari Firestore secara permanen.
     */
    suspend fun deleteTestimonial(id: String) {
        testimonialsCollection.document(id).delete().await()
    }

    private val rateCardsCollection = firestore.collection("rate_cards")

    suspend fun saveRateCardToFirestore(rateCard: com.isankamil.mcjobid.data.local.entity.RateCardEntity): Result<Unit> {
        return try {
            if (rateCard.ownerId.isNotBlank() && rateCard.id.isNotBlank()) {
                rateCardsCollection.document(rateCard.id).set(rateCard, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRateCardFromFirestore(rateCardId: String): Result<Unit> {
        return try {
            rateCardsCollection.document(rateCardId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Todos ---
    private val todosCollection = firestore.collection("todos")

    suspend fun saveTodoToFirestore(todo: com.isankamil.mcjobid.data.local.entity.TodoEntity): Result<Unit> {
        return try {
            if (todo.ownerId.isNotBlank() && todo.id.isNotBlank()) {
                todosCollection.document(todo.id).set(todo, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTodoFromFirestore(todoId: String): Result<Unit> {
        return try {
            todosCollection.document(todoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeTodos(userId: String): Flow<List<com.isankamil.mcjobid.data.local.entity.TodoEntity>> = callbackFlow {
        val listener = todosCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(com.isankamil.mcjobid.data.local.entity.TodoEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun fetchAllTodosFromFirestore(userId: String): Result<List<com.isankamil.mcjobid.data.local.entity.TodoEntity>> {
        return try {
            val snapshot = todosCollection.whereEqualTo("ownerId", userId).get().await()
            Result.success(snapshot.toObjects(com.isankamil.mcjobid.data.local.entity.TodoEntity::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


