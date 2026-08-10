package com.isankamil.mcjobid.ui.screen.invoice

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Invoice
import com.isankamil.mcjobid.domain.model.InvoiceTemplate
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

private fun showNativeDatePicker(context: Context, initialDateStr: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    try {
        val parts = initialDateStr.split("-")
        if (parts.size == 3) {
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
    } catch (_: Exception) {}

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookings by viewModel.bookings.collectAsState()
    val selectedBooking by viewModel.selectedBooking.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val invoiceNumber by viewModel.invoiceNumber.collectAsState()
    val issueDate by viewModel.issueDate.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val invoiceHistory by viewModel.invoiceHistory.collectAsState()
    val pdfFile by viewModel.generatedPdfFile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showBookingDropdown by remember { mutableStateOf(false) }
    var showGenerateConfirm by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pdfFile) {
        if (pdfFile != null) {
            showSuccessDialog = true
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showErrorDialog = true
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Buat Invoice, 1 = Template & Preview

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Invoice Generator", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            MCJobInfoTooltip(tooltipText = "Buat invoice digital resmi untuk klien MC dengan rincian honor, DP, sisa piutang, dan rekening bank.")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        if (selectedBooking != null) {
                            IconButton(onClick = { viewModel.copySummaryToClipboard(context) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Salin Teks Invoice", tint = Primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                // Sub-Navigation Tabs: Invoice Generate vs Template
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buat Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Template & Preview", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            if (selectedTab == 1) {
                // ========================================================
                // TAB 1: TEMPLATE DESIGN MANAGER & LIVE GRAPHIC PREVIEWS
                // ========================================================
                val selectedTpl by viewModel.selectedTemplate.collectAsState()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Pilih & Pratinjau Template Invoice", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Primary)
                                    Text("Pilih tata letak PDF resmi yang sesuai dengan jenis dan gaya acara MC kamu.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }

                    InvoiceTemplate.values().forEach { tpl ->
                        item {
                            val isSelected = selectedTpl == tpl
                            val badgeColor = try { Color(android.graphics.Color.parseColor(tpl.accentColorHex)) } catch (e: Exception) { Primary }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Primary else Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = badgeColor
                                            ) {
                                                Text(
                                                    text = tpl.tag,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(tpl.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                        }

                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Success.copy(alpha = 0.12f)
                                            ) {
                                                Text("✓ AKTIF", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Success, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(tpl.description, fontSize = 12.sp, color = Color(0xFF64748B))

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Graphic Wireframe Mockup Preview
                                    TemplateGraphicMockup(tpl)

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.previewSamplePdf(tpl, context)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Primary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Live PDF Preview", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.selectTemplate(tpl)
                                                selectedTab = 0
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Success else Primary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isSelected) "Sedang Dipakai" else "Pilih Template", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (bookings.isEmpty() && !isLoading) {
                // ========================================================
                // EMPTY STATE
                // ========================================================
                MCJobEmptyState(
                    title = "Belum Ada Job Terdaftar",
                    description = "Kamu belum memiliki data job acara. Buat job pertama kamu untuk dapat menerbitkan invoice profesional.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    actionText = "Kembali",
                    onActionClick = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // ========================================================
                // TAB 0: FORM BUAT INVOICE (INVOICE GENERATE)
                // ========================================================
                val selectedTpl by viewModel.selectedTemplate.collectAsState()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Active Template Banner Indicator
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Primary.copy(alpha = 0.12f)
                                    ) {
                                        Box(modifier = Modifier.padding(8.dp)) {
                                            Icon(Icons.Default.Palette, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("TEMPLATE AKTIF:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                                        Text(selectedTpl.title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Primary)
                                    }
                                }

                                TextButton(onClick = { selectedTab = 1 }) {
                                    Text("Ubah Template", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                                }
                            }
                        }
                    }

                    // Alert Banner: Missing Bank Account
                    if (userProfile != null && userProfile?.bankName.isNullOrBlank()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Warning.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Warning)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Rekening Bank Belum Diatur",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = OnBackground
                                        )
                                        Text(
                                            "Atur nomor rekening di menu Profil Saya agar nomor rekening pembayaran otomatis tertera di PDF invoice.",
                                            fontSize = 11.sp,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Card 1: Job Selector
                    item {
                        FormSectionCard(
                            title = "PILIH ACARA / JOB",
                            icon = Icons.Default.Event
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                MCJobTextField(
                                    value = selectedBooking?.let { "${it.name} • ${it.client ?: "Personal"}" } ?: "Pilih Pekerjaan / Event",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Pilih Acara",
                                    placeholder = "Pilih Pekerjaan / Event",
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = Primary) },
                                    trailingIcon = {
                                        IconButton(onClick = { showBookingDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Job")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Transparent click overlay Box
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showBookingDropdown = true }
                                )

                                DropdownMenu(
                                    expanded = showBookingDropdown,
                                    onDismissRequest = { showBookingDropdown = false }
                                ) {
                                    bookings.forEach { b ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(b.name, fontWeight = FontWeight.Bold)
                                                    Text("Klien: ${b.client ?: "Personal"} • ${Formatter.formatCurrency(b.fee)}", fontSize = 11.sp, color = OnSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectBooking(b)
                                                showBookingDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selected Job Details & Live Invoice Card
                    selectedBooking?.let { b ->
                        // Card 2: Invoice Metadata & Notes Form
                        item {
                            FormSectionCard(
                                title = "PENGATURAN INVOICE",
                                icon = Icons.Default.Tune
                            ) {
                                MCJobTextField(
                                    value = invoiceNumber,
                                    onValueChange = {},
                                    label = "Nomor Invoice",
                                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = Primary) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val formattedIssueDate = remember(issueDate) {
                                        try { Formatter.formatDateShort(LocalDate.parse(issueDate)) } catch (_: Exception) { issueDate }
                                    }
                                    val formattedDueDate = remember(dueDate) {
                                        try { Formatter.formatDateShort(LocalDate.parse(dueDate)) } catch (_: Exception) { dueDate }
                                    }

                                    // Tgl Terbit Card Selector
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Tgl Terbit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                        Surface(
                                            onClick = {
                                                showNativeDatePicker(context, issueDate) { selected ->
                                                    viewModel.updateIssueDate(selected)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = formattedIssueDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            }
                                        }
                                    }

                                    // Jatuh Tempo Card Selector
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Jatuh Tempo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                        Surface(
                                            onClick = {
                                                showNativeDatePicker(context, dueDate) { selected ->
                                                    viewModel.updateDueDate(selected)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Event, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = formattedDueDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            }
                                        }
                                    }
                                }

                                MCJobTextField(
                                    value = notes,
                                    onValueChange = { viewModel.updateNotes(it) },
                                    label = "Catatan / Syarat Pembayaran",
                                    placeholder = "Contoh: Pelunasan H-1 acara via transfer BCA...",
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Primary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    minLines = 3
                                )
                            }
                        }

                        // Card 3: Summary Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Primary
                                            ) {
                                                Text("mcjob.id", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Invoice Resmi", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(invoiceNumber, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFF1F5F9))

                                    Text("DITUJUKAN KEPADA:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(b.client ?: "Personal Client", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = OnBackground)
                                    b.pic?.let { Text("PIC / Kontak: $it", fontSize = 12.sp, color = OnSurfaceVariant) }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("RINCIAN BIAYA ACARA:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(b.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnBackground)
                                                Text(Formatter.formatCurrency(b.fee), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            if (b.dp > 0) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("DP Terbayar", fontSize = 12.sp, color = Success, fontWeight = FontWeight.Medium)
                                                    Text("- ${Formatter.formatCurrency(b.dp)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Success)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("SISA TAGIHAN", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Primary.copy(alpha = 0.1f)
                                        ) {
                                            Text(Formatter.formatCurrency(b.outstanding), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Primary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Card 4: Action Buttons
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.viewInvoice(context) },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Primary),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Preview PDF Draf", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
                                    }

                                    MCJobPrimaryButton(
                                        text = "Simpan PDF",
                                        onClick = { showGenerateConfirm = true },
                                        isLoading = isLoading,
                                        icon = Icons.Default.PictureAsPdf,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (pdfFile != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MCJobSecondaryButton(
                                            text = "Bagikan ke WA",
                                            onClick = { viewModel.shareInvoice(context) },
                                            icon = Icons.Default.Share,
                                            modifier = Modifier.weight(1f)
                                        )

                                        MCJobSecondaryButton(
                                            text = "Buka File PDF",
                                            onClick = { viewModel.viewInvoice(context) },
                                            icon = Icons.Default.Visibility,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 5: Riwayat Invoice Diterbitkan
                    if (invoiceHistory.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Riwayat Invoice Diterbitkan (${invoiceHistory.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        items(invoiceHistory) { inv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (inv.isPaid) Success.copy(alpha = 0.15f) else Primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = inv.status.label,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (inv.isPaid) Success else Primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text("Total: ${Formatter.formatCurrency(inv.totalAmount)} • Tgl: ${inv.issueDate}", fontSize = 11.sp, color = OnSurfaceVariant)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                val target = bookings.firstOrNull { it.id == inv.bookingId }
                                                if (target != null) {
                                                    viewModel.selectBooking(target)
                                                }
                                                viewModel.viewInvoice(context)
                                            }
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = "Lihat", tint = Primary, modifier = Modifier.size(20.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                invoiceToDelete = inv
                                                showDeleteConfirmDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Error, modifier = Modifier.size(20.dp))
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

    // Confirmation Dialog before creating PDF
    val b = selectedBooking
    if (showGenerateConfirm && b != null) {
        MCJobConfirmDialog(
            title = "Buat Invoice Resmi?",
            description = "Invoice $invoiceNumber akan dibuat untuk '${b.client ?: "Personal"}' sebesar ${Formatter.formatCurrency(b.fee)} (Jatuh tempo: $dueDate).",
            primaryCtaText = "Buat Invoice",
            secondaryCtaText = "Periksa Kembali",
            onConfirm = {
                showGenerateConfirm = false
                viewModel.generateInvoice(context)
            },
            onDismiss = { showGenerateConfirm = false }
        )
    }

    // Success Dialog after creating PDF
    if (showSuccessDialog && pdfFile != null) {
        MCJobSuccessDialog(
            title = "Invoice Berhasil Dibuat!",
            description = "File PDF invoice telah siap dan tersimpan di memori perangkat.",
            primaryCtaText = "Buka File PDF",
            secondaryCtaText = "Bagikan via WA",
            onPrimary = {
                showSuccessDialog = false
                viewModel.viewInvoice(context)
            },
            onSecondary = {
                showSuccessDialog = false
                viewModel.shareInvoice(context)
            },
            onDismiss = { showSuccessDialog = false }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        MCJobErrorDialog(
            title = "Invoice Belum Dapat Dibuat",
            description = errorMessage ?: "Terjadi kesalahan saat memproses file invoice. Silakan coba lagi.",
            primaryCtaText = "Coba Lagi",
            secondaryCtaText = "Tutup",
            onRetry = {
                showErrorDialog = false
                viewModel.clearErrorMessage()
                viewModel.generateInvoice(context)
            },
            onDismiss = {
                showErrorDialog = false
                viewModel.clearErrorMessage()
            }
        )
    }

    // Destructive Delete Dialog
    val inv = invoiceToDelete
    if (showDeleteConfirmDialog && inv != null) {
        MCJobDestructiveDialog(
            title = "Hapus Invoice?",
            description = "Invoice ${inv.invoiceNumber} akan dihapus dari riwayat dan cloud Firestore. Data job tetap aman.",
            primaryCtaText = "Hapus Invoice",
            secondaryCtaText = "Batal",
            onConfirm = {
                showDeleteConfirmDialog = false
                viewModel.deleteInvoice(inv)
                invoiceToDelete = null
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                invoiceToDelete = null
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
                ClarifiedSpacer(8.dp)
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary, fontSize = 13.sp)
            }
            HorizontalDivider(color = SurfaceVariant)
            content()
        }
    }
}

@Composable
fun ClarifiedSpacer(width: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
private fun TemplateGraphicMockup(template: InvoiceTemplate) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        when (template) {
            InvoiceTemplate.MODERN_CORPORATE -> {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Header 2 Col
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4F46E5), modifier = Modifier.size(50.dp, 10.dp)) {}
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0F172A), modifier = Modifier.size(40.dp, 10.dp)) {}
                    }
                    HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(shape = RoundedCornerShape(3.dp), color = Color(0xFF94A3B8), modifier = Modifier.size(70.dp, 8.dp)) {}
                        Surface(shape = RoundedCornerShape(3.dp), color = Color(0xFF94A3B8), modifier = Modifier.size(70.dp, 8.dp)) {}
                    }
                    // Table Header Indigo
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4F46E5), modifier = Modifier.fillMaxWidth().height(16.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("TABLE HEADER GRID", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    // Total Box
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEEF2FF), modifier = Modifier.size(110.dp, 20.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SISA: Rp3.000.000", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4F46E5))
                            }
                        }
                    }
                }
            }
            InvoiceTemplate.LUXURY_ELEGANT -> {
                Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                    // Double Frame Line
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFB45309)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Centered Gold Title
                            Text("SURAT PENAWARAN & INVOICE RESMI", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                            HorizontalDivider(color = Color(0xFFB45309), thickness = 0.8.dp)

                            // Gold Tint Box
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEF3C7), modifier = Modifier.fillMaxWidth().height(22.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("ACARA: WEDDING GALA • HOTEL KEMPINSKI", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Syarat & Kebijakan DP H-1", fontSize = 7.sp, color = Color(0xFF64748B))
                                // Circular Stamp
                                Surface(shape = CircleShape, color = Color.White, border = BorderStroke(1.dp, Color(0xFFB45309)), modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("SEAL", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            InvoiceTemplate.MINIMALIST_CREATIVE -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Thick Left Emerald Bar
                    Surface(color = Color(0xFF059669), modifier = Modifier.width(8.dp).fillMaxHeight()) {}
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("INVOICE", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF18181B))
                            Text("#INV-2026", fontSize = 8.sp, color = Color(0xFF71717A))
                        }
                        // Grid Cards
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE4E4E7), modifier = Modifier.weight(1f).height(24.dp)) {}
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE4E4E7), modifier = Modifier.weight(1f).height(24.dp)) {}
                        }
                        // Emerald Solid Total Banner
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF059669), modifier = Modifier.fillMaxWidth().height(24.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("TOTAL INVOICE", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Rp5.000.000", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
