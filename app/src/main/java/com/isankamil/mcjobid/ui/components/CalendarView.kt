package com.isankamil.mcjobid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.ui.util.getStatusColor
import com.isankamil.mcjobid.util.Formatter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.isankamil.mcjobid.domain.model.Reminder

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    bookings: List<Booking>,
    reminders: List<Reminder> = emptyList(),
    onDateClick: (LocalDate) -> Unit,
    selectedDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month header
            MonthHeader(
                currentMonth = currentMonth,
                onMonthChange = onMonthChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days of week header
            WeekDaysHeader()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                bookings = bookings,
                reminders = reminders,
                onDateClick = onDateClick,
                selectedDate = selectedDate
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Legend indicators footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendBadge(color = Success, label = "Lunas")
                LegendBadge(color = Warning, label = "Piutang")
                LegendBadge(color = Primary, label = "Job DP")
                LegendBadge(color = Info, label = "Catatan 📌")
            }
        }
    }
}

@Composable
fun LegendBadge(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MonthHeader(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month"
            )
        }
        
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month"
            )
        }
    }
}

@Composable
fun WeekDaysHeader() {
    val days = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    bookings: List<Booking>,
    reminders: List<Reminder>,
    onDateClick: (LocalDate) -> Unit,
    selectedDate: LocalDate?
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sunday
    
    // Group bookings and reminders by date
    val bookingsByDate = bookings.groupBy { it.date }
    val remindersByDate = reminders.groupBy { it.targetDate }
    
    Column {
        var currentCell = 0
        
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val dayNumber = currentCell - firstDayOfMonth + 1
                    val isValidDay = dayNumber in 1..daysInMonth
                    val date = if (isValidDay) {
                        currentMonth.atDay(dayNumber)
                    } else {
                        null
                    }
                    
                    CalendarDay(
                        date = date,
                        bookings = date?.let { bookingsByDate[it] } ?: emptyList(),
                        reminders = date?.let { remindersByDate[it.toString()] } ?: emptyList(),
                        isSelected = date == selectedDate,
                        isCurrentMonth = isValidDay,
                        onClick = { date?.let { onDateClick(it) } },
                        modifier = Modifier.weight(1f)
                    )
                    
                    currentCell++
                }
            }
            
            if (week < 5) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate?,
    bookings: List<Booking>,
    reminders: List<Reminder>,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val isToday = date == today
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Primary
                    isToday -> Primary.copy(alpha = 0.18f)
                    isCurrentMonth -> Color.White
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isSelected) 2.dp else if (isToday) 1.5.dp else 0.5.dp,
                color = if (isSelected) Primary else if (isToday) Primary else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isCurrentMonth && date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            if (date != null) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else if (isToday) Primary else OnBackground,
                    fontSize = 12.sp
                )
                
                // Indicators Row for Bookings + Reminders/Notes
                val hasNotes = reminders.any { it.reminderType == "DATE_NOTE" || it.id.startsWith("note_") }
                val hasReminders = reminders.any { it.reminderType != "DATE_NOTE" && !it.id.startsWith("note_") }

                if (bookings.isNotEmpty() || hasNotes || hasReminders) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bookings.take(2).forEach { booking ->
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else getStatusColor(booking.paymentStatus))
                            )
                        }
                        if (hasNotes) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else Info)
                            )
                        }
                        if (hasReminders && bookings.size < 2) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else Warning)
                            )
                        }
                        val totalCount = bookings.size + (if (hasNotes) 1 else 0) + (if (hasReminders) 1 else 0)
                        if (totalCount > 3) {
                            Text(
                                text = "+${totalCount - 2}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaList(
    selectedDate: LocalDate,
    bookings: List<Booking>,
    onBookingClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayBookings = bookings.filter { it.date == selectedDate }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = Formatter.formatDate(selectedDate),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (dayBookings.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceVariant
                )
            ) {
                Text(
                    text = "Tidak ada jadwal untuk tanggal ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            dayBookings.forEach { booking ->
                BookingCard(
                    booking = booking,
                    onEdit = { onBookingClick(booking) },
                    onDelete = { /* Handle delete */ },
                    onInvoice = { /* Handle invoice */ },
                    onAddToCalendar = { /* Handle calendar */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

