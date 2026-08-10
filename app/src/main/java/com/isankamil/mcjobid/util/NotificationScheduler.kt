package com.isankamil.mcjobid.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.MainActivity
import java.time.ZoneId
import java.util.Calendar

class NotificationScheduler(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "mcjobid_reminders"
        private const val CHANNEL_NAME = "Job Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming jobs and payment reminders"
        
        private const val NOTIFICATION_ID_BASE = 1000
        private const val REQUEST_CODE_BASE = 2000
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Jadwalkan alarm untuk setiap hari yang ada di [activeDays].
     * [activeDays] adalah Set<Int> offset hari sebelum acara:
     *   0 = Hari-H, 1 = H-1, 2 = H-2, 5 = H-5, 7 = H-7, dst.
     * Jika [activeDays] kosong, gunakan default {1}.
     */
    fun scheduleEventReminders(booking: Booking, activeDays: Set<Int> = setOf(1)) {
        val days = activeDays.ifEmpty { setOf(1) }
        days.forEach { dayOffset ->
            val message = when (dayOffset) {
                0    -> "Event hari ini! Persiapkan peralatan Anda."
                1    -> "Event besok! Jangan lupa gladi bersih."
                2    -> "Event 2 hari lagi! Periksa kelengkapan."
                3    -> "Event 3 hari lagi! Konfirmasi sound system."
                5    -> "Event 5 hari lagi! Persiapkan rundown acara."
                7    -> "Event seminggu lagi! Hubungi panitia."
                else -> "Event dalam $dayOffset hari lagi!"
            }
            scheduleReminder(booking, -dayOffset, message)
        }
    }
    
    private fun scheduleReminder(booking: Booking, daysOffset: Int, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
val calendar = Calendar.getInstance().apply {
            time = java.util.Date.from(
                booking.date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            )
            add(Calendar.DAY_OF_MONTH, daysOffset)
            
            // Set time to 9:00 AM for reminders
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Skip reminders whose trigger time is already in the past (e.g. H-7
        // of a booking created less than 7 days before the event).
        if (calendar.timeInMillis <= System.currentTimeMillis()) return
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("booking_id", booking.id)
            putExtra("booking_name", booking.name)
            putExtra("message", message)
            putExtra("notification_id", NOTIFICATION_ID_BASE + booking.id.hashCode() + daysOffset)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + booking.id.hashCode() + daysOffset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Handle case where exact alarm permission is not granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun schedulePaymentReminder(booking: Booking) {
        if (booking.outstanding > 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Schedule reminder 3 days after event
val calendar = Calendar.getInstance().apply {
                time = java.util.Date.from(
                    booking.date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
                add(Calendar.DAY_OF_MONTH, 3)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // Skip if event is already more than 3 days in the past
            if (calendar.timeInMillis <= System.currentTimeMillis()) return
            
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("booking_id", booking.id)
                putExtra("booking_name", booking.name)
                putExtra("message", "Tagihan jatuh tempo: ${Formatter.formatCurrency(booking.outstanding)}")
                putExtra("notification_id", NOTIFICATION_ID_BASE + booking.id.hashCode() + 100)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + booking.id.hashCode() + 100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelBookingReminders(bookingId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancel semua offset yang mungkin (0..30) + payment offset (100)
        // Ini memastikan tidak ada alarm zombie walau setting hari berubah
        val allPossibleOffsets = (0..30).map { -it } + listOf(100)
        for (offset in allPossibleOffsets) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + bookingId.hashCode() + offset,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue
            alarmManager.cancel(pendingIntent)
        }
    }
    
    fun showNotification(
        bookingId: String,
        bookingName: String,
        message: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("booking_id", bookingId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(bookingName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}

class ReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bookingId = intent.getStringExtra("booking_id") ?: return
        val bookingName = intent.getStringExtra("booking_name") ?: return
        val message = intent.getStringExtra("message") ?: return
        val notificationId = intent.getIntExtra("notification_id", 0)
        
        val scheduler = NotificationScheduler(context)
        scheduler.showNotification(bookingId, bookingName, message, notificationId)
    }
}

