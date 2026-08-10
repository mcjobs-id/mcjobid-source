package com.isankamil.mcjobid.ui.components.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

/**
 * Base Container Dialog for MCJOBID Global UI Feedback System.
 * Follows exact Design System:
 * - Rounded 24dp
 * - Background #F8FAFC / White surface
 * - Primary #4F46E5, Text #111827, Secondary Text #64748B
 * - Minimal shadow & clean whitespace
 * - No emoji icons (Material icons only)
 * - Hierarchy: ICON -> TITLE -> DESCRIPTION -> OPTIONAL CONTENT -> PRIMARY CTA -> SECONDARY CTA
 */
@Composable
fun MCJobBaseDialog(
    icon: ImageVector,
    iconTint: Color = Primary,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    optionalContent: @Composable (ColumnScope.() -> Unit)? = null,
    primaryButton: @Composable (() -> Unit)? = null,
    secondaryButton: @Composable (() -> Unit)? = null,
    draftButton: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ICON CONTAINER
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.1f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // TITLE & DESCRIPTION
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }

                // OPTIONAL CONTENT
                if (optionalContent != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = optionalContent
                    )
                }

                // ACTION BUTTONS (Max 2 Main CTAs)
                if (primaryButton != null || secondaryButton != null || draftButton != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        primaryButton?.invoke()
                        secondaryButton?.invoke()
                        draftButton?.invoke()
                    }
                }
            }
        }
    }
}

