package com.isankamil.mcjobid.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import com.isankamil.mcjobid.domain.model.Booking
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class CalendarIntegration(private val context: Context) {
    
    fun addToCalendar(booking: Booking) {
        addBookingToCalendar(
            title = booking.name,
            date = booking.date,
            startTime = booking.start,
            endTime = booking.end,
            location = booking.location,
            description = buildEventDescription(booking)
        )
    }

    fun addFormDetailsToCalendar(
        title: String,
        date: LocalDate,
        startTime: String?,
        endTime: String?,
        location: String?,
        client: String?,
        pic: String?,
        dresscode: String?,
        theme: String?,
        mcType: String?,
        language: String?,
        fee: Long,
        dp: Long,
        note: String?
    ) {
        val desc = StringBuilder()
        desc.append("Event: $title\n")
        if (!client.isNullOrBlank()) desc.append("Klien: $client\n")
        if (!pic.isNullOrBlank()) desc.append("PIC / WO: $pic\n")
        if (!dresscode.isNullOrBlank()) desc.append("Dresscode: $dresscode\n")
        if (!theme.isNullOrBlank()) desc.append("Tema: $theme\n")
        if (!mcType.isNullOrBlank()) desc.append("Jenis MC: $mcType\n")
        if (!language.isNullOrBlank()) desc.append("Bahasa: $language\n")
        desc.append("Tanggal: ${Formatter.formatDate(date)}\n")
        if (!startTime.isNullOrBlank() && !endTime.isNullOrBlank()) {
            desc.append("Waktu: $startTime - $endTime\n")
        }
        if (!location.isNullOrBlank()) desc.append("Lokasi: $location\n")
        desc.append("\nDetail Keuangan:\n")
        desc.append("Total Honor: ${Formatter.formatCurrency(fee)}\n")
        desc.append("DP / Terbayar: ${Formatter.formatCurrency(dp)}\n")
        val sisa = maxOf(0L, fee - dp)
        if (sisa > 0) {
            desc.append("Sisa Piutang: ${Formatter.formatCurrency(sisa)}\n")
        }
        if (!note.isNullOrBlank()) {
            desc.append("\nCatatan Brief: $note\n")
        }
        desc.append("\n---\nDicatat via mcjob.id")

        addBookingToCalendar(
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            location = location,
            description = desc.toString()
        )
    }

    fun addBookingToCalendar(
        title: String,
        date: LocalDate,
        startTime: String?,
        endTime: String?,
        location: String?,
        description: String?
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description ?: "Agenda via mcjob.id")
            putExtra(CalendarContract.Events.EVENT_LOCATION, location ?: "")
            
            // Set start time
            val startCalendar = Calendar.getInstance().apply {
                time = java.util.Date.from(
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
                startTime?.let { sTime ->
                    try {
                        val (hour, minute) = sTime.split(":").map { it.toInt() }
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    } catch (e: Exception) {
                        Log.e("CalendarIntegration", "Invalid start time format: $sTime", e)
                    }
                }
            }
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCalendar.timeInMillis)
            
            // Set end time
            val endCalendar = Calendar.getInstance().apply {
                time = java.util.Date.from(
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
                endTime?.let { eTime ->
                    try {
                        val (hour, minute) = eTime.split(":").map { it.toInt() }
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    } catch (e: Exception) {
                        Log.e("CalendarIntegration", "Invalid end time format: $eTime", e)
                    }
                } ?: run {
                    add(Calendar.HOUR, 2)
                }
            }
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCalendar.timeInMillis)
            
            if (startTime == null && endTime == null) {
                putExtra(CalendarContract.Events.ALL_DAY, true)
            }
        }
        
        try {
            val chooser = Intent.createChooser(intent, "Tambah ke Kalender")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("CalendarIntegration", "Gagal menambahkan ke kalender", e)
        }
    }
    
    private fun buildEventDescription(booking: Booking): String {
        val description = StringBuilder()
        
        description.append("Event: ${booking.name}\n")
        
        booking.client?.let {
            description.append("Client: $it\n")
        }
        
        booking.pic?.let {
            description.append("PIC: $it\n")
        }
        
        booking.dresscode?.let {
            description.append("Dresscode: $it\n")
        }
        
        val formattedDate = Formatter.formatDate(booking.date)
        description.append("Date: $formattedDate\n")
        
        if (booking.start != null && booking.end != null) {
            description.append("Time: ${booking.start} - ${booking.end}\n")
        }
        
        booking.location?.let {
            description.append("Location: $it\n")
        }
        
        description.append("\nFinancial Details:\n")
        description.append("Total Honor: ${Formatter.formatCurrency(booking.fee)}\n")
        description.append("Paid: ${Formatter.formatCurrency(booking.dp)}\n")
        
        if (booking.outstanding > 0) {
            description.append("Outstanding: ${Formatter.formatCurrency(booking.outstanding)}\n")
        }
        
        booking.note?.let {
            description.append("\nNotes: $it")
        }
        
        description.append("\n\n---\nGenerated by mcjob.id")
        
        return description.toString()
    }
}
