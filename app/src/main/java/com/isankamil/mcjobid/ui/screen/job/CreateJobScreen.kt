package com.isankamil.mcjobid.ui.screen.job

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.screen.booking.BookingFormViewModel
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.CalendarIntegration
import com.isankamil.mcjobid.util.Formatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(
    viewModel: BookingFormViewModel,
    onBack: () -> Unit,
    onJobSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val isEditMode by viewModel.isEditMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val createdBookingId by viewModel.createdBookingId.collectAsState()

    val selectedDate by viewModel.date.collectAsState()
    val allClients by viewModel.clients.collectAsState()

    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val firstErrorField by viewModel.firstErrorField.collectAsState()

    val showConflictDialog by viewModel.showConflictDialog.collectAsState()
    val conflictingBookings by viewModel.conflictingBookings.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showDraftDialog by remember { mutableStateOf(false) }

    // Auto-scroll to the first error item when validation fails
    LaunchedEffect(firstErrorField) {
        firstErrorField?.let { field ->
            when (field) {
                "name", "date", "startTime", "endTime" -> listState.animateScrollToItem(0)
                "clientName" -> listState.animateScrollToItem(1)
                "fee", "dp" -> listState.animateScrollToItem(3)
            }
        }
    }

    LaunchedEffect(createdBookingId) {
        createdBookingId?.let {
            showSaveConfirmation = true
        }
    }

    BackHandler(enabled = true) {
        if (viewModel.hasUnsavedChanges()) {
            showDraftDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Job" else "Catat Job Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            showDraftDialog = true
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: INFORMASI ACARA
                item {
                    FormSectionCard(
                        title = "INFORMASI ACARA",
                        icon = Icons.Default.Event
                    ) {
                        MCJobTextField(
                            value = viewModel.name.collectAsState().value,
                            onValueChange = {
                                viewModel.name.value = it
                                viewModel.clearFieldError("name")
                            },
                            label = "Nama Acara",
                            isRequired = true,
                            errorMessage = fieldErrors["name"],
                            placeholder = "Contoh: Wedding Kevin & Vania"
                        )

                        // PREMIUM DATE SELECTOR CARD
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Tanggal Acara",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fieldErrors.containsKey("date")) Error else OnSurfaceVariant
                                )
                            }

                            Card(
                                onClick = { showDatePicker = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (fieldErrors.containsKey("date")) Error else Primary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Primary.copy(alpha = 0.1f),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "TANGGAL ACARA",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary
                                            )
                                            Text(
                                                text = selectedDate?.let { Formatter.formatDate(it) } ?: "Pilih Tanggal Acara",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedDate != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Primary,
                                        onClick = { showDatePicker = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EditCalendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (selectedDate != null) "Ubah Tanggal" else "Pilih Tanggal",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            MCJobInlineError(errorMessage = fieldErrors["date"])
                        }

                        // Kategori Acara Hybrid Chips + Custom Input
                        val currentCategory = viewModel.category.collectAsState().value
                        val presetCategories = listOf("Wedding", "Corporate", "Birthday", "Seminar", "Concert", "Gathering")
                        val isCustomCategory = currentCategory.isNotBlank() && currentCategory !in presetCategories

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Kategori Acara", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(presetCategories) { cat ->
                                    val selected = currentCategory == cat
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.category.value = cat },
                                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Primary.copy(alpha = 0.07f),
                                            labelColor = Primary.copy(alpha = 0.8f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            selectedBorderColor = Color.Transparent,
                                            borderColor = Primary.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                                if (isCustomCategory) {
                                    item {
                                        FilterChip(
                                            selected = true,
                                            onClick = { },
                                            label = { Text("★ $currentCategory", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Primary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            MCJobTextField(
                                value = currentCategory,
                                onValueChange = { viewModel.category.value = it },
                                label = "Kategori Kustom",
                                placeholder = "Ketik kategori kustom jika tidak ada di chip"
                            )
                        }

                        // INTERACTIVE TIME SELECTOR CARDS (JAM MULAI & JAM SELESAI)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            // Jam Mulai Selector Card
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Jam Mulai", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (fieldErrors.containsKey("startTime")) Error else OnSurfaceVariant)
                                }
                                Surface(
                                    onClick = { showStartTimePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, if (fieldErrors.containsKey("startTime")) Error else Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = viewModel.startTime.collectAsState().value.ifBlank { "19:00" },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Primary
                                            )
                                        }
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                    }
                                }
                                MCJobInlineError(errorMessage = fieldErrors["startTime"])
                            }

                            // Jam Selesai Selector Card
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Jam Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (fieldErrors.containsKey("endTime")) Error else OnSurfaceVariant)
                                }
                                Surface(
                                    onClick = { showEndTimePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, if (fieldErrors.containsKey("endTime")) Error else Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = viewModel.endTime.collectAsState().value.ifBlank { "22:00" },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Primary
                                            )
                                        }
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                    }
                                }
                                MCJobInlineError(errorMessage = fieldErrors["endTime"])
                            }
                        }

                        // Quick Time Slot Preset Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Preset Jam Acara:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val timePresets = listOf(
                                    ("Pagi" to ("08:00" to "12:00")),
                                    ("Siang" to ("13:00" to "17:00")),
                                    ("Malam" to ("19:00" to "22:00")),
                                    ("Seharian" to ("08:00" to "22:00"))
                                )
                                items(timePresets) { (label, times) ->
                                    val (s, e) = times
                                    val isSelected = viewModel.startTime.collectAsState().value == s && viewModel.endTime.collectAsState().value == e
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setStartTime(s)
                                            viewModel.setEndTime(e)
                                        },
                                        label = { Text("$label ($s - $e)", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Primary.copy(alpha = 0.07f),
                                            labelColor = Primary.copy(alpha = 0.8f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            selectedBorderColor = Color.Transparent,
                                            borderColor = Primary.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                            }
                        }

                        val currentLoc = viewModel.location.collectAsState().value
                        val currentAddr = viewModel.address.collectAsState().value
                        var showMapPreviewDialog by remember { mutableStateOf(false) }

                        MCJobTextField(
                            value = currentLoc,
                            onValueChange = {
                                viewModel.location.value = it
                                viewModel.clearFieldError("location")
                            },
                            label = "Nama Lokasi / Venue",
                            isRequired = true,
                            errorMessage = fieldErrors["location"],
                            placeholder = "Contoh: Grand Ballroom Hotel Mulia"
                        )

                        MCJobTextField(
                            value = currentAddr,
                            onValueChange = {
                                viewModel.address.value = it
                                viewModel.clearFieldError("address")
                            },
                            label = "Alamat Lengkap Venue (Titik Peta GPS)",
                            isRequired = true,
                            errorMessage = fieldErrors["address"],
                            placeholder = "Contoh: Jl. Asia Afrika No.1, Senayan, Jakarta Pusat",
                            singleLine = false
                        )

                        // EMBEDDED LIVE GPS MAP PREVIEW CARD IN FORM
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PinDrop, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pratinjau Live Peta GPS Form", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Primary)
                                    }

                                    if (currentLoc.isNotBlank() || currentAddr.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Success.copy(alpha = 0.12f)
                                        ) {
                                            Text("✓ Titik Akurat", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Success, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }

                                val fullAddressQuery = listOfNotNull(currentLoc.trim().ifBlank { null }, currentAddr.trim().ifBlank { null }).joinToString(" - ")
                                if (fullAddressQuery.isNotBlank()) {
                                    Text(
                                        text = "📍 $fullAddressQuery",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 2
                                    )
                                } else {
                                    Text(
                                        text = "Ketik nama venue & alamat lengkap di atas untuk mengaktifkan titik GPS real-time.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (fullAddressQuery.isNotBlank()) {
                                                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(fullAddressQuery)}")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                mapIntent.setPackage("com.google.android.apps.maps")
                                                try {
                                                    context.startActivity(mapIntent)
                                                } catch (e: Exception) {
                                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(fullAddressQuery)}"))
                                                    try { context.startActivity(webIntent) } catch (_: Exception) {}
                                                }
                                            }
                                        },
                                        enabled = fullAddressQuery.isNotBlank(),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🗺️ Buka & Tes di Google Maps", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { showMapPreviewDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Detail", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                            }
                        }

                        // Map Preview & Testing Dialog
                        if (showMapPreviewDialog) {
                            val targetQuery = listOfNotNull(currentLoc.trim().ifBlank { null }, currentAddr.trim().ifBlank { null }).joinToString(" ")
                            AlertDialog(
                                onDismissRequest = { showMapPreviewDialog = false },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Primary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Map, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Pratinjau Titik Peta GPS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("Akurasi Integrasi Maps & Calendar", fontSize = 11.5.sp, color = OnSurfaceVariant)
                                        }
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Nama Venue:", fontSize = 11.sp, color = OnSurfaceVariant)
                                                Text(currentLoc.ifBlank { "Belum diisi" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Alamat Lengkap:", fontSize = 11.sp, color = OnSurfaceVariant)
                                                Text(currentAddr.ifBlank { "Belum diisi" }, fontSize = 12.sp, color = Color(0xFF0F172A))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Success.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, Success.copy(alpha = 0.2f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Titik lokasi presisi & siap tersinkron otomatis ke Google Calendar & Google Maps.",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF065F46)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (targetQuery.isNotBlank()) {
                                                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(targetQuery)}")
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                    mapIntent.setPackage("com.google.android.apps.maps")
                                                    try {
                                                        context.startActivity(mapIntent)
                                                    } catch (e: Exception) {
                                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(targetQuery)}"))
                                                        try { context.startActivity(webIntent) } catch (_: Exception) {}
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🗺️ Tes Akurasi di Google Maps", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showMapPreviewDialog = false }) {
                                        Text("Selesai", fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = Color.White
                            )
                        }
                    }
                }

                // Section 2: INFORMASI KLIEN & WO
                item {
                    FormSectionCard(
                        title = "INFORMASI KLIEN & WO",
                        icon = Icons.Default.Person
                    ) {
                        MCJobTextField(
                            value = viewModel.clientName.collectAsState().value,
                            onValueChange = {
                                viewModel.clientName.value = it
                                viewModel.clearFieldError("clientName")
                            },
                            label = "Nama Klien / Penyelenggara",
                            isRequired = true,
                            errorMessage = fieldErrors["clientName"],
                            placeholder = "Contoh: Kevin Sanjaya / PT Astra"
                        )

                        // Client Suggestions (Auto-complete)
                        val clientQuery = viewModel.clientName.collectAsState().value
                        val matchingClients = remember(allClients, clientQuery) {
                            if (clientQuery.isBlank()) emptyList()
                            else allClients.filter {
                                it.name.contains(clientQuery, ignoreCase = true) && !it.name.equals(clientQuery, ignoreCase = true)
                            }
                        }

                        if (matchingClients.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Pilih dari Klien Tersimpan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(matchingClients.take(4)) { c ->
                                        SuggestionChip(
                                            onClick = { viewModel.selectExistingClient(c) },
                                            label = {
                                                Text(
                                                    text = "${c.name} ${c.company?.let { "($it)" } ?: ""}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            icon = {
                                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        MCJobTextField(
                            value = viewModel.clientPhone.collectAsState().value,
                            onValueChange = { viewModel.clientPhone.value = it },
                            label = "No. WhatsApp Klien",
                            placeholder = "081234567890",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        MCJobTextField(
                            value = viewModel.clientCompany.collectAsState().value,
                            onValueChange = { viewModel.clientCompany.value = it },
                            label = "Perusahaan / Penyelenggara",
                            placeholder = "PT Aksara / Personal"
                        )

                        MCJobTextField(
                            value = viewModel.pic.collectAsState().value,
                            onValueChange = { viewModel.pic.value = it },
                            label = "Kontak PIC / WO",
                            placeholder = "Siska (Happy Wedding Organizer - 081987654321)"
                        )
                    }
                }

                // Section 3: DETAIL PROFESIONAL MC
                item {
                    FormSectionCard(
                        title = "DETAIL PROFESIONAL MC",
                        icon = Icons.Default.Mic
                    ) {
                        // Dresscode with Suggestion Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            MCJobTextField(
                                value = viewModel.dresscode.collectAsState().value,
                                onValueChange = { viewModel.dresscode.value = it },
                                label = "Dresscode",
                                placeholder = "Contoh: Black Tie / Batik Modern"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf("Black Tie", "Batik Modern", "Formal Suit", "Smart Casual", "White Elegance")) { dc ->
                                    SuggestionChip(
                                        onClick = { viewModel.dresscode.value = dc },
                                        label = { Text(dc, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Tema Acara with Suggestion Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            MCJobTextField(
                                value = viewModel.theme.collectAsState().value,
                                onValueChange = { viewModel.theme.value = it },
                                label = "Tema Acara",
                                placeholder = "Contoh: Modern Elegance"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf("Modern Elegance", "Traditional Formal", "Glamour Night", "Casual Outdoor")) { th ->
                                    SuggestionChip(
                                        onClick = { viewModel.theme.value = th },
                                        label = { Text(th, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Jenis MC Hybrid Chips + Custom Input
                        val currentMcType = viewModel.mcType.collectAsState().value
                        val presetMcTypes = listOf("Single", "Duet", "Group")
                        val isCustomMcType = currentMcType.isNotBlank() && currentMcType !in presetMcTypes

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Jenis MC / Peran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(presetMcTypes) { type ->
                                    val isSelected = currentMcType == type
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.mcType.value = type },
                                        label = { Text(type, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Primary.copy(alpha = 0.07f),
                                            labelColor = Primary.copy(alpha = 0.8f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            selectedBorderColor = Color.Transparent,
                                            borderColor = Primary.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                                if (isCustomMcType) {
                                    item {
                                        FilterChip(
                                            selected = true,
                                            onClick = { },
                                            label = { Text("★ $currentMcType", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Primary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            MCJobTextField(
                                value = currentMcType,
                                onValueChange = { viewModel.mcType.value = it },
                                label = "Penyesuaian Jenis MC",
                                placeholder = "Contoh: Single + Moderator / Duet Host"
                            )
                        }

                        // Bahasa Choice Chips + Custom Input
                        val currentLanguage = viewModel.language.collectAsState().value
                        val presetLanguages = listOf("Bahasa Indonesia", "Bilingual (Indo-English)", "English")
                        val isCustomLanguage = currentLanguage.isNotBlank() && currentLanguage !in presetLanguages

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Bahasa Pengantar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(presetLanguages) { lang ->
                                    val isSelected = currentLanguage == lang
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.language.value = lang },
                                        label = { Text(lang, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Primary.copy(alpha = 0.07f),
                                            labelColor = Primary.copy(alpha = 0.8f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            selectedBorderColor = Color.Transparent,
                                            borderColor = Primary.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                                if (isCustomLanguage) {
                                    item {
                                        FilterChip(
                                            selected = true,
                                            onClick = { },
                                            label = { Text("★ $currentLanguage", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Primary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            MCJobTextField(
                                value = currentLanguage,
                                onValueChange = { viewModel.language.value = it },
                                label = "Bahasa Kustom",
                                placeholder = "Contoh: Jawa Halus / Japanese"
                            )
                        }

                        MCJobTextField(
                            value = viewModel.audience.collectAsState().value,
                            onValueChange = { viewModel.audience.value = it },
                            label = "Audience",
                            placeholder = "500 Tamu Undangan"
                        )

                        MCJobTextField(
                            value = viewModel.specialRequest.collectAsState().value,
                            onValueChange = { viewModel.specialRequest.value = it },
                            label = "Request Khusus Klien / WO",
                            placeholder = "Ada sesi pelemparan buket & games interaktif",
                            singleLine = false
                        )
                    }
                }

                // Section 4: KEUANGAN & HONOR
                item {
                    FormSectionCard(
                        title = "KEUANGAN & HONOR MC",
                        icon = Icons.Default.Payments
                    ) {
                        val feeRaw = viewModel.fee.collectAsState().value
                        val feeVal = feeRaw.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MCJobTextField(
                                value = feeRaw,
                                onValueChange = {
                                    viewModel.fee.value = it
                                    viewModel.clearFieldError("fee")
                                },
                                label = "Total Honor MC (Rp)",
                                errorMessage = fieldErrors["fee"],
                                placeholder = "5000000",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            if (feeVal > 0) {
                                Text(
                                    text = Formatter.formatCurrency(feeVal),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        val dpRaw = viewModel.dp.collectAsState().value
                        val dpVal = dpRaw.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MCJobTextField(
                                value = dpRaw,
                                onValueChange = {
                                    viewModel.dp.value = it
                                    viewModel.clearFieldError("dp")
                                },
                                label = "DP / Terbayar Awal (Rp)",
                                errorMessage = fieldErrors["dp"],
                                placeholder = "2000000",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            if (dpVal > 0) {
                                Text(
                                    text = Formatter.formatCurrency(dpVal),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        val totalFee = feeVal
                        val paidDp = dpVal
                        val remaining = maxOf(0L, totalFee - paidDp)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Sisa Piutang Otomatis:", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = Formatter.formatCurrency(remaining),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (remaining > 0) Warning else Success
                                    )
                                }
                                if (paidDp >= totalFee && totalFee > 0L) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Success.copy(alpha = 0.15f)
                                    ) {
                                        Text("LUNAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Success, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: CATATAN BRIEF & RUNDOWN
                item {
                    FormSectionCard(
                        title = "CATATAN BRIEF & RUNDOWN",
                        icon = Icons.AutoMirrored.Filled.Notes
                    ) {
                        MCJobTextField(
                            value = viewModel.note.collectAsState().value,
                            onValueChange = { viewModel.note.value = it },
                            label = "Catatan Tambahan / Brief Acara",
                            placeholder = "Detail rundown, gladi bersih, kontak WO, atau catatan khusus lainnya.",
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }

                // Submit Button with Loading State
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    MCJobPrimaryButton(
                        text = if (isEditMode) "Perbarui Job" else "Simpan Job",
                        onClick = { viewModel.submitForm(forceSave = false) },
                        isLoading = isLoading,
                        icon = Icons.Default.Check,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }
            }
        }
    }

    // Material3 Date Picker Dialog
    if (showDatePicker) {
        val dateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val pickedDate = Instant.ofEpochMilli(ms)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.setDate(pickedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Pilih Tanggal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material3 Time Picker Dialog — Jam Mulai
    if (showStartTimePicker) {
        MCJobTimePickerDialog(
            title = "Pilih Jam Mulai",
            initialTime = viewModel.startTime.collectAsState().value.ifBlank { "19:00" },
            onTimeSelected = { newTime ->
                viewModel.setStartTime(newTime)
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    // Material3 Time Picker Dialog — Jam Selesai
    if (showEndTimePicker) {
        MCJobTimePickerDialog(
            title = "Pilih Jam Selesai",
            initialTime = viewModel.endTime.collectAsState().value.ifBlank { "22:00" },
            onTimeSelected = { newTime ->
                viewModel.setEndTime(newTime)
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    // Schedule Conflict Dialog
    if (showConflictDialog) {
        MCJobConflictDialog(
            conflictingBookings = conflictingBookings,
            onOverrideSave = { viewModel.submitForm(forceSave = true) },
            onDismiss = { viewModel.dismissConflictDialog() }
        )
    }

    // Save Confirmation & Google Calendar Integration Dialog
    val bookingId = createdBookingId
    if (showSaveConfirmation && bookingId != null) {
        val jobTitle = viewModel.name.value
        val eventDate = viewModel.date.value ?: LocalDate.now()
        val sTime = viewModel.startTime.value
        val eTime = viewModel.endTime.value
        val venue = viewModel.location.value.ifBlank { "Lokasi Acara" }

        AlertDialog(
            onDismissRequest = {
                showSaveConfirmation = false
                onJobSaved(bookingId)
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Jadwal Tersimpan! 🎉",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Tambah ke Google Calendar?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Jadwal acara kamu berhasil disimpan. Tambahkan langsung ke Google Calendar agar otomatis tersinkronisasi dan tidak terlewat!",
                        fontSize = 13.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = jobTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                            Text(text = "📅 ${Formatter.formatDate(eventDate)} • $sTime - $eTime", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(text = "📍 $venue", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val feeVal = viewModel.fee.value.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                        val dpVal = viewModel.dp.value.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                        val calendar = CalendarIntegration(context)
                        calendar.addFormDetailsToCalendar(
                            title = viewModel.name.value,
                            date = eventDate,
                            startTime = sTime,
                            endTime = eTime,
                            location = listOfNotNull(
                                viewModel.location.value.trim().ifBlank { null },
                                viewModel.address.value.trim().ifBlank { null }
                            ).joinToString(" - "),
                            client = viewModel.clientName.value,
                            pic = viewModel.pic.value,
                            dresscode = viewModel.dresscode.value,
                            theme = viewModel.theme.value,
                            mcType = viewModel.mcType.value,
                            language = viewModel.language.value,
                            fee = feeVal,
                            dp = dpVal,
                            note = viewModel.note.value
                        )
                        showSaveConfirmation = false
                        onJobSaved(bookingId)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan ke Google Calendar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveConfirmation = false
                        onJobSaved(bookingId)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Selesai & Ke Dashboard", fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        )
    }

    // Unsaved Changes Dialog
    if (showDraftDialog) {
        MCJobUnsavedChangesDialog(
            title = "Perubahan belum disimpan.",
            description = "Data job yang sudah kamu masukkan akan hilang jika keluar sekarang.",
            primaryCtaText = "Tetap Edit",
            secondaryCtaText = "Keluar",
            onStayEdit = { showDraftDialog = false },
            onExit = {
                showDraftDialog = false
                onBack()
            }
        )
    }
}

@Composable
fun FormSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary, fontSize = 13.sp)
            }
            HorizontalDivider(color = SurfaceVariant)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MCJobTimePickerDialog(
    title: String,
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (initialHour, initialMinute) = remember(initialTime) {
        val parts = initialTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 19
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        h to m
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val formatted = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelected(formatted)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simpan Jam", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF64748B))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF1F5F9),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color(0xFF1E293B),
                        selectorColor = Primary,
                        periodSelectorBorderColor = Primary,
                        periodSelectorSelectedContainerColor = Primary.copy(alpha = 0.15f),
                        periodSelectorUnselectedContainerColor = Color(0xFFF8FAFC),
                        periodSelectorSelectedContentColor = Primary,
                        periodSelectorUnselectedContentColor = Color(0xFF64748B),
                        timeSelectorSelectedContainerColor = Primary.copy(alpha = 0.15f),
                        timeSelectorUnselectedContainerColor = Color(0xFFF1F5F9),
                        timeSelectorSelectedContentColor = Primary,
                        timeSelectorUnselectedContentColor = Color(0xFF1E293B)
                    )
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
