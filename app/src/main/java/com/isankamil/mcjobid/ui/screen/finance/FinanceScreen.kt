package com.isankamil.mcjobid.ui.screen.finance

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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.ui.components.EmptyStateView
import com.isankamil.mcjobid.ui.components.ExpenseDialog
import com.isankamil.mcjobid.ui.components.FinanceChart
import com.isankamil.mcjobid.ui.components.PaymentDialog
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val selectedPaymentFilter by viewModel.selectedPaymentFilter.collectAsState()
    val selectedExpenseCategory by viewModel.selectedExpenseCategory.collectAsState()
    val chartData by viewModel.monthlyChartData.collectAsState()
    val filteredBookings by viewModel.filteredBookings.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var selectedBookingForPayment by remember { mutableStateOf<Booking?>(null) }
    var showBookingPicker by remember { mutableStateOf(false) }
    var pendingPaymentData by remember { mutableStateOf<FinancePaymentDraft?>(null) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val snackBarHostState = remember { SnackbarHostState() }

    // Show error and success messages via Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackBarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackBarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Utang & Piutang",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        MCJobInfoTooltip(tooltipText = "Pusat pemantauan sisa piutang klien, utang & pengeluaran operasional vendor, dan arus kas honor MC.")
                    }
                },
                actions = {
                    IconButton(onClick = { showBookingPicker = true }) {
                        Icon(Icons.Default.AddCard, contentDescription = "Tambah Pelunasan", tint = Primary)
                    }
                    IconButton(onClick = { showExpenseDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Catat Pengeluaran Pribadi", tint = Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Modern Dual-Tab Selector
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 3.dp,
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabPill(
                            title = "Piutang",
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = selectedTab == FinanceTab.PIUTANG,
                            badgeCount = allBookings.count { it.outstanding > 0 && it.status != Booking.BookingStatus.CANCELLED },
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setTab(FinanceTab.PIUTANG)
                        }

                        TabPill(
                            title = "Beban",
                            icon = Icons.Default.Receipt,
                            isSelected = selectedTab == FinanceTab.UTANG_PENGELUARAN,
                            badgeCount = filteredExpenses.size,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setTab(FinanceTab.UTANG_PENGELUARAN)
                        }

                        TabPill(
                            title = "Arus Kas",
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            isSelected = selectedTab == FinanceTab.ARUS_KAS,
                            badgeCount = 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setTab(FinanceTab.ARUS_KAS)
                        }
                    }
                }
            }

            // Hero Metric Boxes based on Active Tab
            item {
                when (selectedTab) {
                    FinanceTab.PIUTANG -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FinanceSummaryBox("TOTAL OMSET", Formatter.formatCurrency(summary.totalHonor), Primary, Modifier.weight(1f))
                            FinanceSummaryBox("TERBAYAR (DP)", Formatter.formatCurrency(summary.totalPaid), Success, Modifier.weight(1f))
                            FinanceSummaryBox("SISA PIUTANG", Formatter.formatCurrency(summary.totalOutstanding), Warning, Modifier.weight(1f))
                        }
                    }
                    FinanceTab.UTANG_PENGELUARAN -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FinanceSummaryBox("TOTAL BEBAN", Formatter.formatCurrency(summary.totalExpenses), Error, Modifier.weight(1f))
                            FinanceSummaryBox("TOTAL OMSET", Formatter.formatCurrency(summary.totalHonor), Primary, Modifier.weight(1f))
                            FinanceSummaryBox("LABA BERSIH", Formatter.formatCurrency(summary.netIncome), Success, Modifier.weight(1f))
                        }
                    }
                    FinanceTab.ARUS_KAS -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FinanceSummaryBox("OMSET MASUK", Formatter.formatCurrency(summary.totalHonor), Primary, Modifier.weight(1f))
                            FinanceSummaryBox("BEBAN KELUAR", Formatter.formatCurrency(summary.totalExpenses), Error, Modifier.weight(1f))
                            FinanceSummaryBox("NET INCOME", Formatter.formatCurrency(summary.netIncome), Success, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Content according to Selected Tab
            when (selectedTab) {
                FinanceTab.PIUTANG -> {
                    // Filter Chips for Piutang
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daftar Piutang & Pembayaran Job",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackground
                                )
                                Text(
                                    text = "${filteredBookings.size} Job",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(listOf(
                                    PaymentFilter.ALL to "Semua",
                                    PaymentFilter.OUTSTANDING to "Piutang Aktif",
                                    PaymentFilter.PARTIAL to "DP Masuk",
                                    PaymentFilter.PAID to "Lunas"
                                )) { (filter, label) ->
                                    val isSelected = selectedPaymentFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setPaymentFilter(filter) },
                                        label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

                    if (filteredBookings.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = "Tidak Ada Data Piutang",
                                description = "Tidak ditemukan job dengan kriteria piutang yang dipilih."
                            )
                        }
                    } else {
                        items(filteredBookings) { booking ->
                            PiutangItemCard(
                                booking = booking,
                                onClick = { onBookingClick(booking.id) },
                                onAddPaymentClick = { selectedBookingForPayment = booking },
                                onFollowUpClick = {
                                    val clientPhone = booking.pic ?: ""
                                    val formattedPhone = Formatter.formatWhatsAppNumber(clientPhone)
                                    val message = "Halo Kak ${booking.client ?: ""}, salam dari MC. Izin mengonfirmasi terkait status pelunasan honor untuk acara *${booking.name}* dengan sisa tagihan sebesar *${Formatter.formatCurrency(booking.outstanding)}*. Terima kasih banyak 🙏"
                                    val encodedMsg = Uri.encode(message)
                                    val url = if (formattedPhone.isNotBlank()) "https://wa.me/$formattedPhone?text=$encodedMsg" else "https://wa.me/?text=$encodedMsg"
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    } catch (_: Exception) {}
                                }
                            )
                        }
                    }
                }

                FinanceTab.UTANG_PENGELUARAN -> {
                    // Category Chips for Utang/Beban
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Beban Operasional & Vendor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackground
                                )
                                Text(
                                    text = "${filteredExpenses.size} Item",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(listOf("ALL", "Transport", "Wardrobe", "MUA & Hairdo", "Sound / Mic", "Lainnya")) { cat ->
                                    val isSelected = selectedExpenseCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setExpenseCategory(cat) },
                                        label = { Text(if (cat == "ALL") "Semua" else cat, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

                    if (filteredExpenses.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                title = "Belum Ada Pengeluaran",
                                description = "Catatan beban operasional seperti transport, wardrobe, MUA, atau sound akan muncul di sini."
                            )
                        }
                    } else {
                        items(filteredExpenses) { expense ->
                            val linkedBooking = allBookings.find { it.id == expense.bookingId }
                            ExpenseItemCard(
                                expense = expense,
                                bookingName = linkedBooking?.name ?: "Job #${expense.bookingId.takeLast(4)}",
                                onDeleteClick = { expenseToDelete = expense }
                            )
                        }
                    }
                }

                FinanceTab.ARUS_KAS -> {
                    item {
                        FinanceChart(data = chartData)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Ringkasan Arus Kas & Margin",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Omset Bruto", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                    Text(Formatter.formatCurrency(summary.totalHonor), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Beban Operasional", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                    Text("- ${Formatter.formatCurrency(summary.totalExpenses)}", fontWeight = FontWeight.Bold, color = Error)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pendapatan Bersih (Net)", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                    Text(Formatter.formatCurrency(summary.netIncome), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = Success)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Booking Picker Dialog
    if (showBookingPicker) {
        val pickable = allBookings.filter { it.status != Booking.BookingStatus.CANCELLED }
        AlertDialog(
            onDismissRequest = { showBookingPicker = false },
            title = { Text("Pilih Job untuk Pelunasan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                if (pickable.isEmpty()) {
                    Text("Belum ada job yang bisa dipilih.", color = OnSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(pickable) { b ->
                            Surface(
                                onClick = {
                                    selectedBookingForPayment = b
                                    showBookingPicker = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = listOfNotNull(b.client, Formatter.formatDateShort(b.date)).joinToString(" • "),
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Sisa ${Formatter.formatCurrency(b.outstanding)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (b.outstanding > 0) Error else Success
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookingPicker = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Payment Dialog Input
    selectedBookingForPayment?.let { booking ->
        PaymentDialog(
            bookingName = booking.name,
            totalFee = booking.fee,
            currentPaid = booking.dp,
            onSavePayment = { amount, date, method, notes ->
                val remaining = maxOf(0L, booking.fee - booking.dp - amount)
                val isOver = amount > (booking.fee - booking.dp)
                pendingPaymentData = FinancePaymentDraft(booking, amount, date, method, notes, remaining, isOver)
                selectedBookingForPayment = null
            },
            onDismiss = { selectedBookingForPayment = null }
        )
    }

    // Payment Confirmation Modal
    pendingPaymentData?.let { draft ->
        val b = draft.booking
        MCJobPaymentConfirmationDialog(
            bookingTitle = b.name,
            amount = draft.amount,
            paymentMethod = draft.method,
            paymentDate = draft.date,
            totalFee = b.fee,
            currentPaid = b.dp,
            remainingAfterPayment = draft.remaining,
            isOverpayment = draft.isOverpayment,
            onConfirm = {
                viewModel.addPayment(b.id, draft.amount, draft.date, draft.method, draft.notes)
                pendingPaymentData = null
            },
            onDismiss = { pendingPaymentData = null }
        )
    }

    // Global Expense Dialog
    if (showExpenseDialog) {
        ExpenseDialog(
            availableBookings = allBookings.filter { it.status != Booking.BookingStatus.CANCELLED },
            onSaveExpense = { bId, category, amount, date, note ->
                viewModel.addExpense(bId, category, amount, date, note)
                showExpenseDialog = false
            },
            onDismiss = { showExpenseDialog = false }
        )
    }

    // Destructive Delete Confirmation for Expense
    expenseToDelete?.let { exp ->
        MCJobDestructiveDialog(
            title = "Hapus Catatan Pengeluaran?",
            description = "Catatan pengeluaran ${exp.category} sebesar ${Formatter.formatCurrency(exp.amount)} akan dihapus permanen.",
            primaryCtaText = "Hapus Pengeluaran",
            onConfirm = {
                viewModel.deleteExpense(exp)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }

    // Success & Error Feedback Dialogs
    successMessage?.let { msg ->
        MCJobSuccessDialog(
            title = "Berhasil",
            description = msg,
            primaryCtaText = "OK",
            secondaryCtaText = null,
            onDismiss = { viewModel.clearMessages() }
        )
    }

    errorMessage?.let { err ->
        MCJobErrorDialog(
            title = "Terjadi Kendala",
            description = err,
            onRetry = { viewModel.clearMessages() },
            onDismiss = { viewModel.clearMessages() }
        )
    }
}

private data class FinancePaymentDraft(
    val booking: Booking,
    val amount: Long,
    val date: String,
    val method: String,
    val notes: String,
    val remaining: Long,
    val isOverpayment: Boolean
)

@Composable
private fun TabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Primary else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else OnSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else OnSurfaceVariant,
                maxLines = 1
            )
            if (badgeCount > 0 && !isSelected) {
                Spacer(modifier = Modifier.width(3.dp))
                Surface(
                    shape = CircleShape,
                    color = Warning
                ) {
                    Text(
                        text = "$badgeCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceSummaryBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun PiutangItemCard(
    booking: Booking,
    onClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onFollowUpClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Klien: ${booking.client ?: "Personal"} • ${booking.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                val (badgeBg, badgeText, badgeColor) = when {
                    booking.outstanding == 0L && booking.fee > 0 -> Triple(Success.copy(alpha = 0.15f), "LUNAS", Success)
                    booking.dp > 0 -> Triple(Warning.copy(alpha = 0.15f), "DP MASUK", Warning)
                    else -> Triple(Error.copy(alpha = 0.15f), "BELUM BAYAR", Error)
                }

                Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Honor: ${Formatter.formatCurrency(booking.fee)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Terbayar: ${Formatter.formatCurrency(booking.dp)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Success)
            }

            if (booking.outstanding > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sisa Piutang: ${Formatter.formatCurrency(booking.outstanding)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MCJobSecondaryButton(
                        text = "Follow Up WA",
                        onClick = onFollowUpClick,
                        icon = Icons.AutoMirrored.Filled.Chat,
                        modifier = Modifier.weight(1f)
                    )

                    MCJobPrimaryButton(
                        text = "Catat Bayar",
                        onClick = onAddPaymentClick,
                        icon = Icons.Default.Add,
                        containerColor = Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    bookingName: String,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Error.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.category,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$bookingName • ${expense.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    if (!expense.note.isNullOrBlank()) {
                        Text(
                            text = expense.note,
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatter.formatCurrency(expense.amount),
                    fontWeight = FontWeight.ExtraBold,
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.minimumInteractiveComponentSize().size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
