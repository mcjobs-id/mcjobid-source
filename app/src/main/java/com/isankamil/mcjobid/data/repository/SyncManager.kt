package com.isankamil.mcjobid.data.repository

import android.content.Context
import android.util.Log
import com.isankamil.mcjobid.data.local.dao.BookingDao
import com.isankamil.mcjobid.data.local.dao.ChecklistDao
import com.isankamil.mcjobid.data.local.dao.SyncQueueDao
import com.isankamil.mcjobid.data.local.dao.ClientDao
import com.isankamil.mcjobid.data.local.dao.ExpenseDao
import com.isankamil.mcjobid.data.local.dao.InvoiceDao
import com.isankamil.mcjobid.data.local.dao.PaymentDao
import com.isankamil.mcjobid.data.local.dao.ReminderDao
import com.isankamil.mcjobid.data.local.dao.TodoDao
import com.isankamil.mcjobid.data.local.dao.UserProfileDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.data.remote.firebase.FirebaseAuthService
import com.isankamil.mcjobid.domain.model.AuthUiState
import com.isankamil.mcjobid.util.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val firestoreSyncService: FirestoreSyncService,
    private val firebaseAuthService: FirebaseAuthService,
    private val bookingRepository: BookingRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userProfileDao: UserProfileDao,
    private val bookingDao: BookingDao,
    private val clientDao: ClientDao,
    private val paymentDao: PaymentDao,
    private val expenseDao: ExpenseDao,
    private val invoiceDao: InvoiceDao,
    private val reminderDao: ReminderDao,
    private val checklistDao: ChecklistDao,
    private val todoDao: TodoDao,
    private val syncQueueDao: SyncQueueDao
) {
    
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var syncWorker: Job? = null
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()
    
    init {
        // Start real-time sync listeners whenever the user is authenticated,
        // and tear them down when signed out. This fixes the missing sync on
        // fresh installs where login happens after app startup.
        syncScope.launch {
            firebaseAuthService.authStateFlow
                .distinctUntilChanged()
                .collect { state ->
                    when (state) {
                        is AuthUiState.Authenticated -> startRealTimeSync()
                        AuthUiState.Unauthenticated -> stopRealTimeSync()
                        else -> Unit
                    }
                }
        }
    }

    private suspend inline fun <T> syncEntities(
        userId: String,
        remoteList: List<T>,
        entityType: String,
        crossinline getId: (T) -> String,
        crossinline insertAll: suspend (List<T>) -> Unit,
        crossinline deleteNotInIds: suspend (String, List<String>) -> Unit,
        crossinline deleteAll: suspend (String) -> Unit
    ) {
        val pendingTasks = syncQueueDao.getPendingTasks(userId).filter { it.entityType == entityType }
        val pendingIds = pendingTasks.map { it.entityId }
        val idsToKeep = (remoteList.map(getId) + pendingIds).distinct()

        if (idsToKeep.isNotEmpty()) {
            if (remoteList.isNotEmpty()) {
                insertAll(remoteList)
            }
            deleteNotInIds(userId, idsToKeep)
        } else {
            deleteAll(userId)
        }
        _lastSyncTime.value = System.currentTimeMillis()
    }

    private fun startRealTimeSync() {
        val userId = firebaseAuthService.getCurrentUserId() ?: return

        syncWorker?.cancel()
        syncWorker = syncScope.launch {
            _isSyncing.value = true

            // Retry any failed writes stored in local SyncQueue
            launch {
                processPendingSyncQueue(userId)
            }

            // Sync & Observe User Profile (Real-time stream)
            val profileJob = launch {
                userProfileRepository.observeFirestoreProfile(userId).catch { e ->
                    Log.w("SyncManager", "Profile observe error: ${e.message}")
                }.collect { remoteProfile ->
                    if (remoteProfile != null) {
                        val localEntity = userProfileDao.getUserProfile(userId)
                        val effectivePhotoUri = when {
                            !remoteProfile.photoUri.isNullOrBlank() -> remoteProfile.photoUri
                            !remoteProfile.photoUrl.isNullOrBlank() -> remoteProfile.photoUrl
                            localEntity != null && !localEntity.photoUri.isNullOrBlank() -> localEntity.photoUri
                            else -> null
                        }
                        val finalEntity = remoteProfile.toEntity().copy(
                            photoUri = effectivePhotoUri,
                            photoUrl = remoteProfile.photoUrl ?: effectivePhotoUri
                        )
                        userProfileDao.insertUserProfile(finalEntity)
                    } else {
                        // Preserve local Room DB profile on cloud snapshot absence, and push local profile to cloud if available
                        val localEntity = userProfileDao.getUserProfile(userId)
                        if (localEntity != null) {
                            firestoreSyncService.syncUserProfileToFirestore(localEntity)
                        }
                    }
                    _lastSyncTime.value = System.currentTimeMillis()
                }
            }

            // Observe Bookings
            val bookingsJob = launch {
                firestoreSyncService.observeBookings(userId).catch { e ->
                    Log.e("SyncManager", "Bookings observe error: ${e.message}")
                    _syncError.value = e.message
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "booking", { it.id }, bookingDao::insertBookings, bookingDao::deleteBookingsNotInIds, bookingDao::deleteAllByOwner)
                }
            }

            // Observe Clients
            val clientsJob = launch {
                firestoreSyncService.observeClients(userId).catch { e ->
                    Log.e("SyncManager", "Clients observe error: ${e.message}")
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "client", { it.id }, clientDao::insertClients, clientDao::deleteClientsNotInIds, clientDao::deleteAllByOwner)
                }
            }

            // Observe Payments
            val paymentsJob = launch {
                firestoreSyncService.observePayments(userId).catch { e ->
                    Log.e("SyncManager", "Payments observe error: ${e.message}")
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "payment", { it.id }, paymentDao::insertPayments, paymentDao::deletePaymentsNotInIds, paymentDao::deleteAllByOwner)
                }
            }

            // Observe Expenses
            val expensesJob = launch {
                firestoreSyncService.observeExpenses(userId).catch { e ->
                    Log.e("SyncManager", "Expenses observe error: ${e.message}")
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "expense", { it.id }, expenseDao::insertExpenses, expenseDao::deleteExpensesNotInIds, expenseDao::deleteAllByOwner)
                }
            }

            // Observe Invoices
            val invoicesJob = launch {
                firestoreSyncService.observeInvoices(userId).catch { e ->
                    Log.e("SyncManager", "Invoices observe error: ${e.message}")
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "invoice", { it.id }, invoiceDao::insertInvoices, invoiceDao::deleteInvoicesNotInIds, invoiceDao::deleteAllByOwner)
                }
            }

            // Observe Reminders
            val remindersJob = launch {
                firestoreSyncService.observeReminders(userId).catch { e ->
                    Log.e("SyncManager", "Reminders observe error: ${e.message}")
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "reminder", { it.id }, reminderDao::insertReminders, reminderDao::deleteRemindersNotInIds, reminderDao::deleteAllByOwner)
                }
            }

            // Observe Checklists
            val checklistsJob = launch {
                firestoreSyncService.observeChecklists(userId).catch { e ->
                    Log.e("SyncManager", "Checklists observe error: ${e.message}")
                }.collect { remoteList ->
                    syncEntities(userId, remoteList, "checklist", { it.id }, checklistDao::insertChecklistItems, checklistDao::deleteChecklistsNotInIds, checklistDao::deleteAllByOwner)
                }
            }

            _isSyncing.value = false
        }
    }

    fun forceSync() {
        syncScope.launch {
            if (firebaseAuthService.isUserLoggedIn) {
                startRealTimeSync()
            }
        }
    }

    /**
     * Clears all local Room data for the given userId.
     * MUST be called on logout to prevent cross-user data leakage.
     */
    suspend fun clearAllUserData(userId: String) {
        try {
            bookingDao.deleteAllByOwner(userId)
            clientDao.deleteAllByOwner(userId)
            paymentDao.deleteAllByOwner(userId)
            expenseDao.deleteAllByOwner(userId)
            invoiceDao.deleteAllByOwner(userId)
            reminderDao.deleteAllByOwner(userId)
            checklistDao.deleteAllByOwner(userId)
            todoDao.deleteAllByOwner(userId)
            userProfileDao.deleteUserProfileById(userId)
            Log.i("SyncManager", "All local data cleared for user: $userId")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error clearing user data: ${e.message}")
        }
    }

    private fun stopRealTimeSync() {
        syncWorker?.cancel()
        syncWorker = null
        _isSyncing.value = false
    }
    
    fun clearError() {
        _syncError.value = null
    }
    
    fun isOnline(): Boolean {
        return networkMonitor.isCurrentlyOnline()
    }
    
    val isOnlineFlow: Flow<Boolean> = networkMonitor.isOnline

    private suspend fun processPendingSyncQueue(userId: String) {
        try {
            val pendingTasks = syncQueueDao.getPendingTasks(userId)
            if (pendingTasks.isEmpty()) return
            Log.i("SyncManager", "Processing ${pendingTasks.size} pending sync tasks...")
            for (task in pendingTasks) {
                var success = false
                if (task.operation == "DELETE") {
                    success = when (task.entityType) {
                        "booking" -> firestoreSyncService.deleteBookingFromFirestore(task.entityId).isSuccess
                        "client" -> firestoreSyncService.deleteClientFromFirestore(task.entityId).isSuccess
                        "payment" -> firestoreSyncService.deletePaymentFromFirestore(task.entityId).isSuccess
                        "expense" -> firestoreSyncService.deleteExpenseFromFirestore(task.entityId).isSuccess
                        "invoice" -> firestoreSyncService.deleteInvoiceFromFirestore(task.entityId).isSuccess
                        "reminder" -> firestoreSyncService.deleteReminderFromFirestore(task.entityId).isSuccess
                        "checklist" -> firestoreSyncService.deleteChecklistItemFromFirestore(task.entityId).isSuccess
                        else -> true
                    }
                } else {
                    when (task.entityType) {
                        "booking" -> {
                            val entity = bookingDao.getBookingById(task.entityId)
                            success = if (entity != null) firestoreSyncService.saveBookingToFirestore(entity).isSuccess else true
                        }
                        "client" -> {
                            val entity = clientDao.getClientById(task.entityId)
                            success = if (entity != null) firestoreSyncService.saveClientToFirestore(entity).isSuccess else true
                        }
                        "payment" -> {
                            val entity = paymentDao.getPaymentById(task.entityId)
                            success = if (entity != null) firestoreSyncService.savePaymentToFirestore(entity).isSuccess else true
                        }
                        "expense" -> {
                            val entity = expenseDao.getExpenseById(task.entityId)
                            success = if (entity != null) firestoreSyncService.saveExpenseToFirestore(entity).isSuccess else true
                        }
                        "invoice" -> {
                            val entity = invoiceDao.getInvoiceById(task.entityId)
                            success = if (entity != null) firestoreSyncService.saveInvoiceToFirestore(entity).isSuccess else true
                        }
                        "reminder" -> {
                            val entity = reminderDao.getReminderById(task.entityId)
                            success = if (entity != null) firestoreSyncService.saveReminderToFirestore(entity).isSuccess else true
                        }
                        "checklist" -> {
                            val entity = checklistDao.getChecklistItemById(task.entityId)
                            success = if (entity != null) firestoreSyncService.saveChecklistItemToFirestore(entity).isSuccess else true
                        }
                        "userprofile" -> {
                            val entity = userProfileDao.getUserProfile(task.entityId)
                            success = if (entity != null) firestoreSyncService.syncUserProfileToFirestore(entity).isSuccess else true
                        }
                    }
                }
                if (success) {
                    syncQueueDao.deleteTask(task.id)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error processing sync queue: ${e.message}")
        }
    }
}


