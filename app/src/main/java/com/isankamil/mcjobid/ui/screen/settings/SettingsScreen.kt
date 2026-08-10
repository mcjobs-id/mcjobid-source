package com.isankamil.mcjobid.ui.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.screen.testimonial.TestimonialViewModel
import com.isankamil.mcjobid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToQuickActionSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isSynced by viewModel.isSynced.collectAsState()
    val lastSyncFormatted by viewModel.lastSyncFormatted.collectAsState()

    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val pinEnabled by viewModel.pinEnabled.collectAsState()
    val pinCode by viewModel.pinCode.collectAsState()
    val pinTimeoutMinutes by viewModel.pinTimeoutMinutes.collectAsState()
    val securityBackupKey by viewModel.securityBackupKey.collectAsState()
    val reminderDays by viewModel.reminderDays.collectAsState()
    val quickActionEnabled by viewModel.quickActionEnabled.collectAsState()
    var showCustomDayDialog by remember { mutableStateOf(false) }
    var customDayInput by remember { mutableStateOf("") }
    var customDayError by remember { mutableStateOf<String?>(null) }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    // Developer Mode state
    val testimonialViewModel: TestimonialViewModel = hiltViewModel()
    val testimonials by testimonialViewModel.testimonials.collectAsState()
    val deleteStatus by testimonialViewModel.deleteStatus.collectAsState()
    var showDevPinDialog by remember { mutableStateOf(false) }
    var showDevPanel by remember { mutableStateOf(false) }
    var devPinInput by remember { mutableStateOf("") }
    var devPinError by remember { mutableStateOf(false) }
    var devDeleteConfirmId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(deleteStatus) {
        deleteStatus?.let {
            snackbarHostState.showSnackbar(it)
            testimonialViewModel.clearDeleteStatus()
        }
    }

    // Navigate to Login screen after successful logout
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            onLogout()
        }
    }

    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updatePhotoUri(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pengaturan Aplikasi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    devPinInput = ""
                                    devPinError = false
                                    showDevPinDialog = true
                                }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card 1: Keamanan & Kunci Aplikasi (PIN Lock)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "KEAMANAN & KUNCI APLIKASI",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }

                            HorizontalDivider(color = SurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Kunci PIN Aplikasi (4-Digit)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                    Text("Minta PIN keamanan setiap kali aplikasi dibuka", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = pinEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled && pinCode.isBlank()) {
                                            showPinDialog = true
                                        } else {
                                            viewModel.setPinEnabled(enabled)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Primary,
                                        checkedBorderColor = Color.Transparent,
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFFE2E8F0),
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            if (pinEnabled || pinCode.isNotBlank()) {
                                val securityBackupKey by viewModel.securityBackupKey.collectAsState()
                                var showBackupKeyDialog by remember { mutableStateOf(false) }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showPinDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("Status Kunci PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                                    Text(if (pinCode.isNotBlank()) "PIN Terpasang (****)" else "PIN Belum Diatur", fontSize = 11.sp, color = OnSurfaceVariant)
                                                }
                                            }
                                            Text("Ubah PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                                        }
                                    }

                                    // PIN TIMEOUT SELECTOR
                                    if (pinEnabled && pinCode.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Timer, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("Toleransi Kunci Otomatis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                                        Text("Kunci PIN aktif setelah keluar selama:", fontSize = 11.sp, color = OnSurfaceVariant)
                                                    }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    listOf(5, 10) .forEach { minutes ->
                                                        val isSelected = pinTimeoutMinutes == minutes
                                                        Surface(
                                                            shape = RoundedCornerShape(10.dp),
                                                            color = if (isSelected) Primary else Color.White,
                                                            border = BorderStroke(1.dp, if (isSelected) Primary else Color(0xFFE2E8F0)),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clickable { viewModel.setPinTimeoutMinutes(minutes) }
                                                        ) {
                                                            Text(
                                                                text = "$minutes Menit",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) Color.White else Color(0xFF64748B),
                                                                modifier = Modifier.padding(vertical = 8.dp),
                                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showBackupKeyDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("Kunci Cadangan Keamanan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                                    Text("Kunci Cadangan: $securityBackupKey", fontSize = 11.sp, color = OnSurfaceVariant)
                                                }
                                            }
                                            Text("Ubah Kunci", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                                        }
                                    }
                                }

                                if (showBackupKeyDialog) {
                                    var backupInput by remember { mutableStateOf(securityBackupKey) }
                                    AlertDialog(
                                        onDismissRequest = { showBackupKeyDialog = false },
                                        title = { Text("Atur Kunci Cadangan Keamanan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                        text = {
                                            Column {
                                                Text("Kunci cadangan ini digunakan untuk mereset PIN jika kamu lupa PIN keamanan.", fontSize = 12.sp, color = OnSurfaceVariant)
                                                Spacer(modifier = Modifier.height(10.dp))
                                                OutlinedTextField(
                                                    value = backupInput,
                                                    onValueChange = { backupInput = it },
                                                    label = { Text("Kunci Cadangan / Password") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    if (backupInput.trim().isNotBlank()) {
                                                        viewModel.setSecurityBackupKey(backupInput.trim())
                                                        showBackupKeyDialog = false
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                            ) {
                                                Text("Simpan", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showBackupKeyDialog = false }) { Text("Batal") }
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        containerColor = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Card 3.5: Navigasi Kustomisasi Pintasan Cepat Dasbor
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.clickable { onNavigateToQuickActionSettings() }
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Pintasan Cepat Dasbor",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (quickActionEnabled) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Success.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "Aktif",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Success,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Atur tombol melayang & kustomisasi hak akses fitur cepat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                        }
                    }

                    // Card 4: Notifikasi & Pengingat Event

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NOTIFIKASI & PENGINGAT",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }

                            HorizontalDivider(color = SurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pengingat Otomatis Agenda", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                    Text("Notifikasi jadwal event & pengingat konfirmasi DP", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = reminderEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            showNotificationPermissionDialog = true
                                        }
                                        viewModel.setReminderEnabled(checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Primary,
                                        checkedBorderColor = Color.Transparent,
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFFE2E8F0),
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            if (reminderEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "Waktu Pengingat Acara",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF374151)
                                    )
                                    Text(
                                        "Pilih satu atau lebih hari pengingat sebelum acara:",
                                        fontSize = 11.sp,
                                        color = OnSurfaceVariant
                                    )

                                    // Chip preset: Hari-H, H-1, H-2, H-3, H-5, H-7
                                    val presetDays = listOf(
                                        0 to "Hari-H",
                                        1 to "H-1",
                                        2 to "H-2",
                                        3 to "H-3",
                                        5 to "H-5",
                                        7 to "H-7"
                                    )
                                    // Baris 1: 3 chip pertama
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        presetDays.take(3).forEach { (days, label) ->
                                            val selected = reminderDays.contains(days)
                                            FilterChip(
                                                selected = selected,
                                                onClick = { viewModel.toggleReminderDay(days) },
                                                label = {
                                                    Text(
                                                        label,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                leadingIcon = if (selected) {{
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }} else null,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Primary,
                                                    selectedLabelColor = Color.White,
                                                    selectedLeadingIconColor = Color.White,
                                                    containerColor = Color.White,
                                                    labelColor = Color(0xFF64748B)
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = selected,
                                                    selectedBorderColor = Primary,
                                                    borderColor = Color(0xFFE2E8F0)
                                                )
                                            )
                                        }
                                    }
                                    // Baris 2: 3 chip berikutnya
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        presetDays.drop(3).forEach { (days, label) ->
                                            val selected = reminderDays.contains(days)
                                            FilterChip(
                                                selected = selected,
                                                onClick = { viewModel.toggleReminderDay(days) },
                                                label = {
                                                    Text(
                                                        label,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                leadingIcon = if (selected) {{
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }} else null,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Primary,
                                                    selectedLabelColor = Color.White,
                                                    selectedLeadingIconColor = Color.White,
                                                    containerColor = Color.White,
                                                    labelColor = Color(0xFF64748B)
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = selected,
                                                    selectedBorderColor = Primary,
                                                    borderColor = Color(0xFFE2E8F0)
                                                )
                                            )
                                        }
                                        // Tombol tambah custom
                                        AssistChip(
                                            onClick = {
                                                customDayInput = ""
                                                customDayError = null
                                                showCustomDayDialog = true
                                            },
                                            label = {
                                                Text(
                                                    "+ Kustom",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = Secondary.copy(alpha = 0.08f),
                                                labelColor = Secondary,
                                                leadingIconContentColor = Secondary
                                            ),
                                            border = AssistChipDefaults.assistChipBorder(enabled = true,
                                                borderColor = Secondary.copy(alpha = 0.3f))
                                        )
                                    }

                                    // Chip hari kustom (di luar preset)
                                    val customDays = reminderDays.filter { day ->
                                        presetDays.none { it.first == day }
                                    }.sorted()
                                    if (customDays.isNotEmpty()) {
                                        Text(
                                            "Hari kustom aktif:",
                                            fontSize = 11.sp,
                                            color = OnSurfaceVariant
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            customDays.forEach { day ->
                                                InputChip(
                                                    selected = true,
                                                    onClick = {},
                                                    label = {
                                                        Text(
                                                            "H-$day",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    },
                                                    trailingIcon = {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Hapus H-$day",
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .clickable { viewModel.removeCustomReminderDay(day) },
                                                            tint = Color.White
                                                        )
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = InputChipDefaults.inputChipColors(
                                                        selectedContainerColor = Secondary,
                                                        selectedLabelColor = Color.White,
                                                        selectedTrailingIconColor = Color.White
                                                    ),
                                                    border = InputChipDefaults.inputChipBorder(
                                                        enabled = true,
                                                        selected = true,
                                                        selectedBorderColor = Secondary
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Info ringkasan aktif
                                    val activeLabels = reminderDays.sorted().map { day ->
                                        if (day == 0) "Hari-H" else "H-$day"
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Primary.copy(alpha = 0.06f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Pengingat aktif: ${activeLabels.joinToString(", ")}",
                                                fontSize = 11.sp,
                                                color = Primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 5: Sinkronisasi Cloud & Storage
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SINKRONISASI DATA CLOUD",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }

                            HorizontalDivider(color = SurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSynced) Success else Warning,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSynced) "Cloud Sync Aktif" else "Menunggu Koneksi Cloud",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurface
                                        )
                                    }
                                    Text(lastSyncFormatted, fontSize = 11.sp, color = OnSurfaceVariant)
                                }

                                Button(
                                    onClick = { viewModel.forceSyncNow() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f), contentColor = Primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync Ulang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Footer
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "mcjob.id • Professional MC Management System",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Error Dialog with Retry Callback
            errorMessage?.let { error ->
                MCJobErrorDialog(
                    title = "Terjadi Kesalahan",
                    description = error,
                    primaryCtaText = "OK",
                    onRetry = { viewModel.clearError() },
                    onDismiss = { viewModel.clearError() }
                )
            }

            // Success Feedback Dialog
            successMessage?.let { message ->
                MCJobSuccessDialog(
                    title = "Pengaturan Diperbarui",
                    description = message,
                    primaryCtaText = "Selesai",
                    secondaryCtaText = null,
                    onPrimary = { viewModel.clearSuccessMessage() },
                    onDismiss = { viewModel.clearSuccessMessage() }
                )
            }

            // Notification Permission Context Pre-Dialog
            if (showNotificationPermissionDialog) {
                MCJobNotificationPermissionDialog(
                    onEnable = {
                        viewModel.setReminderEnabled(true)
                        showNotificationPermissionDialog = false
                    },
                    onLater = {
                        showNotificationPermissionDialog = false
                    }
                )
            }

            // Logout Destructive Dialog
            if (showLogoutConfirmDialog) {
                MCJobDestructiveDialog(
                    title = "Konfirmasi Keluar Akun",
                    description = "Anda akan keluar dari sesi akun mcjob.id. Seluruh data tetap tersinkronisasi dengan aman di server cloud.",
                    primaryCtaText = "Ya, Keluar",
                    secondaryCtaText = "Batal",
                    onConfirm = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    onDismiss = { showLogoutConfirmDialog = false }
                )
            }

            // PIN Setup Dialog
            if (showPinDialog) {
                var pinInput by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showPinDialog = false },
                    title = { Text("Atur PIN Keamanan 4-Digit", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Masukkan 4 digit angka PIN untuk mengunci aplikasi MCJOB.id Anda demi keamanan data keuangan & agenda.", fontSize = 13.sp, color = Color(0xFF64748B))
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                                label = { Text("PIN 4-Digit") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (pinError != null) {
                                Text(pinError!!, fontSize = 12.sp, color = Error, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (pinInput.length == 4) {
                                    viewModel.setPinCode(pinInput)
                                    showPinDialog = false
                                } else {
                                    pinError = "PIN harus terdiri dari 4 angka!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simpan PIN", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinDialog = false }) {
                            Text("Batal", color = Color(0xFF64748B))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Dialog tambah hari kustom
            if (showCustomDayDialog) {
                AlertDialog(
                    onDismissRequest = { showCustomDayDialog = false },
                    title = {
                        Text(
                            "Tambah Hari Pengingat Kustom",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Masukkan jumlah hari sebelum acara (1–30).\nContoh: 10 = H-10, 14 = H-14.",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            OutlinedTextField(
                                value = customDayInput,
                                onValueChange = {
                                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                        customDayInput = it
                                        customDayError = null
                                    }
                                },
                                label = { Text("Jumlah hari (mis: 10)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = customDayError != null,
                                supportingText = customDayError?.let {
                                    { Text(it, color = Error, fontSize = 11.sp) }
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val day = customDayInput.toIntOrNull()
                                when {
                                    day == null || day < 1 || day > 30 ->
                                        customDayError = "Masukkan angka antara 1 sampai 30"
                                    reminderDays.contains(day) ->
                                        customDayError = "H-$day sudah aktif"
                                    else -> {
                                        viewModel.addCustomReminderDay(day)
                                        showCustomDayDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tambahkan", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomDayDialog = false }) {
                            Text("Batal", color = Color(0xFF64748B))
                        }
                    },
                    containerColor = Color.White,
                )
            }

            // Dialog PIN Rahasia Developer Mode (PIN: 2026)
            if (showDevPinDialog) {
                AlertDialog(
                    onDismissRequest = { showDevPinDialog = false },
                    title = {
                        Text(
                            "Verifikasi Keamanan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Masukkan 4-digit kode akses:",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            OutlinedTextField(
                                value = devPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                        devPinInput = it
                                        devPinError = false
                                    }
                                },
                                label = { Text("Kode Akses") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = devPinError,
                                supportingText = if (devPinError) {
                                    { Text("Kode akses tidak valid", color = Error, fontSize = 11.sp) }
                                } else null
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (devPinInput == "2026") {
                                    showDevPinDialog = false
                                    testimonialViewModel.enterDeveloperMode()
                                    showDevPanel = true
                                } else {
                                    devPinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Verifikasi", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDevPinDialog = false }) {
                            Text("Batal", color = Color(0xFF64748B))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Fullscreen Developer Moderation Panel
            if (showDevPanel) {
                Dialog(
                    onDismissRequest = {
                        showDevPanel = false
                        testimonialViewModel.exitDeveloperMode()
                    },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background),
                        color = Background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Developer Panel",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                    Text(
                                        "Moderasi & Hapus Testimoni Real-time",
                                        fontSize = 12.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showDevPanel = false
                                        testimonialViewModel.exitDeveloperMode()
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = OnSurface)
                                }
                            }

                            HorizontalDivider(color = SurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Daftar Testimoni (${testimonials.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (testimonials.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Belum ada testimoni.", color = OnSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(testimonials, key = { it.id }) { item ->
                                        val isSeed = item.id.startsWith("seed_")
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (!item.photoUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = item.photoUrl,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = item.userName.ifBlank { "Anonim" },
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = OnSurface
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isSeed) Color(0xFFE2E8F0) else Color(0xFFDCFCE7)
                                                        ) {
                                                            Text(
                                                                text = if (isSeed) "Seed" else "Firestore (Real)",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = if (isSeed) Color(0xFF64748B) else Color(0xFF15803D),
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        repeat(item.rating) {
                                                            Icon(
                                                                Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = Color(0xFFF59E0B),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        repeat(5 - item.rating) {
                                                            Icon(
                                                                Icons.Default.StarBorder,
                                                                contentDescription = null,
                                                                tint = Color(0xFFCBD5E1),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Rating: ${item.rating}/5 • ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", java.util.Locale.forLanguageTag("id-ID")).format(java.util.Date(item.createdAt))}",
                                                            fontSize = 11.sp,
                                                            color = OnSurfaceVariant
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = item.review,
                                                        fontSize = 12.sp,
                                                        color = OnSurface,
                                                        maxLines = 4,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    if (item.suggestion.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Saran: ${item.suggestion}",
                                                            fontSize = 11.sp,
                                                            color = Secondary,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                if (!isSeed) {
                                                    IconButton(
                                                        onClick = { devDeleteConfirmId = item.id }
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Hapus Testimoni",
                                                            tint = Error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dialog Konfirmasi Hapus Testimoni
            devDeleteConfirmId?.let { targetId ->
                AlertDialog(
                    onDismissRequest = { devDeleteConfirmId = null },
                    title = { Text("Hapus Testimoni?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Text(
                            "Testimoni ini akan dihapus secara permanen dari Firestore cloud dan hilang dari semua perangkat user secara real-time.",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                testimonialViewModel.deleteTestimonial(targetId)
                                devDeleteConfirmId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Hapus Permanen", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { devDeleteConfirmId = null }) {
                            Text("Batal", color = Color(0xFF64748B))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun UserInfoCard(
    email: String?,
    name: String?,
    photoUri: String?,
    onPhotoClick: (() -> Unit)? = null,
    onEditProfile: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Container with optional camera badge
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(64.dp)
                        .then(if (onPhotoClick != null) Modifier.clickable { onPhotoClick() } else Modifier)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(60.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary.copy(alpha = 0.3f))
                    ) {
                        com.isankamil.mcjobid.ui.components.MCJobAvatarImage(
                            photoUri = photoUri,
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (onPhotoClick != null) {
                        Surface(
                            shape = CircleShape,
                            color = Secondary,
                            modifier = Modifier.size(22.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Ubah Foto", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name ?: "MC Professional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = email ?: "Sesi Akun Aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Success.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Cloud Sync Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (onEditProfile != null) {
                HorizontalDivider(color = SurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditProfile() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Profil MC & Rate Card", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun UserInfoCard(
    email: String?,
    name: String?,
    photoUri: String?,
    onPhotoClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Container with optional camera badge
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(60.dp)
                    .then(if (onPhotoClick != null) Modifier.clickable { onPhotoClick() } else Modifier)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                ) {
                    com.isankamil.mcjobid.ui.components.MCJobAvatarImage(
                        photoUri = photoUri,
                        contentDescription = "Foto Profil",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (onPhotoClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = Secondary,
                        modifier = Modifier.size(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Ubah Foto", tint = Color.White, modifier = Modifier.size(11.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = name ?: "Pengguna MCJOBID",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email ?: "Cloud Account Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BankAccountForm(
    bankName: String,
    onBankNameChange: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChange: (String) -> Unit,
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Rekening & Kontak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            MCJobTextField(
                value = bankName,
                onValueChange = onBankNameChange,
                label = "Bank Utama",
                isRequired = true,
                placeholder = "Contoh: BCA, Mandiri"
            )
            
            MCJobTextField(
                value = accountNumber,
                onValueChange = onAccountNumberChange,
                label = "Nomor Rekening",
                isRequired = true,
                placeholder = "Masukkan angka"
            )
            
            MCJobTextField(
                value = accountName,
                onValueChange = onAccountNameChange,
                label = "Pemilik Rekening",
                isRequired = true,
                placeholder = "Nama sesuai buku tabungan"
            )
            
            MCJobTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = "Kontak WhatsApp",
                placeholder = "Contoh: 081234567890"
            )
            
            MCJobPrimaryButton(
                text = "Perbarui Data",
                onClick = onSave,
                icon = Icons.Default.Save,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
