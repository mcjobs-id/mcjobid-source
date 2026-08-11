package com.isankamil.mcjobid.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
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

    val allStates = listOf(
        qaAddJobEnabled, qaAddClientEnabled, qaAddPaymentEnabled, qaAddExpenseEnabled,
        qaReminderEnabled, qaRateCardEnabled, qaInvoiceEnabled,
        qaAnalyticsEnabled, qaNotificationsEnabled, qaProfileEnabled, qaSettingsEnabled,
        qaTodoEnabled
    )
    val activeCount = allStates.count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pintasan Cepat Dasbor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kustomisasi Tombol & Hak Akses Fitur",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card Penjelasan & Master Switch
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tombol Pintasan Melayang",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                                Text(
                                    text = "Akses instan 1-ketukan di layar dasbor utama",
                                    fontSize = 11.5.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Saat diaktifkan, sebuah tombol pintasan mengambang (+) akan muncul di sudut kanan bawah dasbor. Anda dapat memilih fitur apa saja yang ingin ditampilkan di dalamnya.",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(color = SurfaceVariant)

                        // Master Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aktifkan Tombol di Dasbor",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quickActionEnabled) Primary else OnSurface
                                )
                                Text(
                                    text = if (quickActionEnabled) "Tombol pintasan aktif & tampil di dasbor" else "Tombol pintasan disembunyikan (Non-aktif)",
                                    fontSize = 11.sp,
                                    color = if (quickActionEnabled) Primary else OnSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = quickActionEnabled,
                                onCheckedChange = { viewModel.setQuickActionEnabled(it) },
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
                    }
                }
            }

            // Bagian Kustomisasi Item Fitur
            if (quickActionEnabled) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Primary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "$activeCount dari ${allStates.size} Fitur Terpilih",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { viewModel.selectAllQuickActions(true) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Pilih Semua", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Primary)
                            }
                            TextButton(
                                onClick = { viewModel.selectAllQuickActions(false) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Hapus Semua", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            }
                        }
                    }
                }

                // 1. Kategori: Agenda & Klien
                item {
                    CategoryHeader(title = "AGENDA, KLIEN & PENGINGAT")
                }
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            QuickActionRow(
                                icon = Icons.Default.Event,
                                iconBg = Color(0xFF4F46E5),
                                title = "Catat Job Baru",
                                subtitle = "Formulir pencatatan agenda & detail acara MC baru",
                                isChecked = qaAddJobEnabled,
                                onCheckedChange = { viewModel.setQaAddJobEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.PersonAdd,
                                iconBg = Color(0xFF0284C7),
                                title = "Tambah Klien Baru",
                                subtitle = "Pendaftaran profil calon pengantin atau vendor EO/WO",
                                isChecked = qaAddClientEnabled,
                                onCheckedChange = { viewModel.setQaAddClientEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.Alarm,
                                iconBg = Color(0xFFD97706),
                                title = "Buat Pengingat Acara",
                                subtitle = "Pengingat otomatis jadwal persiapan, busana, & konfirmasi",
                                isChecked = qaReminderEnabled,
                                onCheckedChange = { viewModel.setQaReminderEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.Checklist,
                                iconBg = Color(0xFF0D9488),
                                title = "Daftar Tugas & To-Do MC",
                                subtitle = "Checklist persiapan perform, gladi resik, & karier",
                                isChecked = qaTodoEnabled,
                                onCheckedChange = { viewModel.setQaTodoEnabled(it) }
                            )
                        }
                    }
                }

                // 2. Kategori: Keuangan & Dokumen Bisnis
                item {
                    CategoryHeader(title = "KEUANGAN & DOKUMEN BISNIS")
                }
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            QuickActionRow(
                                icon = Icons.Default.Payments,
                                iconBg = Color(0xFF059669),
                                title = "Catat Pelunasan / DP",
                                subtitle = "Input pencatatan tanda jadi atau pelunasan honor MC",
                                isChecked = qaAddPaymentEnabled,
                                onCheckedChange = { viewModel.setQaAddPaymentEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.ReceiptLong,
                                iconBg = Color(0xFFDC2626),
                                title = "Catat Pengeluaran Operasional",
                                subtitle = "Pencatatan nota bensin, busana, asisten, & biaya tampil",
                                isChecked = qaAddExpenseEnabled,
                                onCheckedChange = { viewModel.setQaAddExpenseEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                iconBg = Color(0xFF7C3AED),
                                title = "Generator Invoice & Kwitansi",
                                subtitle = "Pembuatan dan pembagian surat tagihan PDF profesional",
                                isChecked = qaInvoiceEnabled,
                                onCheckedChange = { viewModel.setQaInvoiceEnabled(it) }
                            )
                        }
                    }
                }

                // 3. Kategori: Rate Card & Analisis Performa
                item {
                    CategoryHeader(title = "RATE CARD & ANALISIS")
                }
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            QuickActionRow(
                                icon = Icons.Default.Sell,
                                iconBg = Color(0xFFDB2777),
                                title = "Simulasi Rate Card & Paket",
                                subtitle = "Katalog paket harga MC & bagikan rincian ke calon klien",
                                isChecked = qaRateCardEnabled,
                                onCheckedChange = { viewModel.setQaRateCardEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.Analytics,
                                iconBg = Color(0xFF65A30D),
                                title = "Analisis Omset & Statistik",
                                subtitle = "Grafik pertumbuhan omset, performa job, & sisa piutang",
                                isChecked = qaAnalyticsEnabled,
                                onCheckedChange = { viewModel.setQaAnalyticsEnabled(it) }
                            )
                        }
                    }
                }

                // 4. Kategori: Notifikasi & Pengaturan
                item {
                    CategoryHeader(title = "MANAJEMEN AKUN & PENGATURAN")
                }
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            QuickActionRow(
                                icon = Icons.Default.NotificationsActive,
                                iconBg = Color(0xFFF59E0B),
                                title = "Pusat Pengingat & Notifikasi",
                                subtitle = "Pantau seluruh antrean jadwal notifikasi agenda MC",
                                isChecked = qaNotificationsEnabled,
                                onCheckedChange = { viewModel.setQaNotificationsEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.AccountCircle,
                                iconBg = Color(0xFF6366F1),
                                title = "Edit Profil MC",
                                subtitle = "Perbarui foto, nama panggung, spesialisasi, & kontak",
                                isChecked = qaProfileEnabled,
                                onCheckedChange = { viewModel.setQaProfileEnabled(it) }
                            )
                            HorizontalDivider(color = SurfaceVariant)
                            QuickActionRow(
                                icon = Icons.Default.Settings,
                                iconBg = Color(0xFF475569),
                                title = "Pengaturan Aplikasi",
                                subtitle = "Konfigurasi keamanan PIN, tema tampilan, & cloud sync",
                                isChecked = qaSettingsEnabled,
                                onCheckedChange = { viewModel.setQaSettingsEnabled(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconBg.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) OnSurface else OnSurfaceVariant
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = OnSurfaceVariant,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = Primary,
                checkmarkColor = Color.White
            )
        )
    }
}
