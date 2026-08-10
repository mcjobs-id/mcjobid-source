package com.isankamil.mcjobid.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.McJobIdDatabase
import com.isankamil.mcjobid.data.local.dao.*
import com.isankamil.mcjobid.data.local.entity.*
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.FinancialSummary
import com.isankamil.mcjobid.util.NotificationScheduler
import com.isankamil.mcjobid.util.SettingsKeys
import com.isankamil.mcjobid.util.settingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class FinancialAnalytics(
    val grossRevenue: Long = 0,
    val totalExpenses: Long = 0,
    val netIncome: Long = 0,
    val growthPercentage: Double = 0.0,
    val totalPaid: Long = 0,
    val totalOutstanding: Long = 0,
    val totalJobs: Int = 0,
    val averageFee: Long = 0,
    val collectionRate: Double = 0.0
)

@Singleton
class BookingRepository @Inject constructor(
    private val database: McJobIdDatabase,
    private val bookingDao: BookingDao,
    private val clientDao: ClientDao,
    private val paymentDao: PaymentDao,
    private val reminderDao: ReminderDao,
    private val expenseDao: ExpenseDao,
    private val checklistDao: ChecklistDao,
    private val invoiceDao: InvoiceDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val context: Context
) {
    
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    /** Baca set hari pengingat aktif dari DataStore. Default H-1 jika belum diatur. */
    private suspend fun getReminderDaysSet(): Set<Int> {
        val prefs = context.settingsDataStore.data.first()
        val raw = prefs[SettingsKeys.REMINDER_DAYS_SET]
        return if (!raw.isNullOrBlank()) {
            raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else {
            val legacy = prefs[SettingsKeys.EVENT_REMINDER_DAYS]
            if (legacy != null) setOf(legacy) else setOf(1)
        }
    }
    
    fun getAllBookings(): Flow<List<Booking>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.getAllBookingsByOwner(uid).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.getAllBookings().map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }
    
    fun getActiveBookings(): Flow<List<Booking>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.getActiveBookingsByOwner(uid).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.getActiveBookings().map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }
    
    fun getHistoryBookings(): Flow<List<Booking>> {
        val todayStr = LocalDate.now().toString()
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.getHistoryBookingsByOwner(todayStr, uid).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.getHistoryBookings(todayStr).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }
    
    suspend fun getBookingById(id: String): Booking? {
        return bookingDao.getBookingById(id)?.let { Booking.fromEntity(it) }
    }
    
    fun getBookingsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Booking>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.getBookingsByDateRangeByOwner(
                startDate.format(formatter),
                endDate.format(formatter),
                uid
            ).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.getBookingsByDateRange(
                startDate.format(formatter),
                endDate.format(formatter)
            ).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }
    
    fun getBookingsByDate(date: LocalDate): Flow<List<Booking>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.getBookingsByDateByOwner(date.format(formatter), uid).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.getBookingsByDate(date.format(formatter)).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }

    fun getBookingsByClient(clientName: String, clientId: String = ""): Flow<List<Booking>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.getBookingsByClientByOwner(clientName, uid, clientId).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.getBookingsByClient(clientName, clientId).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }

    fun searchBookings(query: String): Flow<List<Booking>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            bookingDao.searchBookingsByOwner(query, uid).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        } else {
            bookingDao.searchBookings(query).map { entities ->
                entities.map { Booking.fromEntity(it) }
            }
        }
    }

    // Smart Time Conflict Detection: A.start < B.end AND A.end > B.start
    suspend fun checkScheduleConflict(
        date: LocalDate,
        newStartStr: String? = null,
        newEndStr: String? = null,
        excludeId: String = ""
    ): List<Booking> {
        val uid = getCurrentUserId()
        val sameDayBookings = if (uid.isNotBlank()) {
            bookingDao.getBookingsOnDateByOwner(date.toString(), uid, excludeId)
        } else {
            bookingDao.getBookingsOnDate(date.toString(), excludeId)
        }.map { Booking.fromEntity(it) }
            .filter { it.status != Booking.BookingStatus.CANCELLED }

        if (newStartStr == null || newEndStr == null) {
            return sameDayBookings
        }

        return try {
            val newStart = LocalTime.parse(newStartStr)
            val newEnd = LocalTime.parse(newEndStr)

            sameDayBookings.filter { existing ->
                if (existing.start != null && existing.end != null) {
                    val exStart = LocalTime.parse(existing.start)
                    val exEnd = LocalTime.parse(existing.end)
                    newStart.isBefore(exEnd) && newEnd.isAfter(exStart)
                } else {
                    true
                }
            }
        } catch (e: Exception) {
            sameDayBookings
        }
    }
    
    suspend fun insertBooking(booking: Booking) {
        val uid = getCurrentUserId()
        val entity = booking.toEntity().copy(ownerId = uid)
        database.withTransaction {
            bookingDao.insertBooking(entity)
            if (booking.dp > 0) {
                val newPayment = com.isankamil.mcjobid.data.local.entity.PaymentEntity(
                    id = "pay_init_${booking.id}",
                    ownerId = uid,
                    bookingId = booking.id,
                    amount = booking.dp,
                    paymentDate = booking.date.toString(),
                    paymentMethod = "Bank Transfer",
                    notes = "Pembayaran DP Awal",
                    createdAt = LocalDateTime.now().toString()
                )
                paymentDao.insertPayment(newPayment)
                try { firestoreSyncService.savePaymentToFirestore(newPayment) } catch (_: Exception) {}
            }
        }
        firestoreSyncService.saveBookingToFirestore(entity)
    }
    
    suspend fun insertBookings(bookings: List<Booking>) {
        val uid = getCurrentUserId()
        val entities = bookings.map { it.toEntity().copy(ownerId = uid) }
        bookingDao.insertBookings(entities)
        entities.forEach { firestoreSyncService.saveBookingToFirestore(it) }
    }
    
    suspend fun updateBooking(booking: Booking) {
        val uid = getCurrentUserId()
        val entity = booking.toEntity().copy(ownerId = uid)
        
        database.withTransaction {
            bookingDao.updateBooking(entity)

            // Update associated Auto-Invoice if exists
            val existingInvoice = invoiceDao.getInvoiceByBookingId(booking.id)
            if (existingInvoice != null) {
                val updatedInvoice = existingInvoice.copy(
                    totalAmount = booking.fee,
                    dpAmount = booking.dp,
                    remainingAmount = maxOf(0L, booking.fee - booking.dp),
                    dueDate = booking.date.toString(),
                    notes = booking.note,
                    createdAt = LocalDateTime.now().toString()
                )
                invoiceDao.updateInvoice(updatedInvoice)
                try { firestoreSyncService.saveInvoiceToFirestore(updatedInvoice) } catch (_: Exception) {}
            }

            // Update or Insert initial DP Payment if DP > 0
            if (booking.dp > 0) {
                val existingPayment = paymentDao.getPaymentById("pay_init_${booking.id}")
                if (existingPayment != null) {
                    val updatedPayment = existingPayment.copy(
                        amount = booking.dp,
                        paymentDate = booking.date.toString(),
                        createdAt = LocalDateTime.now().toString()
                    )
                    paymentDao.insertPayment(updatedPayment)
                    try { firestoreSyncService.savePaymentToFirestore(updatedPayment) } catch (_: Exception) {}
                } else {
                    val newPayment = PaymentEntity(
                        id = "pay_init_${booking.id}",
                        ownerId = uid,
                        bookingId = booking.id,
                        amount = booking.dp,
                        paymentDate = booking.date.toString(),
                        paymentMethod = "Bank Transfer",
                        notes = "Pembayaran DP Awal",
                        createdAt = LocalDateTime.now().toString()
                    )
                    paymentDao.insertPayment(newPayment)
                    try { firestoreSyncService.savePaymentToFirestore(newPayment) } catch (_: Exception) {}
                }
            }
        }

        try {
            firestoreSyncService.saveBookingToFirestore(entity)
            val scheduler = NotificationScheduler(context)
            val activeDays = getReminderDaysSet()
            scheduler.cancelBookingReminders(booking.id)
            scheduler.scheduleEventReminders(booking, activeDays)
            if (booking.outstanding > 0) {
                scheduler.schedulePaymentReminder(booking)
            }
        } catch (e: Exception) {
            // Background sync failure handled gracefully
        }
    }
    
    suspend fun deleteBooking(booking: Booking) {
        deleteBookingById(booking.id)
    }
    
    suspend fun deleteBookingById(id: String) {
        // Capture associated child entity IDs BEFORE Room transaction deletes them locally
        val invoiceIdToDelete = invoiceDao.getInvoiceByBookingId(id)?.id
        val paymentIdsToDelete = paymentDao.getPaymentsListByBooking(id).map { it.id }
        val reminderIdsToDelete = reminderDao.getRemindersListByBooking(id).map { it.id }
        val checklistIdsToDelete = checklistDao.getChecklistListByBooking(id).map { it.id }

        database.withTransaction {
            bookingDao.deleteBookingById(id)
            paymentDao.deletePaymentsByBooking(id)
            reminderDao.deleteRemindersByBooking(id)
            checklistDao.deleteChecklistForBooking(id)
            invoiceDao.getInvoiceByBookingId(id)?.let {
                invoiceDao.deleteInvoice(it)
            }
        }
        try {
            // Delete all associated child entities from Firestore (or queue for offline sync)
            paymentIdsToDelete.forEach { payId ->
                firestoreSyncService.deletePaymentFromFirestore(payId)
            }
            reminderIdsToDelete.forEach { remId ->
                firestoreSyncService.deleteReminderFromFirestore(remId)
            }
            checklistIdsToDelete.forEach { chkId ->
                firestoreSyncService.deleteChecklistItemFromFirestore(chkId)
            }
            if (invoiceIdToDelete != null) {
                firestoreSyncService.deleteInvoiceFromFirestore(invoiceIdToDelete)
            }
            firestoreSyncService.deleteBookingFromFirestore(id)
            NotificationScheduler(context).cancelBookingReminders(id)
        } catch (e: Exception) {
            // Remote sync resilience handled by SyncQueue
        }
    }

    suspend fun duplicateJob(originalBookingId: String, newDate: LocalDate): Booking? {
        val original = getBookingById(originalBookingId) ?: return null
        val now = LocalDateTime.now()
        val uid = getCurrentUserId()
        val newId = System.currentTimeMillis().toString()

        val duplicated = original.copy(
            id = newId,
            date = newDate,
            dp = 0L,
            status = Booking.BookingStatus.CONFIRMED,
            createdAt = now,
            updatedAt = now
        )

        val entity = duplicated.toEntity().copy(ownerId = uid)

        database.withTransaction {
            bookingDao.insertBooking(entity)

            // Copy default checklist from the original booking
            val sourceChecklists = checklistDao.getChecklistByBooking(originalBookingId).first()
            sourceChecklists.forEachIndexed { idx, item ->
                checklistDao.insertChecklistItem(
                    item.copy(
                        id = "chk_${newId}_${idx}",
                        ownerId = uid,
                        bookingId = newId,
                        isCompleted = false,
                        createdAt = now.toString()
                    )
                )
            }

            // Copy reminders (fresh targetDate aligned to the new booking date)
            val sourceReminders = reminderDao.getRemindersByBooking(originalBookingId).first()
            sourceReminders.forEachIndexed { idx, rem ->
                val offsetDays = when (rem.reminderType) {
                    "H-14" -> -14; "H-7" -> -7; "H-3" -> -3; "H-1" -> -1
                    "TODAY" -> 0; "H+1" -> 1; "PAYMENT_OVERDUE" -> 3
                    else -> 0
                }
                reminderDao.insertReminder(
                    rem.copy(
                        id = "rem_${newId}_$idx",
                        ownerId = uid,
                        bookingId = newId,
                        targetDate = newDate.plusDays(offsetDays.toLong()).toString(),
                        isRead = false,
                        isDismissed = false,
                        createdAt = now.toString()
                    )
                )
            }

            // Auto-draft a fresh invoice for the duplicated job
            val dupCount = invoiceDao.getInvoiceCountByOwner(uid)
            val dupSeq = (dupCount + 1).toString().padStart(4, '0')
            val invoiceNumber = "INV-${now.year}-$dupSeq"
            invoiceDao.insertInvoice(
                InvoiceEntity(
                    id = "inv_auto_$newId",
                    ownerId = uid,
                    invoiceNumber = invoiceNumber,
                    bookingId = newId,
                    issueDate = LocalDate.now().toString(),
                    dueDate = newDate.toString(),
                    status = "DRAFT",
                    totalAmount = duplicated.fee,
                    dpAmount = 0L,
                    remainingAmount = duplicated.fee,
                    notes = duplicated.note,
                    createdAt = now.toString()
                )
            )
        }

        // Remote sync + alarm scheduling
        try {
            firestoreSyncService.saveBookingToFirestore(entity)
            // Retrieve the invoice just inserted into Room to get the correct invoice number
            val insertedInvoice = invoiceDao.getInvoiceByBookingId(newId)
            if (insertedInvoice != null) {
                firestoreSyncService.saveInvoiceToFirestore(insertedInvoice)
            }
            val scheduler = NotificationScheduler(context)
            val activeDays = getReminderDaysSet()
            scheduler.scheduleEventReminders(duplicated, activeDays)
            if (duplicated.outstanding > 0) {
                scheduler.schedulePaymentReminder(duplicated)
            }
        } catch (e: Exception) {
            // Background sync failure handled gracefully
        }

        return duplicated
    }

    enum class TimeFilter {
        TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL, CUSTOM_RANGE
    }

    suspend fun getFinancialAnalytics(): FinancialAnalytics {
        val allBookings = getAllBookings().first().filter { it.status != Booking.BookingStatus.CANCELLED }
        if (allBookings.isEmpty()) {
            return FinancialAnalytics()
        }

        val today = LocalDate.now()
        val currentMonthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val lastMonthStr = today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))

        val currentMonthBookings = allBookings.filter { it.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) == currentMonthStr }
        val lastMonthBookings = allBookings.filter { it.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) == lastMonthStr }

        val thisMonthRev = currentMonthBookings.sumOf { it.fee }
        val lastMonthRev = lastMonthBookings.sumOf { it.fee }

        val growth = if (lastMonthRev > 0) {
            ((thisMonthRev.toDouble() - lastMonthRev.toDouble()) / lastMonthRev.toDouble()) * 100.0
        } else {
            0.0
        }

        val totalRev = allBookings.sumOf { it.fee }
        val totalPaid = allBookings.sumOf { it.dp }
        val totalOutstanding = allBookings.sumOf { it.outstanding }
        val totalExpenses = expenseDao.getTotalExpenses() ?: 0L
        val netIncome = totalRev - totalExpenses

        val avgFee = if (allBookings.isNotEmpty()) totalRev / allBookings.size else 0L
        val rate = if (totalRev > 0) (totalPaid.toDouble() / totalRev.toDouble()) * 100.0 else 0.0

        return FinancialAnalytics(
            grossRevenue = totalRev,
            totalExpenses = totalExpenses,
            netIncome = netIncome,
            growthPercentage = growth,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            totalJobs = allBookings.size,
            averageFee = avgFee,
            collectionRate = rate
        )
    }
    
    suspend fun getFinancialSummaryFiltered(filter: TimeFilter): FinancialSummary {
        val today = LocalDate.now()
        val allBookings = getAllBookings().first()
        
        val filtered = when (filter) {
            TimeFilter.TODAY -> allBookings.filter { it.date == today && it.status != Booking.BookingStatus.CANCELLED }
            TimeFilter.THIS_WEEK -> {
                val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                allBookings.filter { !it.date.isBefore(startOfWeek) && !it.date.isAfter(endOfWeek) && it.status != Booking.BookingStatus.CANCELLED }
            }
            TimeFilter.THIS_MONTH -> {
                val currentMonthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                allBookings.filter { it.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) == currentMonthStr && it.status != Booking.BookingStatus.CANCELLED }
            }
            TimeFilter.THIS_YEAR -> {
                allBookings.filter { it.date.year == today.year && it.status != Booking.BookingStatus.CANCELLED }
            }
            TimeFilter.CUSTOM_RANGE -> allBookings.filter { it.status != Booking.BookingStatus.CANCELLED }
            TimeFilter.ALL -> allBookings.filter { it.status != Booking.BookingStatus.CANCELLED }
        }
        
        val totalHonor = filtered.sumOf { it.fee }
        val totalPaid = filtered.sumOf { it.dp }
        val totalOutstanding = filtered.sumOf { it.outstanding }
        
        return FinancialSummary(
            totalHonor = totalHonor,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            month = if (filter == TimeFilter.THIS_MONTH) today.format(DateTimeFormatter.ofPattern("yyyy-MM")) else null,
            year = today.year
        )
    }

    suspend fun createNewBooking(
        name: String,
        client: String? = null,
        clientId: String? = null,
        category: String = "Wedding",
        date: LocalDate,
        start: String? = null,
        end: String? = null,
        location: String? = null,
        address: String? = null,
        dresscode: String? = null,
        theme: String? = null,
        mcType: String? = "Single",
        language: String? = "Bahasa Indonesia",
        audience: String? = null,
        specialRequest: String? = null,
        pic: String? = null,
        fee: Long = 0,
        dp: Long = 0,
        note: String? = null
    ): Booking {
        val now = LocalDateTime.now()
        val id = System.currentTimeMillis().toString()
        val uid = getCurrentUserId()
        
        val booking = Booking(
            id = id,
            ownerId = uid,
            name = name,
            client = client,
            clientId = clientId,
            category = category,
            date = date,
            start = start,
            end = end,
            location = location,
            address = address,
            dresscode = dresscode,
            theme = theme,
            mcType = mcType,
            language = language,
            audience = audience,
            specialRequest = specialRequest,
            pic = pic,
            fee = fee,
            dp = dp,
            note = note,
            status = Booking.BookingStatus.CONFIRMED,
            createdAt = now,
            updatedAt = now
        )
        
        val entity = booking.toEntity().copy(ownerId = uid)
        
        // Generate unique invoice number: sequential count scoped to user + year
        val existingCount = invoiceDao.getInvoiceCountByOwner(uid)
        val invoiceSeq = (existingCount + 1).toString().padStart(4, '0')
        val invoiceNumber = "INV-${now.year}-$invoiceSeq"
        val invoiceId = "inv_auto_$id"

        database.withTransaction {
            bookingDao.insertBooking(entity)

            if (dp > 0) {
                val payment = PaymentEntity(
                    id = "pay_init_$id",
                    ownerId = uid,
                    bookingId = id,
                    amount = dp,
                    paymentDate = date.toString(),
                    paymentMethod = "Bank Transfer",
                    notes = "Pembayaran DP Awal",
                    createdAt = now.toString()
                )
                paymentDao.insertPayment(payment)
            }

            // Seed default checklist for new job
            val defaults = listOf(
                "Konfirmasi PIC / Wedding Organizer",
                "Cek rundown acara & skenario",
                "Cek dresscode & wardrobe MC",
                "Cek lokasi & rute perjalanan venue",
                "Siapkan script & cue card MC"
            )
            defaults.forEachIndexed { idx, chk ->
                checklistDao.insertChecklistItem(
                    ChecklistEntity(
                        id = "chk_${id}_$idx",
                        ownerId = uid,
                        bookingId = id,
                        title = chk,
                        isCompleted = false,
                        sortOrder = idx,
                        createdAt = now.toString()
                    )
                )
            }

            // Generate automated reminders
            val reminderDate = date.minusDays(3).toString()
            val reminder = ReminderEntity(
                id = "rem_$id",
                ownerId = uid,
                bookingId = id,
                title = "H-3 $name",
                message = "Cek rundown, dresscode ($dresscode), dan kebutuhan acara di $location",
                reminderType = "H-3",
                targetDate = reminderDate,
                isRead = false,
                isDismissed = false,
                createdAt = now.toString()
            )
            reminderDao.insertReminder(reminder)

            // Auto-Draft Invoice — uses pre-generated invoiceNumber for consistency
            val invoice = InvoiceEntity(
                id = invoiceId,
                ownerId = uid,
                invoiceNumber = invoiceNumber,
                bookingId = id,
                issueDate = LocalDate.now().toString(),
                dueDate = date.toString(),
                status = "DRAFT",
                totalAmount = fee,
                dpAmount = dp,
                remainingAmount = maxOf(0L, fee - dp),
                notes = note,
                createdAt = now.toString()
            )
            invoiceDao.insertInvoice(invoice)
        }

        // Remote Firestore sync with failure resilience
        try {
            firestoreSyncService.saveBookingToFirestore(entity)

            if (dp > 0) {
                val payment = PaymentEntity(
                    id = "pay_init_$id",
                    ownerId = uid,
                    bookingId = id,
                    amount = dp,
                    paymentDate = date.toString(),
                    paymentMethod = "Bank Transfer",
                    notes = "Pembayaran DP Awal",
                    createdAt = now.toString()
                )
                firestoreSyncService.savePaymentToFirestore(payment)
            }

            val reminderDate = date.minusDays(3).toString()
            val reminder = ReminderEntity(
                id = "rem_$id",
                ownerId = uid,
                bookingId = id,
                title = "H-3 $name",
                message = "Cek rundown, dresscode ($dresscode), dan kebutuhan acara di $location",
                reminderType = "H-3",
                targetDate = reminderDate,
                isRead = false,
                isDismissed = false,
                createdAt = now.toString()
            )
            firestoreSyncService.saveReminderToFirestore(reminder)

            val invoiceForFirestore = InvoiceEntity(
                id = "inv_auto_$id",
                ownerId = uid,
                invoiceNumber = invoiceNumber,  // Reuse same number from Room insert above
                bookingId = id,
                issueDate = LocalDate.now().toString(),
                dueDate = date.toString(),
                status = "DRAFT",
                totalAmount = fee,
                dpAmount = dp,
                remainingAmount = maxOf(0L, fee - dp),
                notes = note,
                createdAt = now.toString()
            )
            firestoreSyncService.saveInvoiceToFirestore(invoiceForFirestore)
        } catch (e: Exception) {
            // Background sync failure handled gracefully
        }

        // Schedule system AlarmManager reminders
        try {
            val scheduler = NotificationScheduler(context)
            val activeDays = getReminderDaysSet()
            scheduler.cancelBookingReminders(booking.id)
            scheduler.scheduleEventReminders(booking, activeDays)
            if (booking.outstanding > 0) {
                scheduler.schedulePaymentReminder(booking)
            }
        } catch (e: Exception) {
            // Fallback if exact alarm permissions not available
        }

        return booking
    }
}
