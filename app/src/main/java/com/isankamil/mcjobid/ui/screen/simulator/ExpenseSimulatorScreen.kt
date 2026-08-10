package com.isankamil.mcjobid.ui.screen.simulator

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.ExpenseSimulationPdfGenerator
import com.isankamil.mcjobid.util.Formatter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSimulatorScreen(
    viewModel: ExpenseSimulatorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val jobTitle by viewModel.jobTitle.collectAsState()
    val grossFee by viewModel.grossFee.collectAsState()
    val expenseItems by viewModel.expenseItems.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val marginPercentage by viewModel.profitMarginPercentage.collectAsState()
    val recommendedMinFee by viewModel.recommendedMinFee.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var grossFeeInput by remember(grossFee) { mutableStateOf(if (grossFee > 0) grossFee.toString() else "") }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val pdfGenerator = remember { ExpenseSimulationPdfGenerator(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Simulasi Biaya & Profit Job", fontWeight = FontWeight.Bold)
                        Text("Kalkulator Estimasi Pengeluaran Agenda", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (grossFee > 0 || expenseItems.isNotEmpty() || jobTitle.isNotBlank()) {
                        IconButton(onClick = { viewModel.resetSimulation() }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Form", tint = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Proyeksi Laba Bersih Card
            item {
                val isHealthy = marginPercentage >= 65.0
                val marginColor = if (isHealthy) Success else Warning

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROYEKSI LABA BERSIH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            if (grossFee > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = marginColor
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", marginPercentage)}% Margin",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatter.formatCurrency(netProfit),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("HONORARIUM GROSS", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = Formatter.formatCurrency(grossFee),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ESTIMASI BIAYA", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = Formatter.formatCurrency(totalExpenses),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }

                        if (recommendedMinFee > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("💡 Rekomendasi Tarif Min.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                                    Text(
                                        text = Formatter.formatCurrency(recommendedMinFee),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Warning
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 1: Input Target Honor & Nama Agenda
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "HONORARIUM & NAMA AGENDA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary,
                            letterSpacing = 0.5.sp
                        )

                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { viewModel.updateJobTitle(it) },
                            label = { Text("Nama / Topik Acara") },
                            placeholder = { Text("misal: Wedding Resepsi Pak Anton") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = grossFeeInput,
                            onValueChange = {
                                grossFeeInput = it
                                val parsed = it.toLongOrNull() ?: 0L
                                viewModel.updateGrossFee(parsed)
                            },
                            label = { Text("Penawaran Honor / Budget Gross (Rp)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Primary) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val parsedFee = grossFeeInput.toLongOrNull() ?: 0L
                        if (parsedFee > 0) {
                            Text(
                                text = "Nominal Honor: ${Formatter.formatCurrency(parsedFee)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // Card 2: Daftar Item Pengeluaran
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RINCIAN ESTIMASI PENGELUARAN (${expenseItems.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary,
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = { showAddItemDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Tambah Biaya", tint = Primary)
                            }
                        }

                        HorizontalDivider(color = SurfaceVariant)

                        if (expenseItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum Ada Item Biaya Disimulasikan",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tambahkan estimasi bensin, attire, MUA, atau kru di bawah.",
                                        fontSize = 11.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                expenseItems.forEach { item ->
                                    val catColor = when (item.category) {
                                        "Transportasi" -> Color(0xFF0284C7)
                                        "MUA & Attire" -> Color(0xFFE11D48)
                                        "Asisten & Crew" -> Color(0xFF059669)
                                        "Akomodasi" -> Color(0xFFD97706)
                                        else -> Color(0xFF64748B)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = catColor.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = item.category,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = catColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = Formatter.formatCurrency(item.amount),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC2626)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { viewModel.removeExpenseItem(item.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showAddItemDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Item Biaya", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Card 3: Action Buttons (Cetak PDF & Salin Ringkasan)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Tombol Cetak & Bagikan PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isGeneratingPdf = true
                                pdfGenerator.generatePdf(
                                    jobTitle = jobTitle,
                                    grossFee = grossFee,
                                    expenseItems = expenseItems,
                                    totalExpenses = totalExpenses,
                                    netProfit = netProfit,
                                    marginPercentage = marginPercentage,
                                    recommendedMinFee = recommendedMinFee,
                                    userProfile = userProfile,
                                    onSuccess = { file ->
                                        isGeneratingPdf = false
                                        pdfGenerator.openPdf(file)
                                    },
                                    onError = { err ->
                                        isGeneratingPdf = false
                                        Toast.makeText(context, "Gagal membuat PDF: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Preview PDF 📄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                isGeneratingPdf = true
                                pdfGenerator.generatePdf(
                                    jobTitle = jobTitle,
                                    grossFee = grossFee,
                                    expenseItems = expenseItems,
                                    totalExpenses = totalExpenses,
                                    netProfit = netProfit,
                                    marginPercentage = marginPercentage,
                                    recommendedMinFee = recommendedMinFee,
                                    userProfile = userProfile,
                                    onSuccess = { file ->
                                        isGeneratingPdf = false
                                        pdfGenerator.sharePdf(file)
                                    },
                                    onError = { err ->
                                        isGeneratingPdf = false
                                        Toast.makeText(context, "Gagal membagikan PDF: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan PDF 📤", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }

                    // Tombol Salin Ringkasan Teks
                    OutlinedButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnBackground),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Ringkasan Teks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Salin Ringkasan Report (Polesan Elegan dengan Background Putih & Kartu Rapi)
    if (showReportDialog) {
        val reportText = remember(jobTitle, grossFee, totalExpenses, netProfit, marginPercentage, expenseItems) {
            viewModel.generateSimulationReportText()
        }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Primary)
                    }
                }
            },
            title = {
                Text(
                    text = "Ringkasan Hasil Simulasi",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = OnBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Berikut ringkasan proyeksi laba bersih dan rincian biaya yang siap disalin:",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = reportText,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(reportText))
                        showReportDialog = false
                        Toast.makeText(context, "Ringkasan simulasi disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Teks", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Tutup", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Dialog Tambah Biaya Simulasi (Polesan Standar Proyek, Background Putih & Input Rapi)
    if (showAddItemDialog) {
        var nameInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Transportasi") }
        var amountInput by remember { mutableStateOf("") }

        val categories = listOf("Transportasi", "MUA & Attire", "Asisten & Crew", "Akomodasi", "Lainnya")

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Tambah Item Biaya",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Pengeluaran *") },
                        placeholder = { Text("misal: Sewa Jas / Bensin & Tol") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Kategori Biaya:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val isSelected = cat == categoryInput
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { categoryInput = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Estimasi Biaya (Rp) *") },
                        placeholder = { Text("250000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val parsedAmount = amountInput.toLongOrNull() ?: 0L
                    if (parsedAmount > 0) {
                        Text(
                            text = "Nominal: ${Formatter.formatCurrency(parsedAmount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toLongOrNull() ?: 0L
                        if (nameInput.isNotBlank() && amt > 0) {
                            viewModel.addExpenseItem(nameInput, categoryInput, amt)
                            showAddItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tambah Biaya", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Batal", color = OnSurfaceVariant)
                }
            }
        )
    }
}
