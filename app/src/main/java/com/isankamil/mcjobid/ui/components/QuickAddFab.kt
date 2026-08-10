package com.isankamil.mcjobid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.isankamil.mcjobid.ui.theme.Primary

@Composable
fun QuickAddFab(
    onAddJob: () -> Unit = {},
    onAddClient: () -> Unit = {},
    onAddPayment: () -> Unit = {},
    onAddExpense: () -> Unit = {},
    onAddReminder: () -> Unit = {},
    onRateCard: () -> Unit = {},
    onExpenseSimulator: () -> Unit = {},
    onInvoice: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    onTodo: () -> Unit = {},
    showJob: Boolean = true,
    showClient: Boolean = true,
    showPayment: Boolean = true,
    showExpense: Boolean = true,
    showReminder: Boolean = true,
    showRateCard: Boolean = true,
    showExpenseSimulator: Boolean = true,
    showInvoice: Boolean = true,
    showAnalytics: Boolean = true,
    showNotifications: Boolean = true,
    showProfile: Boolean = true,
    showSettings: Boolean = true,
    showTodo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hasAnyItem = showJob || showClient || showPayment || showExpense ||
            showReminder || showRateCard || showExpenseSimulator ||
            showInvoice || showAnalytics || showNotifications ||
            showProfile || showSettings || showTodo

    if (!hasAnyItem) return

    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(end = 4.dp, bottom = 4.dp)
            ) {
                if (showSettings) {
                    SpeedDialItem(
                        icon = Icons.Default.Settings,
                        label = "Pengaturan",
                        iconColor = Color(0xFF475569)
                    ) {
                        expanded = false
                        onSettings()
                    }
                }
                if (showProfile) {
                    SpeedDialItem(
                        icon = Icons.Default.AccountCircle,
                        label = "Profil MC",
                        iconColor = Color(0xFF6366F1)
                    ) {
                        expanded = false
                        onProfile()
                    }
                }
                if (showNotifications) {
                    SpeedDialItem(
                        icon = Icons.Default.NotificationsActive,
                        label = "Pusat Pengingat",
                        iconColor = Color(0xFFF59E0B)
                    ) {
                        expanded = false
                        onNotifications()
                    }
                }
                if (showAnalytics) {
                    SpeedDialItem(
                        icon = Icons.Default.Analytics,
                        label = "Analisis Omset",
                        iconColor = Color(0xFF65A30D)
                    ) {
                        expanded = false
                        onAnalytics()
                    }
                }
                if (showExpenseSimulator) {
                    SpeedDialItem(
                        icon = Icons.Default.Calculate,
                        label = "Simulasi Profit",
                        iconColor = Color(0xFF0891B2)
                    ) {
                        expanded = false
                        onExpenseSimulator()
                    }
                }
                if (showRateCard) {
                    SpeedDialItem(
                        icon = Icons.Default.Sell,
                        label = "Rate Card & Paket",
                        iconColor = Color(0xFFDB2777)
                    ) {
                        expanded = false
                        onRateCard()
                    }
                }
                if (showInvoice) {
                    SpeedDialItem(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "Generator Invoice",
                        iconColor = Color(0xFF7C3AED)
                    ) {
                        expanded = false
                        onInvoice()
                    }
                }
                if (showExpense) {
                    SpeedDialItem(
                        icon = Icons.Default.ReceiptLong,
                        label = "Catat Pengeluaran",
                        iconColor = Color(0xFFDC2626)
                    ) {
                        expanded = false
                        onAddExpense()
                    }
                }
                if (showPayment) {
                    SpeedDialItem(
                        icon = Icons.Default.Payments,
                        label = "Catat Pelunasan",
                        iconColor = Color(0xFF059669)
                    ) {
                        expanded = false
                        onAddPayment()
                    }
                }
                if (showTodo) {
                    SpeedDialItem(
                        icon = Icons.Default.Checklist,
                        label = "Tugas & To-Do MC",
                        iconColor = Color(0xFF0D9488)
                    ) {
                        expanded = false
                        onTodo()
                    }
                }
                if (showReminder) {
                    SpeedDialItem(
                        icon = Icons.Default.Alarm,
                        label = "Buat Pengingat",
                        iconColor = Color(0xFFD97706)
                    ) {
                        expanded = false
                        onAddReminder()
                    }
                }
                if (showClient) {
                    SpeedDialItem(
                        icon = Icons.Default.PersonAdd,
                        label = "Tambah Klien",
                        iconColor = Color(0xFF0284C7)
                    ) {
                        expanded = false
                        onAddClient()
                    }
                }
                if (showJob) {
                    SpeedDialItem(
                        icon = Icons.Default.Event,
                        label = "Catat Job Baru",
                        iconColor = Color(0xFF4F46E5)
                    ) {
                        expanded = false
                        onAddJob()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            containerColor = Primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Pintasan Cepat"
            )
        }
    }
}

@Composable
fun SpeedDialItem(
    icon: ImageVector,
    label: String,
    iconColor: Color = Primary,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = Color.White,
            contentColor = iconColor,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
        }
    }
}
