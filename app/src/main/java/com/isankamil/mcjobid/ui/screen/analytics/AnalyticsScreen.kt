package com.isankamil.mcjobid.ui.screen.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.*
import com.isankamil.mcjobid.ui.components.CalendarView
import com.isankamil.mcjobid.ui.components.EmptyStateView
import com.isankamil.mcjobid.ui.components.feedback.MCJobErrorDialog
import com.isankamil.mcjobid.ui.components.feedback.MCJobInfoTooltip
import com.isankamil.mcjobid.ui.components.feedback.MCJobPrimaryButton
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit,
    onCreateJobClick: () -> Unit = {},
    onFollowUpClick: () -> Unit = {},
    onFinanceClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedTimePeriod.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val allBookings by viewModel.allBookings.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()

    var selectedTrendMonth by remember { mutableStateOf<MonthlyPerformanceTrend?>(null) }

    // Picker States
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Analisis & Insight Performa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        MCJobInfoTooltip(tooltipText = "Evaluasi performa finansial, tren pendapatan bersih, profitabilitas kategori event, dan insight cerdas untuk pengembangan bisnis MC Anda.")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState is AnalyticsUiState.Success) {
                        val data = (uiState as AnalyticsUiState.Success).data
                        IconButton(onClick = { viewModel.sharePerformanceSummary(context, data) }) {
                            Icon(Icons.Default.Share, contentDescription = "Bagikan Laporan", tint = Primary)
                        }
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = Primary)
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
            // Period Filter Tabs
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(AnalyticsTimePeriod.values()) { period ->
                        val isSelected = selectedPeriod == period
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Primary else SurfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    when (period) {
                                        AnalyticsTimePeriod.CUSTOM_MONTH -> showMonthPicker = true
                                        AnalyticsTimePeriod.CUSTOM_RANGE -> showDateRangePicker = true
                                        else -> viewModel.setTimePeriod(period)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (period == AnalyticsTimePeriod.CUSTOM_MONTH || period == AnalyticsTimePeriod.CUSTOM_RANGE) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else OnSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = period.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            when (val state = uiState) {
                is AnalyticsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                is AnalyticsUiState.Error -> {
                    MCJobErrorDialog(
                        title = "Gagal Memuat Analitika",
                        description = state.message,
                        onRetry = { viewModel.refresh() },
                        onDismiss = onBack
                    )
                }

                is AnalyticsUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.Analytics,
                            title = "Belum Ada Data Acara",
                            description = "Catat job pertama Anda untuk membuka analisis performa pendapatan, arus kas, efisiensi operasional, dan insight bisnis MC.",
                            actionText = "Catat Job Pertama",
                            onActionClick = onCreateJobClick
                        )
                    }
                }

                is AnalyticsUiState.Success -> {
                    val data = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Hero Net Income Card
                        item {
                            NetIncomeHeroCard(data = data)
                        }

                        // 2. Key Business Metrics Grid
                        item {
                            BusinessMetricsGrid(data = data)
                        }

                        // 3. Smart Automated Insights Section
                        if (data.insights.isNotEmpty()) {
                            item {
                                SmartInsightsSection(
                                    insights = data.insights,
                                    onActionClick = { route ->
                                        when (route) {
                                            "create_job" -> onCreateJobClick()
                                            "follow_up" -> onFollowUpClick()
                                            "finance" -> onFinanceClick()
                                        }
                                    }
                                )
                            }
                        }

                        // 4. Monthly Trend Chart
                        if (data.monthlyTrends.isNotEmpty() && data.monthlyTrends.any { it.grossRevenue > 0 || it.totalExpenses > 0 }) {
                            item {
                                MonthlyTrendSection(
                                    trends = data.monthlyTrends,
                                    selectedTrend = selectedTrendMonth,
                                    onSelectTrend = { selectedTrendMonth = if (selectedTrendMonth == it) null else it }
                                )
                            }
                        }

                        // 5. Category Breakdown Analysis
                        if (data.categoryBreakdowns.isNotEmpty()) {
                            item {
                                CategoryBreakdownSection(breakdowns = data.categoryBreakdowns)
                            }
                        }

                        // 6. Top Clients / WO Partners
                        if (data.topClients.isNotEmpty()) {
                            item {
                                TopClientsSection(
                                    topClients = data.topClients,
                                    repeatRate = data.repeatClientRate
                                )
                            }
                        }

                        // Bottom Spacer for smooth scrolling
                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }

    // --- PICKER BOTTOM SHEETS ---

    // --- PICKER BOTTOM SHEETS ---

    if (showMonthPicker) {
        var pickerMonth by remember { mutableStateOf(YearMonth.now()) }
        ModalBottomSheet(
            onDismissRequest = { showMonthPicker = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceVariant) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    "Pilih Bulan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnBackground,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                
                CalendarView(
                    currentMonth = pickerMonth,
                    onMonthChange = { pickerMonth = it },
                    bookings = allBookings,
                    reminders = allReminders,
                    onDateClick = { date ->
                        viewModel.setCustomMonth(YearMonth.from(date))
                        showMonthPicker = false
                    },
                    selectedDate = null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { showMonthPicker = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDateRangePicker) {
        var pickerMonth by remember { mutableStateOf(YearMonth.now()) }
        var tempStart by remember { mutableStateOf<LocalDate?>(null) }
        
        ModalBottomSheet(
            onDismissRequest = { 
                showDateRangePicker = false
                tempStart = null 
            },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceVariant) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = if (tempStart == null) "Pilih Tanggal Mulai" else "Pilih Tanggal Akhir",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnBackground,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )

                if (tempStart != null) {
                    Text(
                        text = "Mulai: ${Formatter.formatDate(tempStart!!)}",
                        fontSize = 12.sp,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    )
                }

                CalendarView(
                    currentMonth = pickerMonth,
                    onMonthChange = { pickerMonth = it },
                    bookings = allBookings,
                    reminders = allReminders,
                    onDateClick = { date ->
                        if (tempStart == null) {
                            tempStart = date
                        } else {
                            viewModel.setCustomDateRange(tempStart!!, date)
                            showDateRangePicker = false
                            tempStart = null
                        }
                    },
                    selectedDate = tempStart
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { 
                        showDateRangePicker = false
                        tempStart = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NetIncomeHeroCard(data: PerformanceAnalyticsResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NET INCOME (LABA BERSIH)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    MCJobInfoTooltip(tooltipText = "Pendapatan bersih setelah dikurangi seluruh beban operasional (Gross Revenue - Total Expenses).")
                }

                if (data.growthPercentage != 0.0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (data.growthPercentage > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (data.growthPercentage > 0) "+" else ""}${String.format(Locale.US, "%.1f", data.growthPercentage)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = Formatter.formatCurrency(data.netIncome),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Margin: ${String.format(Locale.US, "%.1f", data.profitMargin)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Periode: ${data.displayLabel}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.White.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Omset Bruto", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = Formatter.formatCurrency(data.grossRevenue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Pengeluaran Beban", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = "- ${Formatter.formatCurrency(data.totalExpenses)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB4AB)
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessMetricsGrid(data: PerformanceAnalyticsResult) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "METRIK PERFORMA BISNIS MC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            MCJobInfoTooltip(tooltipText = "Ringkasan indikator performa utama: total job, rata-rata tarif, pelunasan kas, dan rasio kolektibilitas piutang.")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsMetricCard(
                title = "Total Event Job",
                value = "${data.totalJobs} Acara",
                subtitle = "${data.completedJobs} selesai • ${data.upcomingJobs} mendatang",
                icon = Icons.Default.Event,
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            AnalyticsMetricCard(
                title = "Rata-rata Tarif / Fee",
                value = Formatter.formatCurrency(data.averageFee),
                subtitle = "Per acara",
                icon = Icons.Default.PriceCheck,
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsMetricCard(
                title = "Kas Masuk (DP/Lunas)",
                value = Formatter.formatCurrency(data.totalPaid),
                subtitle = "Collection rate: ${String.format(Locale.US, "%.1f", data.collectionRate)}%",
                icon = Icons.Default.AccountBalanceWallet,
                color = Success,
                modifier = Modifier.weight(1f)
            )
            AnalyticsMetricCard(
                title = "Sisa Piutang Klien",
                value = Formatter.formatCurrency(data.totalOutstanding),
                subtitle = if (data.totalOutstanding > 0) "Perlu follow up" else "Semua lunas 100%",
                icon = Icons.Default.HourglassBottom,
                color = if (data.totalOutstanding > 0) Warning else Success,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SmartInsightsSection(
    insights: List<SmartInsight>,
    onActionClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SMART AUTOMATED INSIGHTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            MCJobInfoTooltip(tooltipText = "Rekomendasi otomatis berbasis data untuk memaksimalkan arus kas, efisiensi beban, dan strategi harga MC Anda.")
        }

        insights.forEach { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        val (icon, tint) = when (insight.type) {
                            InsightType.CASH_FLOW_WARNING -> Pair(Icons.Default.WarningAmber, Warning)
                            InsightType.REVENUE_GROWTH -> Pair(Icons.AutoMirrored.Filled.TrendingUp, Primary)
                            InsightType.RATE_CARD_OPTIMIZATION -> Pair(Icons.Default.AutoGraph, Primary)
                            InsightType.TOP_PARTNER -> Pair(Icons.Default.Handshake, Success)
                            InsightType.EXPENSE_EFFICIENCY -> Pair(Icons.Default.Savings, Primary)
                            InsightType.SEASONAL_PEAK -> Pair(Icons.Default.Star, Warning)
                            InsightType.POSITIVE_MILESTONE -> Pair(Icons.Default.CheckCircle, Success)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tint.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insight.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = insight.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            if (insight.actionLabel != null && insight.actionRoute != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { onActionClick(insight.actionRoute) }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${insight.actionLabel} ➔",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
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

@Composable
private fun MonthlyTrendSection(
    trends: List<MonthlyPerformanceTrend>,
    selectedTrend: MonthlyPerformanceTrend?,
    onSelectTrend: (MonthlyPerformanceTrend) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Tren Omset & Beban",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pergerakan 6 bulan terakhir (Tekan bar untuk detail)",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxVal = trends.maxOfOrNull { maxOf(it.grossRevenue, it.totalExpenses) }?.coerceAtLeast(1L) ?: 1L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                trends.forEach { item ->
                    val isSelected = selectedTrend?.yearMonth == item.yearMonth
                    val revRatio = (item.grossRevenue.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                    val expRatio = (item.totalExpenses.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)

                    val barHeightRev = (100 * revRatio).coerceAtLeast(6f)
                    val barHeightExp = (100 * expRatio).coerceAtLeast(if (item.totalExpenses > 0) 6f else 0f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .clickable { onSelectTrend(item) }
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Revenue Bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(barHeightRev.dp)
                                    .background(
                                        color = if (isSelected) Primary else Primary.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            // Expense Bar
                            if (item.totalExpenses > 0) {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(barHeightExp.dp)
                                        .background(
                                            color = if (isSelected) Error else Error.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.monthLabel,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Primary else OnSurfaceVariant
                        )
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).background(Primary, shape = CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Omset", fontSize = 10.sp, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(10.dp).background(Error, shape = CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pengeluaran", fontSize = 10.sp, color = OnSurfaceVariant)
            }

            // Selected Month Detail Sheet/Card
            selectedTrend?.let { sel ->
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Rincian ${sel.monthLabel} (${sel.yearMonth}): ${sel.eventCount} Acara",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Omset: ${Formatter.formatCurrency(sel.grossRevenue)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Beban: ${Formatter.formatCurrency(sel.totalExpenses)}", fontSize = 11.sp, color = Error, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Laba Bersih: ${Formatter.formatCurrency(sel.netIncome)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (sel.netIncome >= 0) Success else Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(breakdowns: List<CategoryBreakdown>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profitabilitas Kategori Acara",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${breakdowns.size} Kategori",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Text(
                text = "Porsi kontribusi omset dan rata-rata tarif per jenis event",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            breakdowns.forEachIndexed { index, cat ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat.category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${cat.totalEvents} Event",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatter.formatCurrency(cat.totalRevenue),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Primary
                            )
                            Text(
                                text = "Avg: ${Formatter.formatCurrency(cat.averageFee)}",
                                fontSize = 10.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Visual Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (cat.percentageOfTotal / 100.0).toFloat().coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (index == 0) Primary else Primary.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}% dari total omset",
                        fontSize = 10.sp,
                        color = OnSurfaceVariant
                    )

                    if (index < breakdowns.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopClientsSection(
    topClients: List<ClientPerformance>,
    repeatRate: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Partner & Klien MC",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (repeatRate > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Success.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Repeat: ${String.format(Locale.US, "%.1f", repeatRate)}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Text(
                text = "Mitra WO, EO, dan klien dengan kontribusi transaksi terbesar",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            topClients.forEachIndexed { idx, client ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (idx == 0) Warning.copy(alpha = 0.2f) else Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (idx == 0) Warning else Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = client.clientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${client.eventCount} Event ${if (client.isRepeatClient) "• Repeat Partner ⭐" else ""}",
                                fontSize = 10.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = Formatter.formatCurrency(client.totalRevenue),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Primary
                    )
                }
                if (idx < topClients.size - 1) {
                    HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
