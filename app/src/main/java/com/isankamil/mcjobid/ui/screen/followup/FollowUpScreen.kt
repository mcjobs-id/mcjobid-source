package com.isankamil.mcjobid.ui.screen.followup

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.isankamil.mcjobid.ui.components.EmptyStateView
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpScreen(
    viewModel: FollowUpViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onBookingClick: (String) -> Unit,
    onCreateJob: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Pembayaran") }
    val unpaidBookings by viewModel.unpaidBookings.collectAsState()
    val upcomingBookings by viewModel.upcomingBookings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow Up Center", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Pembayaran", "Konfirmasi Event").forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

            Spacer(modifier = Modifier.height(8.dp))

            val displayList = if (selectedCategory == "Pembayaran") unpaidBookings else upcomingBookings

            if (displayList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "Semua Follow Up Aman",
                    description = "Tidak ada tagihan atau konfirmasi acara yang perlu difollow up saat ini.",
                    actionText = "Catat Job Baru",
                    onActionClick = onCreateJob
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayList) { item ->
                        val isPayment = selectedCategory == "Pembayaran"
                        val accentBrush = if (isPayment) {
                            androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Error, Color(0xFFEF4444)))
                        } else {
                            androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Primary, Color(0xFF4F46E5)))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookingClick(item.id) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                // Left Accent Vertical Gradient Bar
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
                                    // Header Row: Category Tag Pill + Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isPayment) Error.copy(alpha = 0.12f) else Primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (isPayment) "FOLLOW UP PELUNASAN" else "KONFIRMASI ACARA",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isPayment) Error else Primary,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            ) {
                                                Icon(Icons.Default.Event, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = Formatter.formatDate(item.date),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF334155)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Event Name & Outstanding Callout Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = OnBackground
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "Klien: ${item.client ?: "Personal"}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = OnSurfaceVariant
                                            )
                                            item.pic?.let {
                                                Text(
                                                    text = "PIC / WO: $it",
                                                    fontSize = 12.sp,
                                                    color = OnSurfaceVariant
                                                )
                                            }
                                        }

                                        if (isPayment && item.outstanding > 0) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Error.copy(alpha = 0.08f),
                                                border = BorderStroke(1.dp, Error.copy(alpha = 0.2f))
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                                ) {
                                                    Text("SISA PIUTANG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Error)
                                                    Text(Formatter.formatCurrency(item.outstanding), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Error)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Follow Up Action Button
                                    Button(
                                        onClick = {
                                            val picPhone = item.pic ?: ""
                                            val normalizedPhone = Formatter.formatWhatsAppNumber(picPhone)
                                            val waMessage = if (isPayment) {
                                                "Halo Kak, salam dari MC. Izin follow up terkait pembayaran untuk acara ${item.name}. Saat ini masih terdapat sisa tagihan honor sebesar ${Formatter.formatCurrency(item.outstanding)}. Terima kasih banyak 🙏"
                                            } else {
                                                "Halo Kak, izin konfirmasi persiapan akhir untuk acara ${item.name} pada tanggal ${Formatter.formatDate(item.date)}. Terima kasih."
                                            }

                                            val encodedMsg = Uri.encode(waMessage)
                                            val url = if (normalizedPhone.isNotBlank()) "https://wa.me/$normalizedPhone?text=$encodedMsg" else "https://wa.me/?text=$encodedMsg"
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Handle case where browser/WhatsApp is not available
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Follow Up Chat WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
