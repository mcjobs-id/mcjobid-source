package com.isankamil.mcjobid.ui.screen.notification

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isankamil.mcjobid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSimulationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasSimulations by viewModel.hasSimulatedReminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laboratorium Simulasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Pengujian Notifikasi & System Alert",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (hasSimulations) {
                        TextButton(onClick = {
                            viewModel.clearSimulations()
                            Toast.makeText(context, "Data notifikasi simulasi telah dibersihkan", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bersihkan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Engine Status Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Laboratorium Pengujian Terpisah", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Primary)
                            Text(
                                "Modul pengujian notifikasi ini terisolasi dari notifikasi asli. Data simulasi berawalan 'sim_' dan tidak mempengaruhi agenda atau finansial riil Anda.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Card 1: Simulasi Notifikasi H-1 Acara
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Primary.copy(alpha = 0.12f)) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Simulasi Pengingat H-1 Acara", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("Uji notifikasi persiapan H-1 gladi bersih & cek lokasi acara MC.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.createSimulatedReminder(
                                    reminderType = "H-1",
                                    title = "H-1 Persiapan MC Wedding Gala",
                                    message = "Event besok jam 08:00 WIB di Hotel Kempinski! Persiapkan jas/wardrobe & file rundown.",
                                    context = context
                                )
                                Toast.makeText(context, "Simulasi Notifikasi H-1 Berhasil Dikirim!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim Simulasi Notifikasi H-1", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            // Card 2: Simulasi Notifikasi Pelunasan Piutang Overdue
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Warning.copy(alpha = 0.12f)) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Simulasi Tagihan Piutang Overdue", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("Uji notifikasi pengingat penagihan pelunasan honor yang belum dibayar.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.createSimulatedReminder(
                                    reminderType = "PAYMENT_OVERDUE",
                                    title = "Tagihan Pelunasan Honor Belum Lunas",
                                    message = "Sisa piutang Rp3.000.000 untuk Event Gala Dinner belum dilunasi oleh Klien PT Djarum.",
                                    context = context
                                )
                                Toast.makeText(context, "Simulasi Tagihan Piutang Berhasil Dikirim!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Warning),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OnBackground, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim Simulasi Tagihan Piutang", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = OnBackground)
                        }
                    }
                }
            }

            // Card 3: Simulasi Konfirmasi DP Booking
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Success.copy(alpha = 0.12f)) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Simulasi Konfirmasi DP Booking", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("Uji notifikasi pengingat konfirmasi pembayaran DP untuk mengunci tanggal.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.createSimulatedReminder(
                                    reminderType = "PAYMENT",
                                    title = "Pengingat Konfirmasi DP Klien",
                                    message = "DP sebesar 30% (Rp1.500.000) untuk Acara Birthday Bash belum dikonfirmasi.",
                                    context = context
                                )
                                Toast.makeText(context, "Simulasi Konfirmasi DP Berhasil Dikirim!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim Simulasi Konfirmasi DP", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            // Action Button: Clear Simulations
            if (hasSimulations) {
                item {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearSimulations()
                            Toast.makeText(context, "Notifikasi simulasi berhasil dibersihkan!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bersihkan Notifikasi Simulasi", fontWeight = FontWeight.Bold, color = Error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
