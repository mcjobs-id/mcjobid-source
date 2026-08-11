package com.isankamil.mcjobid.ui.screen.agenda

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.components.CalendarView
import com.isankamil.mcjobid.ui.components.EmptyStateView
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel,
    onBookingClick: (String) -> Unit,
    onAddJobClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilterTab by viewModel.selectedFilterTab.collectAsState()
    val isCalendarView by viewModel.isCalendarView.collectAsState()
    val calendarMode by viewModel.calendarMode.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val bookings by viewModel.filteredBookings.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteMessageInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Semua Job & Agenda",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleCalendarView() }) {
                        Icon(
                            imageVector = if (isCalendarView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.CalendarMonth,
                            contentDescription = "Toggle View",
                            tint = Primary
                        )
                    }
                    IconButton(onClick = onAddJobClick) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Job", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A),
                    actionIconContentColor = Primary
                ),
                windowInsets = TopAppBarDefaults.windowInsets // Explicitly ensure insets are handled
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Search Bar & View Mode
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { 
                            Text(
                                "Cari nama event, klien, atau lokasi...", 
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(JobFilterTab.values()) { tab ->
                            val label = when (tab) {
                                JobFilterTab.ALL -> "Semua"
                                JobFilterTab.UPCOMING -> "Akan Datang"
                                JobFilterTab.TODAY -> "Hari Ini"
                                JobFilterTab.COMPLETED -> "Selesai"
                                JobFilterTab.UNPAID -> "Belum Lunas"
                                JobFilterTab.CANCELLED -> "Dibatalkan"
                            }
                            val isSelected = selectedFilterTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilterTab(tab) },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF64748B)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = Primary,
                                    borderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isCalendarView) {
                // Mode Selector Bar (Month / Week / Day)
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            CalendarMode.MONTH to "Bulan",
                            CalendarMode.WEEK to "Minggu",
                            CalendarMode.DAY to "Hari"
                        ).forEach { (mode, label) ->
                            val isSelected = calendarMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCalendarMode(mode) },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF64748B)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = Primary,
                                    borderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CalendarView(
                            currentMonth = currentMonth,
                            onMonthChange = { viewModel.setCurrentMonth(it) },
                            bookings = allBookings,
                            reminders = allReminders,
                            onDateClick = { viewModel.setSelectedDate(it) },
                            selectedDate = selectedDate
                        )
                    }

                    selectedDate?.let { date ->
                        val dateBookings = allBookings.filter { it.date == date }
                        val dateReminders = allReminders.filter { it.targetDate == date.toString() }
                        val dateNotes = dateReminders.filter { it.reminderType == "DATE_NOTE" || it.id.startsWith("note_") }
                        val dateAlerts = dateReminders.filterNot { it.reminderType == "DATE_NOTE" || it.id.startsWith("note_") }

                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = Formatter.formatDate(date),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${dateBookings.size} Job • ${dateNotes.size} Catatan",
                                                fontSize = 11.5.sp,
                                                color = OnSurfaceVariant
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(
                                                onClick = onAddJobClick,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("+ Job", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    noteTitleInput = ""
                                                    noteMessageInput = ""
                                                    showAddNoteDialog = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("+ Catatan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 1: Catatan Tanggal Ini
                        if (dateNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📌 Catatan & Memo Tanggal Ini (${dateNotes.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }

                            items(dateNotes) { note ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.PushPin, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface)
                                                if (note.message.isNotBlank()) {
                                                    Text(note.message, fontSize = 12.sp, color = OnSurfaceVariant)
                                                }
                                            }
                                        }
                                        IconButton(onClick = { viewModel.dismissReminder(note.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Catatan", tint = Error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Job Tanggal Ini
                        if (dateBookings.isEmpty() && dateNotes.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Belum ada agenda atau catatan untuk tanggal ini.", fontSize = 12.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else if (dateBookings.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🎤 Job & Event Tanggal Ini (${dateBookings.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }

                            items(dateBookings) { booking ->
                                AgendaJobCard(
                                    booking = booking,
                                    onClick = { onBookingClick(booking.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                // List View
                if (bookings.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        title = "Belum Ada Job",
                        description = "Mulai catat pekerjaan pertamamu dan biarkan MCJOBID membantu mengaturnya.",
                        actionText = "Catat Job Pertama",
                        onActionClick = onAddJobClick
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            AgendaJobCard(
                                booking = booking,
                                onClick = { onBookingClick(booking.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog && selectedDate != null) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Catatan Tanggal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnBackground)
                        Text(Formatter.formatDate(selectedDate!!), fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Tambahkan catatan/memo khusus untuk tanggal ini (misal: Gladi bersih, fitting jas, meeting vendor).",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text("Judul Catatan / Agenda") },
                        placeholder = { Text("Contoh: Gladi Bersih Jam 19:00") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteMessageInput,
                        onValueChange = { noteMessageInput = it },
                        label = { Text("Detail Catatan / Pesan Pengingat") },
                        placeholder = { Text("Contoh: Lokasi Hall A, Bawa Wardrobe Jas Hitam") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitleInput.trim().isNotBlank()) {
                            viewModel.addDateNote(
                                date = selectedDate!!,
                                title = noteTitleInput.trim(),
                                message = noteMessageInput.trim(),
                                context = context
                            )
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Catatan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Batal", color = Secondary, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun AgendaJobCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    modifier = Modifier.weight(1f)
                )

                val (badgeBg, badgeText, badgeColor) = when (booking.paymentStatus) {
                    Booking.PaymentStatus.PAID -> Triple(Success.copy(alpha = 0.15f), "LUNAS", Success)
                    Booking.PaymentStatus.PARTIAL -> Triple(Warning.copy(alpha = 0.15f), "BELUM LUNAS", Warning)
                    else -> Triple(Error.copy(alpha = 0.15f), "BELUM LUNAS", Error)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.client ?: "Personal Klien",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${Formatter.formatDate(booking.date)} • ${booking.start ?: "19:00"} - ${booking.end ?: "22:00"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = booking.location ?: "Lokasi belum ditentukan",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val hasLocation = !booking.location.isNullOrBlank() || !booking.address.isNullOrBlank()
                if (hasLocation) {
                    Surface(
                        onClick = {
                            val query = listOfNotNull(booking.location?.trim()?.ifBlank { null }, booking.address?.trim()?.ifBlank { null }).joinToString(" ")
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"))
                                try { context.startActivity(webIntent) } catch (_: Exception) {}
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Petunjuk Maps", tint = Primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Honor", fontSize = 10.sp, color = OnSurfaceVariant)
                    Text(Formatter.formatCurrency(booking.fee), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("DP Masuk", fontSize = 10.sp, color = OnSurfaceVariant)
                    Text(Formatter.formatCurrency(booking.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Success)
                }
                Column {
                    Text("Sisa Piutang", fontSize = 10.sp, color = OnSurfaceVariant)
                    Text(Formatter.formatCurrency(booking.outstanding), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (booking.outstanding > 0) Warning else OnSurfaceVariant)
                }
            }
        }
    }
}
