package com.isankamil.mcjobid.ui.screen.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isankamil.mcjobid.domain.model.Reminder
import com.isankamil.mcjobid.ui.components.EmptyStateView
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCreateJob: () -> Unit = {},
    onOpenSimulation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val reminders by viewModel.reminders.collectAsState()

    // Strictly filter real notifications only (excluding all simulation items)
    val filteredReminders = remember(reminders, selectedCategory) {
        val realList = reminders.filterNot { it.id.startsWith("sim_") || it.bookingId == "sim_booking" }
        if (selectedCategory == "Semua") realList
        else realList.filter {
            when (selectedCategory) {
                "Acara" -> it.reminderType.startsWith("H-") || it.reminderType == "EVENT"
                "Pelunasan" -> it.reminderType == "PAYMENT_OVERDUE" || it.reminderType == "PAYMENT"
                else -> true
            }
        }
    }

    val realReminders = remember(reminders) {
        reminders.filterNot { it.id.startsWith("sim_") || it.bookingId == "sim_booking" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pusat Notifikasi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (realReminders.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                realReminders.forEach { viewModel.dismissReminder(it.id) }
                            }
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Tandai Semua Selesai", tint = Primary)
                        }
                    }
                    IconButton(onClick = onOpenSimulation) {
                        Icon(Icons.Default.Science, contentDescription = "Laboratorium Simulasi", tint = Secondary)
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
            // Category Filter Chips
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Acara", "Pelunasan").forEach { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF64748B)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = Primary,
                                borderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredReminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.NotificationsNone,
                        title = "Tidak Ada Notifikasi",
                        description = "Semua persiapan acara dan pengingat pelunasan pembayaran Anda aman dan terkendali."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReminders) { item ->
                        NotificationCardItem(
                            reminder = item,
                            onDismiss = { viewModel.dismissReminder(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCardItem(
    reminder: Reminder,
    onDismiss: () -> Unit
) {
    val isPayment = reminder.reminderType == "PAYMENT_OVERDUE" || reminder.reminderType == "PAYMENT"
    val (iconBg, iconRes, iconColor) = if (isPayment) {
        Triple(Warning.copy(alpha = 0.12f), Icons.Default.Payments, Warning)
    } else {
        Triple(Primary.copy(alpha = 0.12f), Icons.Default.Event, Primary)
    }

    val badgeTag = when {
        reminder.reminderType == "PAYMENT_OVERDUE" -> "PELUNASAN HONOR"
        reminder.reminderType.startsWith("H-") -> reminder.reminderType.uppercase() + " PERSIAPAN"
        else -> "PENGINGAT ACARA"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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

                val formattedDateText = remember(reminder.targetDate) {
                    try {
                        val parsed = java.time.LocalDate.parse(reminder.targetDate)
                        Formatter.formatDateShort(parsed)
                    } catch (_: Exception) {
                        reminder.targetDate
                    }
                }

                Text(
                    text = formattedDateText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = CircleShape,
                    color = iconBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(iconRes, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = reminder.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Tandai Selesai",
                        tint = Success,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
