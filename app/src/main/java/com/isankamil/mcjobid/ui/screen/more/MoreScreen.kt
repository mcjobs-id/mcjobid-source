package com.isankamil.mcjobid.ui.screen.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.ui.components.feedback.MCJobDestructiveDialog
import com.isankamil.mcjobid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onProfileClick: () -> Unit,
    onPriceListClick: () -> Unit,
    onInvoiceClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onFollowUpClick: () -> Unit,
    onTodoClick: () -> Unit = {},
    onQuickActionSettingsClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    userProfile: UserProfile? = null,
    isSynced: Boolean = false,
    isSyncing: Boolean = false
) {
    var showLogoutDialog by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lainnya & Hub Bisnis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
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
            // Profile Card Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onProfileClick),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            com.isankamil.mcjobid.ui.components.MCJobAvatarImage(
                                photoUri = userProfile?.photoUri,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize(),
                                fallbackTint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile?.name ?: "MC Professional",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${userProfile?.specialization ?: "Wedding & Corporate"} · ${userProfile?.city ?: "Jakarta"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lihat & Edit Profil MC 🚀",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Semua menu dalam satu Card — Rate Card, Analisis, & Follow Up
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        MoreMenuItem(
                            icon = Icons.Default.Sell,
                            title = "Rate Card & Price List",
                            subtitle = "Kelola paket harga MC, bagikan ke klien & buat job instan",
                            onClick = onPriceListClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.Default.Analytics,
                            title = "Analisis Performa",
                            subtitle = "Pantau omset, pengeluaran, dan laba bersih",
                            onClick = onAnalyticsClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.Default.ConnectWithoutContact,
                            title = "Pusat Follow Up",
                            subtitle = "Konfirmasi agenda dan penagihan piutang",
                            onClick = onFollowUpClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.Default.Checklist,
                            title = "Daftar Tugas & To-Do MC",
                            subtitle = "Checklist persiapan perform, gladi resik, & karier",
                            onClick = onTodoClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.Default.Bolt,
                            title = "Pintasan Cepat Dasbor",
                            subtitle = "Kustomisasi tombol melayang & hak akses fitur cepat",
                            onClick = onQuickActionSettingsClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "Generator Invoice",
                            subtitle = "Buat dan bagikan invoice PDF profesional",
                            onClick = onInvoiceClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "Pusat Pengingat",
                            subtitle = "Notifikasi otomatis agenda dan pelunasan",
                            onClick = onNotificationClick
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        MoreMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Pengaturan",
                            subtitle = "Notifikasi, dan preferensi aplikasi",
                            onClick = onSettingsClick
                        )
                        // Divider tebal sebagai pemisah visual danger zone
                        HorizontalDivider(thickness = 6.dp, color = Background)
                        MoreMenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = "Keluar Akun",
                            subtitle = "Selesaikan sesi akses mcjob.id",
                            titleColor = Error,
                            onClick = { showLogoutDialog = true }
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            MCJobDestructiveDialog(
                title = "Konfirmasi Keluar Sesi",
                description = "Apakah Anda yakin ingin keluar dari akun MCJOB.id? Data Anda tetap aman ter-sinkronisasi di cloud server.",
                primaryCtaText = "Ya, Keluar",
                secondaryCtaText = "Batal",
                onConfirm = {
                    showLogoutDialog = false
                    onLogoutClick()
                },
                onDismiss = {
                    showLogoutDialog = false
                }
            )
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = OnBackground,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (titleColor == Error) Error.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (titleColor == Error) Error else Primary, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
    }
}
