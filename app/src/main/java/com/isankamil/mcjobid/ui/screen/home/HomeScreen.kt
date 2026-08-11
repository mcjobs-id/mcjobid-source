package com.isankamil.mcjobid.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.FinancialSummary
import com.isankamil.mcjobid.domain.model.Reminder
import com.isankamil.mcjobid.ui.components.CalendarView
import com.isankamil.mcjobid.ui.components.DashboardUpdateBanner
import com.isankamil.mcjobid.ui.components.AppUpdateModalDialog
import com.isankamil.mcjobid.ui.components.MCJobAvatarImage
import com.isankamil.mcjobid.ui.components.McJobIdLogo
import com.isankamil.mcjobid.ui.components.QuickAddFab
import com.isankamil.mcjobid.ui.components.feedback.MCJobInfoTooltip
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookingClick: (String) -> Unit,
    onAddJobClick: () -> Unit,
    onAddClientClick: () -> Unit,
    onCreateInvoiceClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddReminderClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMcDayModeClick: (String) -> Unit,
    onTestimonialClick: () -> Unit,
    onPriceListClick: () -> Unit,
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onQuickActionSettingsClick: () -> Unit = {},
    onTodoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val financialSummary by viewModel.financialSummary.collectAsState()
    val selectedFilter by viewModel.selectedTimeFilter.collectAsState()
    val customStartDate by viewModel.customStartDate.collectAsState()
    val customEndDate by viewModel.customEndDate.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val activeBookings by viewModel.activeBookings.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    val quickActionEnabled by viewModel.quickActionEnabled.collectAsState()
    val qaAddJobEnabled by viewModel.qaAddJobEnabled.collectAsState()
    val qaAddClientEnabled by viewModel.qaAddClientEnabled.collectAsState()
    val qaAddPaymentEnabled by viewModel.qaAddPaymentEnabled.collectAsState()
    val qaAddExpenseEnabled by viewModel.qaAddExpenseEnabled.collectAsState()
    val qaReminderEnabled by viewModel.qaReminderEnabled.collectAsState()
    val qaRateCardEnabled by viewModel.qaRateCardEnabled.collectAsState()
    val qaInvoiceEnabled by viewModel.qaInvoiceEnabled.collectAsState()
    val qaAnalyticsEnabled by viewModel.qaAnalyticsEnabled.collectAsState()
    val qaNotificationsEnabled by viewModel.qaNotificationsEnabled.collectAsState()
    val qaProfileEnabled by viewModel.qaProfileEnabled.collectAsState()
    val qaSettingsEnabled by viewModel.qaSettingsEnabled.collectAsState()
    val qaTodoEnabled by viewModel.qaTodoEnabled.collectAsState()

    // OTA In-App Update States
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isUpdateBannerDismissed by viewModel.isUpdateBannerDismissed.collectAsState()
    val showUpdateModal by viewModel.showUpdateModal.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val updateErrorMessage by viewModel.updateErrorMessage.collectAsState()

    var selectedFilterChip by remember { mutableStateOf<String?>(null) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showCustomDateRangePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val todayBooking = remember(activeBookings) {
        activeBookings.firstOrNull {
            it.date == LocalDate.now() &&
            it.status != Booking.BookingStatus.CANCELLED &&
            it.status != Booking.BookingStatus.COMPLETED
        }
    }

    val nextBooking = remember(activeBookings) {
        val today = LocalDate.now()
        activeBookings
            .filter { !it.date.isBefore(today) && it.status != Booking.BookingStatus.CANCELLED }
            .minByOrNull { it.date }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Top App Bar
            item {
                HomeHeader(
                    mcName = userProfile?.name ?: "MC Professional",
                    photoUri = userProfile?.photoUri,
                    unreadNotificationCount = reminders.size,
                    isSyncing = isSyncing,
                    isOnline = isOnline,
                    onNotificationClick = onNotificationClick,
                    onProfileClick = onProfileClick,
                    onSearchClick = onSearchClick
                )
            }

            // Hero Financial Surface (Direct Compact Hero Card)
            item {
                HeroFinancialSurface(
                    summary = financialSummary,
                    selectedFilter = selectedFilter,
                    customStartDate = customStartDate,
                    customEndDate = customEndDate,
                    onSelectFilter = { viewModel.selectTimeFilter(it) },
                    onOpenCustomDatePicker = { showCustomDateRangePicker = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 2 CTA Shortcut Cards: Testimoni & Bantuan Kendala
            item {
                TopQuickFilterChipsSurface(
                    onTestimonialClick = onTestimonialClick,
                    onSupportClick = { showSupportDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Banner Update Aplikasi (tampil otomatis jika ada versi baru dari Firestore)
            val shouldShowBanner = appUpdateInfo?.isUpdateAvailable == true && !isUpdateBannerDismissed
            if (shouldShowBanner) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                        exit = fadeOut()
                    ) {
                        DashboardUpdateBanner(
                            updateInfo = appUpdateInfo!!,
                            onUpdateClick = { viewModel.openUpdateModal() },
                            onDismissClick = { viewModel.dismissUpdateBanner() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // MC DAY MODE BANNER (STRICTLY SHOWN ONLY IF THERE IS A JOB TODAY)
            todayBooking?.let { todayJob ->
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Primary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text("ACARA HARI INI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(todayJob.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("${todayJob.start ?: "19:00"} • ${todayJob.location ?: "Venue"}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }

                            Button(
                                onClick = { onMcDayModeClick(todayJob.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Warning)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MC Day Mode", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Agenda Berikutnya Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Agenda Berikutnya",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        MCJobInfoTooltip(tooltipText = "Daftar job terdekat yang akan berlangsung. Peringatan bentrok otomatis jika ada jadwal terdaftar di jam sama.")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (nextBooking != null) {
                        NextBookingCard(
                            booking = nextBooking,
                            onDetailClick = { onBookingClick(nextBooking.id) }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada agenda terdekat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Catat job acara pertama Anda untuk mulai mengelola jadwal profesional.",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onAddJobClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Text("Catat Job Pertama", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Reminder Hari Ini Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Reminder Hari Ini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            MCJobInfoTooltip(tooltipText = "Notifikasi pengingat otomatis H-1 dan H-0 untuk persiapan brief, dresscode, dan venue acara.")
                        }
                        if (reminders.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${reminders.size} Aktif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (reminders.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Tidak ada pengingat pending. Semua persiapan aman!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reminders.forEach { reminder ->
                                ReminderItemCard(
                                    reminder = reminder,
                                    onDismiss = { viewModel.dismissReminder(reminder.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // White overlay pada area status bar — header menyatu seamlessly ke atas
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(Color.White)
                .align(Alignment.TopStart)
        )

        // Tombol Pintasan Cepat Melayang Dasbor (DEFAULT: Non-Aktif, dapat diaktifkan & dikustomisasi di Pengaturan)
        if (quickActionEnabled) {
            QuickAddFab(
                onAddJob = onAddJobClick,
                onAddClient = onAddClientClick,
                onAddPayment = onAddPaymentClick,
                onAddExpense = onAddExpenseClick,
                onAddReminder = onAddReminderClick,
                onRateCard = onPriceListClick,
                onInvoice = onCreateInvoiceClick,
                onAnalytics = onAnalyticsClick,
                onNotifications = onNotificationClick,
                onProfile = onProfileClick,
                onSettings = onSettingsClick,
                onTodo = onTodoClick,
                showJob = qaAddJobEnabled,
                showClient = qaAddClientEnabled,
                showPayment = qaAddPaymentEnabled,
                showExpense = qaAddExpenseEnabled,
                showReminder = qaReminderEnabled,
                showRateCard = qaRateCardEnabled,
                showInvoice = qaInvoiceEnabled,
                showAnalytics = qaAnalyticsEnabled,
                showNotifications = qaNotificationsEnabled,
                showProfile = qaProfileEnabled,
                showSettings = qaSettingsEnabled,
                showTodo = qaTodoEnabled,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 16.dp)
            )
        }

        // Support & Help Modal Dialog
        if (showSupportDialog) {
            SupportHelpModalDialog(
                onDismiss = { showSupportDialog = false },
                onContactSupport = {
                    showSupportDialog = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.link/rxug92"))
                    context.startActivity(intent)
                }
            )
        }

        // Custom Date Range Picker Bottom Sheet Shortcut
        if (showCustomDateRangePicker) {
            var pickerMonth by remember { mutableStateOf(YearMonth.now()) }
            var tempStart by remember { mutableStateOf<LocalDate?>(customStartDate ?: LocalDate.now()) }
            var tempEnd by remember { mutableStateOf<LocalDate?>(customEndDate ?: customStartDate ?: LocalDate.now()) }

            ModalBottomSheet(
                onDismissRequest = { showCustomDateRangePicker = false },
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
                        text = "Pilih Tanggal atau Rentang Spesifik",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnBackground,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    val rangeInfo = remember(tempStart, tempEnd) {
                        if (tempStart != null && tempEnd != null) {
                            val s = if (tempStart!!.isAfter(tempEnd!!)) tempEnd!! else tempStart!!
                            val e = if (tempStart!!.isAfter(tempEnd!!)) tempStart!! else tempEnd!!
                            if (s == e) "Hari Terpilih: ${Formatter.formatDate(s)}"
                            else "Rentang Terpilih: ${Formatter.formatDate(s)} s/d ${Formatter.formatDate(e)}"
                        } else if (tempStart != null) {
                            "Mulai: ${Formatter.formatDate(tempStart!!)}"
                        } else {
                            "Ketuk tanggal pada kalender di bawah:"
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rangeInfo,
                            fontSize = 12.sp,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    CalendarView(
                        currentMonth = pickerMonth,
                        onMonthChange = { pickerMonth = it },
                        bookings = allBookings,
                        reminders = reminders,
                        onDateClick = { date ->
                            if (tempStart == null || (tempStart != null && tempEnd != null && tempStart != tempEnd)) {
                                tempStart = date
                                tempEnd = date
                            } else if (tempStart != null && tempEnd == tempStart) {
                                if (date.isBefore(tempStart)) {
                                    tempEnd = tempStart
                                    tempStart = date
                                } else {
                                    tempEnd = date
                                }
                            }
                        },
                        selectedDate = tempStart
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (tempStart != null) {
                                val s = tempStart!!
                                val e = tempEnd ?: s
                                viewModel.setCustomDateRange(s, e)
                            }
                            showCustomDateRangePicker = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terapkan Filter Tanggal 🚀", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Error Snackbar
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Tutup", color = Color.White)
                    }
                }
            ) {
                Text(error)
            }
        }

        // Modal Dialog Update (forced / user-triggered)
        if (showUpdateModal) {
            appUpdateInfo?.let { info ->
                AppUpdateModalDialog(
                    updateInfo = info,
                    isDownloading = isDownloadingUpdate,
                    downloadProgress = downloadProgress,
                    errorMessage = updateErrorMessage,
                    onDismiss = {
                        if (!info.isForced) viewModel.closeUpdateModal()
                    },
                    onStartDownload = {
                        viewModel.startDownloadAndInstall(context)
                    }
                )
            }
        }

        // Auto-show modal for FORCED updates (no banner needed)
        LaunchedEffect(appUpdateInfo) {
            val info = appUpdateInfo
            if (info != null && info.isUpdateAvailable && info.isForced) {
                viewModel.openUpdateModal()
            }
        }
    }
}

@Composable
fun HomeHeader(
    mcName: String,
    photoUri: String?,
    unreadNotificationCount: Int,
    isSyncing: Boolean,
    isOnline: Boolean,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onProfileClick)
                ) {
                    // Profile Avatar (Real-time synced from user profile)
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp),
                        border = BorderStroke(1.5.dp, Primary)
                    ) {
                        MCJobAvatarImage(
                            photoUri = photoUri,
                            contentDescription = "Foto Profil $mcName",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy((-5).dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "mcjob.id",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                            } else {
                                Icon(
                                    imageVector = if (!isOnline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                    contentDescription = "Sync Status",
                                    tint = if (!isOnline) Color.Gray else Success,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "powered by career mc academy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 3.dp)
                        ) {
                            Text(
                                text = "Halo, $mcName 👋",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = Primary)
                    }
                    Box {
                        IconButton(onClick = onNotificationClick) {
                            Icon(Icons.Default.Notifications, contentDescription = "Pengingat", tint = Primary)
                        }
                        if (unreadNotificationCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Error,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = unreadNotificationCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
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
fun HeroFinancialSurface(
    summary: FinancialSummary,
    selectedFilter: BookingRepository.TimeFilter,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?,
    onSelectFilter: (BookingRepository.TimeFilter) -> Unit,
    onOpenCustomDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Primary
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Chip Filter Tanggal Melintang Tepat Center di Atas
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.22f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val customLabel = if (selectedFilter == BookingRepository.TimeFilter.CUSTOM_RANGE && customStartDate != null) {
                        val d1 = customStartDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))
                        if (customEndDate != null && customStartDate != customEndDate) {
                            val d2 = customEndDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))
                            "$d1-$d2 📅"
                        } else {
                            "$d1 📅"
                        }
                    } else {
                        "Rentang 📅"
                    }

                    val filterItems = listOf(
                        BookingRepository.TimeFilter.TODAY to "Hari Ini",
                        BookingRepository.TimeFilter.THIS_MONTH to "Bulan Ini",
                        BookingRepository.TimeFilter.THIS_YEAR to "Tahun Ini",
                        BookingRepository.TimeFilter.CUSTOM_RANGE to customLabel
                    )

                    filterItems.forEach { (filter, label) ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (filter == BookingRepository.TimeFilter.CUSTOM_RANGE) {
                                        onOpenCustomDatePicker()
                                    } else {
                                        onSelectFilter(filter)
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color.White else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Primary else Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Baris 1: Total Omset (Judul Kiri, Rp Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Omset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = Formatter.formatCurrency(summary.totalHonor),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Baris 2: Terbayar / DP (Judul Kiri, Rp Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terbayar / DP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = Formatter.formatCurrency(summary.totalPaid),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris 3: Sisa Piutang (Judul Kiri, Rp Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sisa Piutang",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (summary.totalOutstanding > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = Formatter.formatCurrency(summary.totalOutstanding),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Text(
                        text = Formatter.formatCurrency(summary.totalOutstanding),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSurface(
    onAddJob: () -> Unit,
    onAddClient: () -> Unit,
    onCreateInvoice: () -> Unit,
    onAddPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "AKSES CEPAT MC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(4.dp))
                MCJobInfoTooltip(tooltipText = "Tombol pintas untuk mencatat job acara baru, menambah profil klien, cetak Invoice PDF resmi, dan mencatat penerimaan DP.")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionButton(icon = Icons.Default.Event, label = "Catat Job", onClick = onAddJob)
                QuickActionButton(icon = Icons.Default.PersonAdd, label = "Klien", onClick = onAddClient)
                QuickActionButton(icon = Icons.AutoMirrored.Filled.ReceiptLong, label = "Invoice", onClick = onCreateInvoice)
                QuickActionButton(icon = Icons.Default.Payments, label = "Pelunasan", onClick = onAddPayment)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Primary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnBackground)
    }
}

@Composable
fun NextBookingCard(
    booking: Booking,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetailClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left Accent Bar (Gradient Royal Blue)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Primary, Color(0xFF4F46E5))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row: Category Badge (Solid Primary) + Formatted Date Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary
                    ) {
                        Text(
                            text = booking.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Formatter.formatDate(booking.date),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = booking.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = OnBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata Details Row (Jam, Venue, Klien)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.start ?: "19:00 WIB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.location ?: "Venue Utama",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))

                // Footer Row: Fee & Status + Action Pill Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fee Honor", fontSize = 10.sp, color = OnSurfaceVariant)
                        Text(
                            text = Formatter.formatCurrency(booking.fee),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Primary.copy(alpha = 0.08f),
                        modifier = Modifier.clickable(onClick = onDetailClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Lihat Detail",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: Reminder,
    onDismiss: () -> Unit
) {
    val isPayment = reminder.reminderType == "PAYMENT_OVERDUE" || reminder.reminderType == "PAYMENT"
    val accentBrush = if (isPayment) {
        androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Warning, Color(0xFFF59E0B)))
    } else {
        androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Primary, Color(0xFF4F46E5)))
    }

    val badgeTag = when {
        reminder.reminderType == "PAYMENT_OVERDUE" -> "PELUNASAN HONOR"
        reminder.reminderType.startsWith("H-") -> reminder.reminderType.uppercase() + " PERSIAPAN"
        else -> "PENGINGAT ACARA"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left Accent Bar (Gradient Bar)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(brush = accentBrush)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header Row: Category Badge + Dismiss Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPayment) Warning.copy(alpha = 0.12f) else Primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badgeTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPayment) Color(0xFFB45309) else Primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.clickable(onClick = onDismiss)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Pengingat",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title & Message Content
                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPayment) Warning.copy(alpha = 0.12f) else Primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPayment) Icons.Default.Payments else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isPayment) Warning else Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reminder.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = reminder.message,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopQuickFilterChipsSurface(
    onTestimonialClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CTA 1: Testimoni (Ungu Primary)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTestimonialClick() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEEF2FF), // Soft Indigo/Purple Background (Aligned with Primary)
                border = BorderStroke(1.dp, Color(0xFFC7D2FE)) // Soft Indigo Border
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = "Testimoni",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Testimoni & Saran",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }
            }

            // CTA 2: Bantuan Kendala (Ungu Primary)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSupportClick() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEEF2FF), // Soft Indigo/Purple Background (Aligned with Primary)
                border = BorderStroke(1.dp, Color(0xFFC7D2FE)) // Soft Indigo Border
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Bantuan Kendala",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bantuan Kendala",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }
            }
        }
    }
}


@Composable
fun SupportHelpModalDialog(
    onDismiss: () -> Unit,
    onContactSupport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bantuan & Pusat Kendala",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Mengalami kendala saat menggunakan aplikasi mcjob.id? Pilih bantuan di bawah ini:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Tim Support MCJob.id", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Respon cepat untuk bantuan kendala teknis & akun.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        },
        confirmButton = {
            // CTA: Hubungi Support via WhatsApp
            Button(
                onClick = onContactSupport,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366) // WhatsApp green
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hubungi Support", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Mengerti", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    )
}