// 1. CONFIRM DIALOG
@Composable
fun MCJobConfirmDialog(
    title: String,
    description: String,
    primaryCtaText: String = "Konfirmasi",
    secondaryCtaText: String = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    MCJobBaseDialog(
        icon = Icons.AutoMirrored.Filled.HelpOutline,
        iconTint = Primary,
        title = title,
        description = description,
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobPrimaryButton(
                text = primaryCtaText,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = secondaryCtaText,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 2. WARNING DIALOG
@Composable
fun MCJobWarningDialog(
    title: String,
    description: String,
    primaryCtaText: String = "Mengerti",
    secondaryCtaText: String? = null,
    onPrimary: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    MCJobBaseDialog(
        icon = Icons.Default.WarningAmber,
        iconTint = Warning,
        title = title,
        description = description,
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobPrimaryButton(
                text = primaryCtaText,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPrimary()
                    onDismiss()
                },
                containerColor = Warning,
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = secondaryCtaText?.let {
            {
                MCJobSecondaryButton(
                    text = it,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// 3. ERROR DIALOG WITH RETRY CALLBACK
@Composable
fun MCJobErrorDialog(
    title: String = "Belum berhasil disimpan",
    description: String = "Data belum tersimpan. Periksa koneksi atau coba lagi.",
    primaryCtaText: String = "Coba Lagi",
    secondaryCtaText: String = "Batal",
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    MCJobBaseDialog(
        icon = Icons.Default.ErrorOutline,
        iconTint = Error,
        title = title,
        description = description,
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobPrimaryButton(
                text = primaryCtaText,
                onClick = {
                    onDismiss()
                    onRetry()
                },
                containerColor = Error,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = secondaryCtaText,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 4. SUCCESS DIALOG
@Composable
fun MCJobSuccessDialog(
    title: String,
    description: String,
    primaryCtaText: String = "Lihat Detail",
    secondaryCtaText: String? = "Selesai",
    onPrimary: () -> Unit = {},
    onSecondary: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    MCJobBaseDialog(
        icon = Icons.Default.CheckCircleOutline,
        iconTint = Success, // Tetap gunakan hijau untuk ikon agar tetap intuitif sebagai status "Berhasil"
        title = title,
        description = description,
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobPrimaryButton(
                text = primaryCtaText,
                onClick = {
                    onPrimary()
                    onDismiss()
                },
                containerColor = Primary, // DIUBAH: Dari Success (Hijau) ke Primary (Indigo)
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = secondaryCtaText?.let {
            {
                MCJobSecondaryButton(
                    text = it,
                    onClick = {
                        onSecondary()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// 5. INFO DIALOG
@Composable
fun MCJobInfoDialog(
    title: String,
    description: String,
    primaryCtaText: String = "Tutup",
    onDismiss: () -> Unit
) {
    MCJobBaseDialog(
        icon = Icons.Default.Info,
        iconTint = Info,
        title = title,
        description = description,
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobPrimaryButton(
                text = primaryCtaText,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 6. DESTRUCTIVE DIALOG
@Composable
fun MCJobDestructiveDialog(
    title: String,
    description: String,
    primaryCtaText: String, // E.g. "Hapus Job", "Batalkan Job"
    secondaryCtaText: String = "Batal",
    icon: ImageVector = if (title.contains("Keluar", ignoreCase = true) || title.contains("Logout", ignoreCase = true)) Icons.AutoMirrored.Filled.Logout else Icons.Default.DeleteForever,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MCJobBaseDialog(
        icon = icon,
        iconTint = Error,
        title = title,
        description = description,
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobDestructiveButton(
                text = primaryCtaText,
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = secondaryCtaText,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 7. MODAL LOADING DIALOG WITH DOUBLE SUBMISSION PROTECTION
@Composable
fun MCJobLoadingDialog(
    message: String = "Sedang menyimpan...",
    onDismissRequest: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}

// 8. SCHEDULE CONFLICT DIALOG
@Composable
fun MCJobConflictDialog(
    conflictingBookings: List<Booking>,
    onOverrideSave: () -> Unit,
    onDismiss: () -> Unit
) {
    MCJobBaseDialog(
        icon = Icons.Default.EventBusy,
        iconTint = Warning,
        title = "Jadwal Bentrok",
        description = "MCJOBID menemukan job lain pada waktu yang sama.",
        onDismissRequest = onDismiss,
        optionalContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conflictingBookings.forEach { booking ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Warning.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = booking.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Tanggal: ${Formatter.formatDate(booking.date)} • Jam: ${booking.start ?: "Seharian"} - ${booking.end ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Lokasi: ${booking.location ?: "Belum diisi"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        },
        primaryButton = {
            MCJobPrimaryButton(
                text = "Tetap Simpan",
                onClick = {
                    onOverrideSave()
                    onDismiss()
                },
                containerColor = Warning,
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = "Kembali & Ubah Jadwal",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 9. PAYMENT CONFIRMATION DIALOG
@Composable
fun MCJobPaymentConfirmationDialog(
    bookingTitle: String,
    amount: Long,
    paymentMethod: String,
    paymentDate: String,
    totalFee: Long,
    currentPaid: Long,
    remainingAfterPayment: Long,
    isOverpayment: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    MCJobBaseDialog(
        icon = if (isOverpayment) Icons.Default.Warning else Icons.Default.Payments,
        iconTint = if (isOverpayment) Warning else Primary,
        title = if (isOverpayment) "Nominal Melebihi Piutang" else "Konfirmasi Pembayaran",
        description = if (isOverpayment)
            "Nominal pembayaran (${Formatter.formatCurrency(amount)}) melebihi sisa piutang (${Formatter.formatCurrency(totalFee - currentPaid)})."
        else
            "Pastikan rincian pembayaran berikut sudah benar sebelum disimpan.",
        onDismissRequest = onDismiss,
        optionalContent = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Job Acara
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Job Acara:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = bookingTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Nominal Bayar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nominal Bayar:", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = Formatter.formatCurrency(amount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary
                        )
                    }

                    // Metode & Tanggal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Metode & Tanggal:", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = "$paymentMethod • $paymentDate",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Sisa Piutang Setelah Bayar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sisa Piutang Setelah Bayar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(
                            text = Formatter.formatCurrency(maxOf(0L, remainingAfterPayment)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (remainingAfterPayment <= 0) Success else Warning
                        )
                    }
                }
            }
        },
        primaryButton = {
            MCJobPrimaryButton(
                text = if (isOverpayment) "Tetap Catat Pembayaran" else "Konfirmasi Pembayaran",
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                    onDismiss()
                },
                containerColor = if (isOverpayment) Warning else Primary,
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = "Periksa Lagi",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 10. UNSAVED CHANGES DIALOG
@Composable
fun MCJobUnsavedChangesDialog(
    title: String = "Perubahan belum disimpan.",
    description: String = "Data yang sudah kamu masukkan akan hilang jika keluar sekarang.",
    primaryCtaText: String = "Tetap Edit",
    secondaryCtaText: String = "Keluar",
    draftCtaText: String? = null,
    onStayEdit: () -> Unit,
    onExit: () -> Unit,
    onSaveDraft: (() -> Unit)? = null
) {
    MCJobBaseDialog(
        icon = Icons.Default.EditOff,
        iconTint = Warning,
        title = title,
        description = description,
        onDismissRequest = onStayEdit,
        primaryButton = {
            MCJobPrimaryButton(
                text = primaryCtaText,
                onClick = onStayEdit,
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = secondaryCtaText,
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            )
        },
        draftButton = draftCtaText?.let {
            {
                TextButton(
                    onClick = { onSaveDraft?.invoke() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, fontWeight = FontWeight.Bold, color = Primary)
                }
            }
        }
    )
}

// 11. DELETE CLIENT DIALOG (Checking linked job history)
@Composable
fun MCJobDeleteClientDialog(
    clientName: String,
    jobCount: Int,
    onArchive: () -> Unit,
    onViewHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    MCJobBaseDialog(
        icon = Icons.Default.FolderSpecial,
        iconTint = Warning,
        title = "Klien Memiliki Riwayat Job",
        description = "Klien '$clientName' memiliki $jobCount job terdaftar. Menghapus klien ini tidak boleh menghapus riwayat transaksi tanpa konfirmasi.",
        onDismissRequest = onDismiss,
        primaryButton = {
            MCJobPrimaryButton(
                text = "Arsipkan Klien",
                onClick = {
                    onArchive()
                    onDismiss()
                },
                containerColor = Primary,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Archive
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = "Lihat Riwayat Job",
                onClick = {
                    onViewHistory()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        draftButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal", color = Color(0xFF64748B))
            }
        }
    )
}

// 12. MC DAY MODE CONFIRMATION DIALOG
@Composable
fun MCJobMCDayModeDialog(
    eventName: String,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    MCJobBaseDialog(
        icon = Icons.Default.FlashOn,
        iconTint = Warning,
        title = "Mulai MC Day Mode?",
        description = "Mode ini akan menampilkan checklist dan informasi acara hari ini ($eventName).",
        onDismissRequest = onCancel,
        primaryButton = {
            MCJobPrimaryButton(
                text = "Mulai MC Day Mode",
                onClick = {
                    onStart()
                    onCancel()
                },
                containerColor = Warning,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.PlayArrow
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = "Batal",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// 13. NOTIFICATION PERMISSION PRE-DIALOG
@Composable
fun MCJobNotificationPermissionDialog(
    onEnable: () -> Unit,
    onLater: () -> Unit
) {
    MCJobBaseDialog(
        icon = Icons.Default.NotificationsActive,
        iconTint = Primary,
        title = "Jangan Sampai Lupa Job",
        description = "MCJOBID dapat mengingatkan kamu sebelum acara dan pembayaran jatuh tempo.",
        onDismissRequest = onLater,
        primaryButton = {
            MCJobPrimaryButton(
                text = "Aktifkan Pengingat",
                onClick = {
                    onEnable()
                    onLater()
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = "Nanti Saja",
                onClick = onLater,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

/**
 * Security validation dialog for permanent account deletion.
 * User MUST type validationPhrase (e.g. "HAPUS AKUN SAYA") to enable the Delete button.
 */
@Composable
fun MCJobAccountDeleteDialog(
    validationPhrase: String = "HAPUS AKUN SAYA",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val isValidationCorrect = inputText.trim().equals(validationPhrase, ignoreCase = true)

    MCJobBaseDialog(
        icon = Icons.Default.Warning,
        iconTint = Error,
        title = "Hapus Akun Permanen",
        description = "PERINGATAN: Seluruh data akun, profil MC, booking job, invoice, dan data keuangan Anda akan dihapus permanen dari server database dan tidak dapat dipulihkan.",
        onDismissRequest = onDismiss,
        optionalContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Surface(
                    color = Error.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Error.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Untuk konfirmasi, silakan ketik:\n\"$validationPhrase\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Ketik: $validationPhrase") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Error,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        primaryButton = {
            Button(
                onClick = {
                    if (isValidationCorrect) {
                        onConfirm()
                        onDismiss()
                    }
                },
                enabled = isValidationCorrect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Error,
                    disabledContainerColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = if (isValidationCorrect) Color.White else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hapus Akun Saya Permanen", fontWeight = FontWeight.Bold)
            }
        },
        secondaryButton = {
            MCJobSecondaryButton(
                text = "Batal",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
