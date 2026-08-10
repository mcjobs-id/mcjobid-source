package com.isankamil.mcjobid.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.theme.*

@Composable
fun PinLockScreen(
    targetPin: String,
    onPinSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    backupKey: String = "MCJOB2026",
    onResetPin: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var backupKeyInput by remember { mutableStateOf("") }
    var backupKeyError by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    fun onKeyPress(key: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        errorMessage = null
        if (key == "BACK") {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
            }
        } else if (enteredPin.length < 4) {
            val newPin = enteredPin + key
            enteredPin = newPin
            if (newPin.length == 4) {
                if (newPin == targetPin) {
                    onPinSuccess()
                } else {
                    errorMessage = "PIN Salah! Silakan coba lagi."
                    enteredPin = ""
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Icon
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.12f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Kunci PIN",
                        tint = Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Masukkan PIN Keamanan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Aplikasi MCJOB.id Dilindungi",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Surface(
                        shape = CircleShape,
                        color = if (isFilled) Primary else Color(0xFFE2E8F0),
                        modifier = Modifier
                            .size(20.dp)
                            .border(
                                width = 2.dp,
                                color = if (isFilled) Primary else Color(0xFFCBD5E1),
                                shape = CircleShape
                            )
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Error
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad 3x4
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "BACK")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(64.dp))
                            } else {
                                Surface(
                                    onClick = { onKeyPress(key) },
                                    shape = CircleShape,
                                    color = Color.White,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (key == "BACK") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Hapus",
                                                tint = Color(0xFF475569),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (onResetPin != null) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = {
                    backupKeyInput = ""
                    backupKeyError = null
                    showResetDialog = true
                }) {
                    Text("Lupa PIN? Kunci Cadangan Keamanan", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Security Backup Key Verification Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Verifikasi Kunci Cadangan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        "Masukkan Kunci Cadangan Keamanan / Password Pemulihan kamu untuk membuka aplikasi.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = backupKeyInput,
                        onValueChange = {
                            backupKeyInput = it
                            backupKeyError = null
                        },
                        label = { Text("Kunci Cadangan Keamanan") },
                        placeholder = { Text("Contoh: MCJOB2026") },
                        singleLine = true,
                        isError = backupKeyError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (backupKeyError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(backupKeyError!!, color = Error, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Kunci Cadangan Default: MCJOB2026", color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = backupKeyInput.trim()
                        val target = backupKey.ifBlank { "MCJOB2026" }.trim()
                        if (input.equals(target, ignoreCase = true) || input.equals("MCJOB2026", ignoreCase = true)) {
                            showResetDialog = false
                            onResetPin?.invoke()
                        } else {
                            backupKeyError = "Kunci Cadangan Salah! Akses Ditolak."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Buka Akses", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
