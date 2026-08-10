package com.isankamil.mcjobid.ui.screen.client

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Client
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    viewModel: ClientViewModel,
    onBookingClick: (String) -> Unit,
    onAddJobForClientClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val clients by viewModel.filteredClients.collectAsState()
    val selectedClient by viewModel.selectedClient.collectAsState()
    val selectedClientBookings by viewModel.selectedClientBookings.collectAsState()

    var showAddClientModal by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<Client?>(null) }
    var clientToDelete by remember { mutableStateOf<Client?>(null) }
    var showDeleteClientDialog by remember { mutableStateOf(false) }

    var showPhoneConfirmDialog by remember { mutableStateOf<Client?>(null) }
    var showWhatsAppPreviewSheet by remember { mutableStateOf<Client?>(null) }

    Scaffold(
                        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manajemen Klien",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.selectClient(null)
                        showAddClientModal = true
                    }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Klien", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                windowInsets = TopAppBarDefaults.windowInsets
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
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { 
                            Text(
                                "Cari nama klien, perusahaan...", 
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

                    // Tab Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isSelected = selectedTab == ClientFilterTab.ALL
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilterTab(ClientFilterTab.ALL) },
                                label = { Text("Semua Klien", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                        item {
                            val isSelected = selectedTab == ClientFilterTab.FAVORITE
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilterTab(ClientFilterTab.FAVORITE) },
                                label = { Text("Favorit ★", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color.White else Color(0xFFF59E0B)
                                    )
                                },
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
                        item {
                            val isSelected = selectedTab == ClientFilterTab.ARCHIVED
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilterTab(ClientFilterTab.ARCHIVED) },
                                label = { Text("Diarsipkan", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

            if (clients.isEmpty()) {
                val emptyDesc = when (selectedTab) {
                    ClientFilterTab.ALL -> "Kelola database klien, WO, dan corporate penyelenggara acara kamu di sini."
                    ClientFilterTab.FAVORITE -> "Belum ada klien yang ditandai sebagai favorit."
                    ClientFilterTab.ARCHIVED -> "Tidak ada klien yang sedang diarsipkan."
                }
                MCJobEmptyState(
                    icon = Icons.Default.People,
                    title = if (selectedTab == ClientFilterTab.ARCHIVED) "Arsip Kosong" else "Belum Ada Klien",
                    description = emptyDesc,
                    actionText = if (selectedTab == ClientFilterTab.ALL) "Tambah Klien" else null,
                    onActionClick = if (selectedTab == ClientFilterTab.ALL) { { showAddClientModal = true } } else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clients, key = { it.id }) { client ->
                        val metrics = viewModel.getMetricsForClient(client)
                        ClientCardItem(
                            client = client,
                            metrics = metrics,
                            onToggleFavorite = { viewModel.toggleFavorite(client.id) },
                            onClick = { viewModel.selectClient(client) }
                        )
                    }
                }
            }
        }
    }

    // Client Detail Modal
    selectedClient?.let { client ->
        val metrics = viewModel.getMetricsForClient(client)
        AlertDialog(
            onDismissRequest = { viewModel.selectClient(null) },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = client.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (metrics.totalJobs > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        "REPEAT CLIENT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = client.company ?: "Personal Client",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(client.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (client.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (client.isFavorite) Color(0xFFF59E0B) else OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Surface(
                            onClick = { clientToEdit = client },
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profil", tint = Primary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Surface(
                            onClick = {
                                clientToDelete = client
                                showDeleteClientDialog = true
                            },
                            shape = CircleShape,
                            color = Error.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus/Arsipkan", tint = Error, modifier = Modifier.size(16.dp))
                            }
                        }
                        IconButton(
                            onClick = { viewModel.selectClient(null) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Contact Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MCJobPrimaryButton(
                            text = "WhatsApp",
                            onClick = {
                                val normalized = Formatter.formatWhatsAppNumber(client.phone)
                                if (normalized.isNotBlank()) {
                                    showWhatsAppPreviewSheet = client
                                }
                            },
                            containerColor = Primary,
                            icon = Icons.AutoMirrored.Filled.Chat,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.weight(1f)
                        )

                        MCJobSecondaryButton(
                            text = "Telepon",
                            onClick = {
                                val phone = client.phone ?: ""
                                if (phone.isNotBlank()) {
                                    showPhoneConfirmDialog = client
                                }
                            },
                            icon = Icons.Default.Call,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Metrics Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricSmallBox("TOTAL JOB", "${metrics.totalJobs}", Modifier.weight(1f).fillMaxHeight())
                            MetricSmallBox("TOTAL OMSET", Formatter.formatCurrency(metrics.totalRevenue), Modifier.weight(1f).fillMaxHeight())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricSmallBox("TERBAYAR", Formatter.formatCurrency(metrics.totalPaid), Modifier.weight(1f).fillMaxHeight(), color = Success)
                            MetricSmallBox("PIUTANG", Formatter.formatCurrency(metrics.totalOutstanding), Modifier.weight(1f).fillMaxHeight(), color = if (metrics.totalOutstanding > 0) Warning else Success)
                        }
                    }

                    // Unarchive Banner (if archived)
                    if (client.isArchived) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Warning.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status: Diarsipkan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Warning)
                                TextButton(onClick = {
                                    viewModel.unarchiveClient(client.id)
                                }) {
                                    Text("Pulihkan", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Riwayat Event Klien", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    if (selectedClientBookings.isEmpty()) {
                        Text("Belum ada riwayat event terdaftar.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedClientBookings.forEach { booking ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectClient(null)
                                            onBookingClick(booking.id)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(booking.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${Formatter.formatDate(booking.date)} • ${Formatter.formatCurrency(booking.fee)}", fontSize = 11.sp, color = OnSurfaceVariant)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                MCJobPrimaryButton(
                    text = "Tambah Job Klien Ini",
                    onClick = {
                        val clientName = client.name
                        viewModel.selectClient(null)
                        onAddJobForClientClick(clientName)
                    },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Add Client Dialog
    if (showAddClientModal) {
        var nameText by remember { mutableStateOf("") }
        var phoneText by remember { mutableStateOf("") }
        var emailText by remember { mutableStateOf("") }
        var companyText by remember { mutableStateOf("") }
        var picText by remember { mutableStateOf("") }
        var notesText by remember { mutableStateOf("") }
        var nameError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddClientModal = false },
            title = { Text("Tambah Klien Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MCJobTextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                            nameError = null
                        },
                        label = "Nama Klien / Instansi",
                        isRequired = true,
                        errorMessage = nameError,
                        placeholder = "Contoh: PT Aksara Mandiri / Sarah"
                    )
                    MCJobTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = "Nomor WhatsApp / HP",
                        placeholder = "081234567890",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    MCJobTextField(
                        value = companyText,
                        onValueChange = { companyText = it },
                        label = "Nama Perusahaan / WO",
                        placeholder = "Happy Wedding Planner / Corporate"
                    )
                    MCJobTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = "Email",
                        placeholder = "klien@domain.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    MCJobTextField(
                        value = picText,
                        onValueChange = { picText = it },
                        label = "Kontak PIC Lapangan",
                        placeholder = "Siska (081987654321)"
                    )
                }
            },
            confirmButton = {
                MCJobPrimaryButton(
                    text = "Simpan Klien",
                    onClick = {
                        if (nameText.trim().isBlank()) {
                            nameError = "Nama Klien / Instansi wajib diisi."
                        } else {
                            viewModel.saveClient(nameText, phoneText, emailText, companyText, picText, notesText)
                            showAddClientModal = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                MCJobSecondaryButton(
                    text = "Batal",
                    onClick = { showAddClientModal = false },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Edit Client Dialog
    clientToEdit?.let { client ->
        var editName by remember(client) { mutableStateOf(client.name) }
        var editPhone by remember(client) { mutableStateOf(client.phone ?: "") }
        var editEmail by remember(client) { mutableStateOf(client.email ?: "") }
        var editCompany by remember(client) { mutableStateOf(client.company ?: "") }
        var editPic by remember(client) { mutableStateOf(client.pic ?: "") }
        var editNotes by remember(client) { mutableStateOf(client.notes ?: "") }
        var editNameError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { clientToEdit = null },
            title = { Text("Edit Profil Klien", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MCJobTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                            editNameError = null
                        },
                        label = "Nama Klien / Instansi",
                        isRequired = true,
                        errorMessage = editNameError,
                        placeholder = "Nama Klien"
                    )
                    MCJobTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = "Nomor WhatsApp / HP",
                        placeholder = "081234567890",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    MCJobTextField(
                        value = editCompany,
                        onValueChange = { editCompany = it },
                        label = "Nama Perusahaan / WO",
                        placeholder = "Perusahaan / WO"
                    )
                    MCJobTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = "Email",
                        placeholder = "klien@domain.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    MCJobTextField(
                        value = editPic,
                        onValueChange = { editPic = it },
                        label = "Kontak PIC Lapangan",
                        placeholder = "Nama PIC & HP"
                    )
                }
            },
            confirmButton = {
                MCJobPrimaryButton(
                    text = "Perbarui Klien",
                    onClick = {
                        if (editName.trim().isBlank()) {
                            editNameError = "Nama Klien / Instansi wajib diisi."
                        } else {
                            viewModel.updateClient(client.id, editName, editPhone, editEmail, editCompany, editPic, editNotes)
                            clientToEdit = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                MCJobSecondaryButton(
                    text = "Batal",
                    onClick = { clientToEdit = null },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Delete / Archive Client Dialog
    val client = clientToDelete
    if (showDeleteClientDialog && client != null) {
        val metrics = viewModel.getMetricsForClient(client)

        if (metrics.totalJobs > 0) {
            MCJobDeleteClientDialog(
                clientName = client.name,
                jobCount = metrics.totalJobs,
                onArchive = {
                    viewModel.archiveClient(client.id)
                    showDeleteClientDialog = false
                    clientToDelete = null
                },
                onViewHistory = {
                    showDeleteClientDialog = false
                    viewModel.selectClient(client)
                },
                onDismiss = {
                    showDeleteClientDialog = false
                    clientToDelete = null
                }
            )
        } else {
            MCJobDestructiveDialog(
                title = "Hapus Klien?",
                description = "Klien '${client.name}' belum memiliki riwayat job dan akan dihapus secara permanen.",
                primaryCtaText = "Hapus Klien",
                secondaryCtaText = "Batal",
                onConfirm = {
                    viewModel.deleteClient(client)
                    showDeleteClientDialog = false
                    clientToDelete = null
                },
                onDismiss = {
                    showDeleteClientDialog = false
                    clientToDelete = null
                }
            )
        }
    }

    // WhatsApp Message Preview Sheet (Fixed P0 Number Normalization)
    showWhatsAppPreviewSheet?.let { client ->
        val normalizedPhone = Formatter.formatWhatsAppNumber(client.phone)
        MCJobWhatsAppPreviewSheet(
            clientName = client.name,
            phone = if (normalizedPhone.isNotBlank()) "+$normalizedPhone" else "Belum ada no. HP",
            initialMessage = "Halo Kak ${client.name}, salam dari MC ${client.company?.let { "($it)" } ?: ""}. Ada yang bisa kami bantu mengenai persiapan agenda event mendatang?",
            onSend = { msg ->
                if (normalizedPhone.isNotBlank()) {
                    val encodedMsg = Uri.encode(msg)
                    val url = "https://wa.me/$normalizedPhone?text=$encodedMsg"
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {}
                }
            },
            onDismiss = { showWhatsAppPreviewSheet = null }
        )
    }

    // Phone Call Confirmation Dialog
    showPhoneConfirmDialog?.let { client ->
        val phone = client.phone ?: ""
        MCJobConfirmDialog(
            title = "Hubungi ${client.name}?",
            description = "Panggilan ke nomor $phone akan dialihkan ke aplikasi Telepon ponsel kamu.",
            primaryCtaText = "Telepon",
            secondaryCtaText = "Batal",
            onConfirm = {
                if (phone.isNotBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            },
            onDismiss = { showPhoneConfirmDialog = null }
        )
    }
}

@Composable
fun MetricSmallBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Primary
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ClientCardItem(
    client: Client,
    metrics: ClientMetrics,
    onToggleFavorite: () -> Unit,
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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (client.isArchived) Color(0xFF94A3B8).copy(alpha = 0.15f) else Primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = client.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (client.isArchived) Color(0xFF64748B) else Primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = client.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (metrics.totalJobs > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Text("REPEAT", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Primary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    if (client.isArchived) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Warning.copy(alpha = 0.15f)
                        ) {
                            Text("ARSIP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Warning, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(
                    text = "${client.company ?: "Personal Client"} • WA: ${client.phone ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${metrics.totalJobs} Job • Omset: ${Formatter.formatCurrency(metrics.totalRevenue)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (client.isArchived) Color(0xFF64748B) else Primary
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.minimumInteractiveComponentSize().size(36.dp)
            ) {
                Icon(
                    imageVector = if (client.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorit",
                    tint = if (client.isFavorite) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
        }
    }
}
