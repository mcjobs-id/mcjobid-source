package com.isankamil.mcjobid.ui.screen.job

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.components.EventBriefDialog
import com.isankamil.mcjobid.ui.components.ExpenseDialog
import com.isankamil.mcjobid.ui.components.PaymentDialog
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    viewModel: JobDetailViewModel,
    onBack: () -> Unit,
    onEditJobClick: (String) -> Unit,
    onCreateInvoiceClick: (String) -> Unit,
    onMcDayModeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val booking by viewModel.booking.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val checklist by viewModel.checklist.collectAsState()
    val invoice by viewModel.invoice.collectAsState()
    val duplicatedBookingId by viewModel.duplicatedBookingId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currentBooking = booking
    val currentInvoice = invoice

    var showActionSheet by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showBriefDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var showCompleteConfirm by remember { mutableStateOf(false) }
    var showMcDayModeConfirm by remember { mutableStateOf(false) }

    var showWhatsAppPreviewSheet by remember { mutableStateOf(false) }
    var showPhoneConfirmDialog by remember { mutableStateOf(false) }
    var showMapsConfirmDialog by remember { mutableStateOf(false) }

    var pendingPaymentData by remember { mutableStateOf<PaymentDraft?>(null) }
    var showNewChecklistInput by remember { mutableStateOf(false) }
    var newChecklistTitle by remember { mutableStateOf("") }

    LaunchedEffect(duplicatedBookingId) {
        duplicatedBookingId?.let { newId ->
            viewModel.clearDuplicatedId()
            onEditJobClick(newId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Job", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    booking?.let { b ->
                        IconButton(onClick = { showBriefDialog = true }) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Quick Brief", tint = Warning)
                        }
                        IconButton(onClick = { showActionSheet = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Kelola Job", tint = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (isLoading || currentBooking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val item = currentBooking
            val totalExpense = remember(expenses) { expenses.sumOf { it.amount } }
            val netIncome = remember(item.fee, totalExpense) { item.fee - totalExpense }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // MC DAY MODE BUTTON (If event is TODAY)
                if (item.date == LocalDate.now()) {
                    item {
                        Button(
                            onClick = { showMcDayModeConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Warning)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ BUKA MC DAY MODE (HARI H)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }

                // Header Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "🎤 ${item.category}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                val isCompleted = item.status == Booking.BookingStatus.COMPLETED
                                val isCancelled = item.status == Booking.BookingStatus.CANCELLED
                                val statusBg = when {
                                    isCompleted -> Success.copy(alpha = 0.15f)
                                    isCancelled -> Error.copy(alpha = 0.15f)
                                    else -> Warning.copy(alpha = 0.15f)
                                }
                                val statusText = when {
                                    isCompleted -> "✓ SELESAI"
                                    isCancelled -> "✕ DIBATALKAN"
                                    else -> "CONFIRMED / ACTIVE"
                                }
                                val statusColor = when {
                                    isCompleted -> Success
                                    isCancelled -> Error
                                    else -> Warning
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusBg
                                ) {
                                    Text(
                                        text = statusText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Klien: ${item.client ?: "Personal"}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                    }
                }

                // Section 1: Event Preparation Checklist
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val completedCount = checklist.count { it.isCompleted }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Persiapan Acara (Checklist)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (checklist.isNotEmpty()) Primary.copy(alpha = 0.1f) else SurfaceVariant
                                ) {
                                    Text(
                                        text = if (checklist.isNotEmpty()) "$completedCount / ${checklist.size} Selesai" else "Belum Ada Item",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (checklist.isNotEmpty()) Primary else OnSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (checklist.isNotEmpty()) {
                                LinearProgressIndicator(
                                    progress = { completedCount.toFloat() / checklist.size.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Primary,
                                    trackColor = SurfaceVariant
                                )

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                checklist.forEach { chk ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = chk.isCompleted,
                                            onCheckedChange = { viewModel.toggleChecklist(chk) },
                                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = chk.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (chk.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                            color = if (chk.isCompleted) OnSurfaceVariant else OnBackground,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.toggleChecklist(chk) }
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteChecklistItem(chk) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Hapus Item",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Primary.copy(alpha = 0.08f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.FactCheck,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Belum Ada Item Persiapan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Catat kebutuhan cue card, dresscode, sound system, atau rundown acara.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            if (showNewChecklistInput) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newChecklistTitle,
                                        onValueChange = { newChecklistTitle = it },
                                        placeholder = { Text("Contoh: Cek Cue Card...", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 13.sp)
                                    )
                                    Button(
                                        onClick = {
                                            if (newChecklistTitle.isNotBlank()) {
                                                viewModel.addChecklistItem(newChecklistTitle)
                                                newChecklistTitle = ""
                                                showNewChecklistInput = false
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("Simpan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showNewChecklistInput = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Item Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                                }
                            }
                        }
                    }
                }

                // Section 2: Financials & Net Income
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💰 Keuangan & Net Income", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
                            HorizontalDivider(color = SurfaceVariant)

                            DetailRow("Total Honor (Revenue)", Formatter.formatCurrency(item.fee))
                            DetailRow("Total DP / Terbayar", Formatter.formatCurrency(item.dp))
                            
                            val isLunas = item.outstanding <= 0L
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sisa Piutang", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                if (isLunas) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Success.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Rp0 • LUNAS ✓",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Success,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = Formatter.formatCurrency(item.outstanding),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }

                            DetailRow("Total Pengeluaran (Expense)", "- ${Formatter.formatCurrency(totalExpense)}")

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Primary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("NET INCOME (BERSIH)", fontWeight = FontWeight.Bold)
                                    Text(Formatter.formatCurrency(netIncome), fontWeight = FontWeight.ExtraBold, color = Primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isLunas) {
                                    MCJobPrimaryButton(
                                        text = "Pelunasan",
                                        icon = Icons.Default.Payments,
                                        onClick = { showPaymentDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                MCJobSecondaryButton(
                                    text = "Catat Pengeluaran",
                                    icon = Icons.Default.Receipt,
                                    onClick = { showExpenseDialog = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 3: Jadwal & Lokasi
                item {
                    DetailSectionCard(title = "📅 Jadwal & Lokasi", icon = Icons.Default.Event) {
                        DetailRow("Tanggal", Formatter.formatDate(item.date))
                        DetailRow("Waktu Acara", "${item.start ?: "19:00"} - ${item.end ?: "22:00"}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lokasi Venue", fontSize = 12.sp, color = OnSurfaceVariant)
                            if (!item.location.isNullOrBlank()) {
                                Surface(
                                    onClick = { showMapsConfirmDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.location,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Map,
                                            contentDescription = "Buka Maps",
                                            tint = Primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else {
                                Text("Belum ditentukan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                            }
                        }
                        if (!item.address.isNullOrBlank()) {
                            DetailRow("Alamat Lengkap", item.address)
                        }
                    }
                }

                // Section 4: Detail MC & Dresscode
                item {
                    DetailSectionCard(title = "👔 Detail Spesifikasi MC & Konsep", icon = Icons.Default.Mic) {
                        DetailRow("Dresscode / Wardrobe", item.dresscode ?: "Sesuaikan")
                        if (!item.theme.isNullOrBlank()) {
                            DetailRow("Tema Acara", item.theme)
                        }
                        DetailRow("Format MC", item.mcType ?: "Single")
                        DetailRow("Bahasa Pengantar", item.language ?: "Bahasa Indonesia")
                        if (!item.audience.isNullOrBlank()) {
                            DetailRow("Estimasi Audience / Tamu", item.audience)
                        }
                        if (!item.pic.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kontak PIC / WO", fontSize = 12.sp, color = OnSurfaceVariant)
                                Surface(
                                    onClick = { showPhoneConfirmDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.pic,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Call,
                                            contentDescription = "Telepon PIC",
                                            tint = Primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: Catatan Khusus Rundown & Protokol VIP
                if (!item.specialRequest.isNullOrBlank()) {
                    item {
                        DetailSectionCard(title = "⚡ Request Khusus & Protokol VIP", icon = Icons.Default.Stars) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Warning.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Warning.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.specialRequest,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // Section 6: Catatan Internal MC
                if (!item.note.isNullOrBlank()) {
                    item {
                        DetailSectionCard(title = "📝 Catatan Internal MC", icon = Icons.Default.NoteAlt) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceVariant,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.note,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // Actions Bar
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (item.outstanding > 0) {
                            MCJobPrimaryButton(
                                text = "Follow Up Piutang via WA",
                                onClick = { showWhatsAppPreviewSheet = true },
                                containerColor = Primary,
                                icon = Icons.AutoMirrored.Filled.Chat,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (currentInvoice != null) {
                            MCJobPrimaryButton(
                                text = "Lihat / Bagikan Invoice (${currentInvoice.invoiceNumber})",
                                onClick = { onCreateInvoiceClick(item.id) },
                                containerColor = Primary,
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            MCJobPrimaryButton(
                                text = "Buat Invoice PDF",
                                onClick = { onCreateInvoiceClick(item.id) },
                                containerColor = Warning,
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        MCJobSecondaryButton(
                            text = "Duplikasi Job Ini (Repeat Client)",
                            onClick = { viewModel.duplicateJob(LocalDate.now().plusDays(30)) },
                            icon = Icons.Default.ContentCopy,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Action Sheet ("Kelola Job")
    if (showActionSheet && currentBooking != null) {
        val item = currentBooking
        MCJobActionSheet(
            title = "Kelola Job",
            options = listOf(
                ActionSheetOption("Edit Job", Icons.Default.Edit) { onEditJobClick(item.id) },
                ActionSheetOption("Catat Pembayaran", Icons.Default.Payments) { showPaymentDialog = true },
                ActionSheetOption(
                    if (currentInvoice != null) "Lihat / Edit Invoice (${currentInvoice.invoiceNumber})" else "Buat Invoice",
                    Icons.AutoMirrored.Filled.ReceiptLong
                ) { onCreateInvoiceClick(item.id) },
                ActionSheetOption(
                    if (item.status == Booking.BookingStatus.COMPLETED) "Tandai Belum Selesai" else "Tandai Job Selesai",
                    Icons.Default.CheckCircle
                ) { showCompleteConfirm = true },
                ActionSheetOption("Batalkan Job", Icons.Default.Cancel, isDestructive = false) { showCancelConfirm = true },
                ActionSheetOption("Hapus Job", Icons.Default.Delete, isDestructive = true) { showDeleteConfirm = true }
            ),
            onDismiss = { showActionSheet = false }
        )
    }

    // Payment Dialog Input
    if (showPaymentDialog && currentBooking != null) {
        val item = currentBooking
        PaymentDialog(
            bookingName = item.name,
            totalFee = item.fee,
            currentPaid = item.dp,
            onSavePayment = { amount, date, method, notes ->
                showPaymentDialog = false
                val remaining = maxOf(0L, item.fee - item.dp - amount)
                val isOver = amount > (item.fee - item.dp)
                pendingPaymentData = PaymentDraft(amount, date, method, notes, remaining, isOver)
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    // Payment Confirmation Modal (Requirement #20, #21)
    pendingPaymentData?.let { draft ->
        if (currentBooking != null) {
            val item = currentBooking
            MCJobPaymentConfirmationDialog(
                bookingTitle = item.name,
                amount = draft.amount,
                paymentMethod = draft.method,
                paymentDate = draft.date,
                totalFee = item.fee,
                currentPaid = item.dp,
                remainingAfterPayment = draft.remaining,
                isOverpayment = draft.isOverpayment,
                onConfirm = {
                    viewModel.addPayment(draft.amount, draft.date, draft.method, draft.notes)
                    pendingPaymentData = null
                },
                onDismiss = { pendingPaymentData = null }
            )
        }
    }

    // Expense Dialog
    if (showExpenseDialog && currentBooking != null) {
        val item = currentBooking
        ExpenseDialog(
            bookingName = item.name,
            bookingId = item.id,
            onSaveExpense = { _, category, amount, date, note ->
                viewModel.addExpense(category, amount, date, note)
                showExpenseDialog = false
            },
            onDismiss = { showExpenseDialog = false }
        )
    }

    // Event Brief Dialog
    if (showBriefDialog && currentBooking != null) {
        EventBriefDialog(
            booking = currentBooking,
            onDismiss = { showBriefDialog = false }
        )
    }

    // WhatsApp Message Preview Sheet (Requirement #28)
    if (showWhatsAppPreviewSheet && currentBooking != null) {
        val item = currentBooking
        val picPhone = Formatter.formatWhatsAppNumber(item.pic)
        val defaultMsg = "Halo Kak, izin follow up terkait pembayaran untuk acara ${item.name}. Saat ini masih terdapat sisa pembayaran sebesar ${Formatter.formatCurrency(item.outstanding)}. Terima kasih."

        MCJobWhatsAppPreviewSheet(
            clientName = item.client ?: "Klien",
            phone = if (picPhone.isNotBlank()) picPhone else "Belum ada no. HP",
            initialMessage = defaultMsg,
            onSend = { msg ->
                val encodedMsg = Uri.encode(msg)
                val url = if (picPhone.isNotBlank()) "https://wa.me/$picPhone?text=$encodedMsg" else "https://wa.me/?text=$encodedMsg"
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {}
            },
            onDismiss = { showWhatsAppPreviewSheet = false }
        )
    }

    // Delete Confirmation Dialog (Requirement #9)
    if (showDeleteConfirm && currentBooking != null) {
        val item = currentBooking
        MCJobDestructiveDialog(
            title = "Hapus Job?",
            description = "Data job '${item.name}' akan dihapus secara permanen. Tindakan ini tidak dapat dibatalkan.",
            primaryCtaText = "Hapus Job",
            secondaryCtaText = "Batal",
            onConfirm = {
                viewModel.deleteBooking(onSuccess = onBack)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // Cancel Job Confirmation Dialog (Requirement #24)
    if (showCancelConfirm && currentBooking != null) {
        val item = currentBooking
        MCJobConfirmDialog(
            title = "Batalkan Job?",
            description = "Job '${item.name}' akan tetap tersimpan dalam riwayat tetapi tidak lagi dianggap sebagai agenda aktif.",
            primaryCtaText = "Batalkan Job",
            secondaryCtaText = "Kembali",
            onConfirm = {
                viewModel.cancelJob()
            },
            onDismiss = { showCancelConfirm = false }
        )
    }

    // Mark Job Completed Dialog (Requirement #25)
    if (showCompleteConfirm && currentBooking != null) {
        val item = currentBooking
        val isCompleted = item.status == Booking.BookingStatus.COMPLETED
        MCJobConfirmDialog(
            title = if (isCompleted) "Tandai Belum Selesai?" else "Job Selesai?",
            description = if (isCompleted)
                "Status job akan dikembalikan menjadi aktif."
            else
                "Pastikan acara '${item.name}' sudah selesai sebelum menandai job sebagai selesai.",
            primaryCtaText = if (isCompleted) "Tandai Aktif" else "Tandai Selesai",
            secondaryCtaText = "Batal",
            onConfirm = {
                viewModel.toggleCompleted()
            },
            onDismiss = { showCompleteConfirm = false }
        )
    }

    // MC Day Mode Confirmation Dialog (Requirement #30)
    if (showMcDayModeConfirm && currentBooking != null) {
        val item = currentBooking
        MCJobMCDayModeDialog(
            eventName = item.name,
            onStart = {
                onMcDayModeClick(item.id)
            },
            onCancel = { showMcDayModeConfirm = false }
        )
    }

    // Phone Call Confirmation Dialog (Requirement #29)
    if (showPhoneConfirmDialog && currentBooking != null) {
        val pic = currentBooking.pic ?: "PIC"
        val phone = pic.filter { it.isDigit() }
        MCJobConfirmDialog(
            title = "Hubungi $pic?",
            description = "Panggilan akan dialihkan ke aplikasi Telepon ponsel kamu.",
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
            onDismiss = { showPhoneConfirmDialog = false }
        )
    }

    // Maps Confirmation Dialog (Requirement #29)
    if (showMapsConfirmDialog && currentBooking != null) {
        val loc = currentBooking.location ?: currentBooking.address ?: "Lokasi"
        MCJobConfirmDialog(
            title = "Buka Lokasi Acara?",
            description = "Lokasi '$loc' akan dibuka menggunakan aplikasi Google Maps.",
            primaryCtaText = "Buka Maps",
            secondaryCtaText = "Batal",
            onConfirm = {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(loc)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    context.startActivity(mapIntent)
                } catch (e: ActivityNotFoundException) {
                    val genericIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    try {
                        context.startActivity(genericIntent)
                    } catch (_: Exception) {}
                }
            },
            onDismiss = { showMapsConfirmDialog = false }
        )
    }
}

private data class PaymentDraft(
    val amount: Long,
    val date: String,
    val method: String,
    val notes: String,
    val remaining: Long,
    val isOverpayment: Boolean
)

@Composable
fun DetailSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
            }
            HorizontalDivider(color = SurfaceVariant)
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnBackground)
    }
}
