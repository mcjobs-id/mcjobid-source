package com.isankamil.mcjobid.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.theme.Primary
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBriefDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    val briefTextWhatsApp = buildString {
        append("📋 *EVENT BRIEF MC*\n")
        append("──────────────────────\n")
        append("🎯 *Acara:* ${booking.name}\n")
        append("📅 *Tanggal:* ${Formatter.formatDate(booking.date)}\n")
        append("⏰ *Waktu:* ${booking.start ?: "19:00"} - ${booking.end ?: "22:00"} WIB\n")
        append("📍 *Lokasi:* ${booking.location ?: "-"}\n")
        if (!booking.address.isNullOrBlank()) {
            append("🏢 *Alamat:* ${booking.address}\n")
        }
        append("👤 *Klien:* ${booking.client ?: "-"}\n")
        if (!booking.pic.isNullOrBlank()) {
            append("📞 *PIC / WO:* ${booking.pic}\n")
        }
        append("👔 *Dresscode:* ${booking.dresscode ?: "-"}\n")
        if (!booking.theme.isNullOrBlank()) {
            append("🎨 *Tema:* ${booking.theme}\n")
        }
        append("🎤 *Peran MC:* ${booking.mcType ?: "Single"} (${booking.language ?: "Bahasa Indonesia"})\n")
        if (!booking.audience.isNullOrBlank()) {
            append("👥 *Audience:* ${booking.audience}\n")
        }
        if (!booking.specialRequest.isNullOrBlank()) {
            append("⭐ *Request Khusus:* ${booking.specialRequest}\n")
        }
        if (!booking.note.isNullOrBlank()) {
            append("\n📝 *Brief / Rundown:* ${booking.note}\n")
        }
        append("──────────────────────\n")
        append("Dikirim via mcjob.id")
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ringkasan Event Brief",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Skenario & Eksekusi Acara Hari-H",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Scrollable Brief Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Event Title Banner (Solid Primary)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = booking.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Formatter.formatDate(booking.date),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${booking.start ?: "19:00"} - ${booking.end ?: "22:00"} WIB",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Structured Details
                    BriefDetailItem(
                        icon = Icons.Default.Place,
                        label = "VENUE & ALAMAT",
                        value = "${booking.location ?: "-"}${booking.address?.let { "\nAlamat: $it" } ?: ""}"
                    )

                    BriefDetailItem(
                        icon = Icons.Default.Person,
                        label = "KLIEN & PENYELENGGARA",
                        value = booking.client ?: "-"
                    )

                    booking.pic?.let {
                        BriefDetailItem(
                            icon = Icons.Default.Call,
                            label = "KONTAK PIC / WO",
                            value = it
                        )
                    }

                    BriefDetailItem(
                        icon = Icons.Default.Checkroom,
                        label = "DRESSCODE & TEMA",
                        value = "${booking.dresscode ?: "Batik / Formal"}${booking.theme?.let { " • Tema: $it" } ?: ""}"
                    )

                    BriefDetailItem(
                        icon = Icons.Default.Mic,
                        label = "JENIS MC & BAHASA",
                        value = "${booking.mcType ?: "Single"} (${booking.language ?: "Bahasa Indonesia"})"
                    )

                    booking.audience?.let {
                        BriefDetailItem(
                            icon = Icons.Default.Group,
                            label = "TARGET AUDIENCE",
                            value = it
                        )
                    }

                    booking.specialRequest?.let {
                        BriefDetailItem(
                            icon = Icons.Default.Star,
                            label = "REQUEST KHUSUS KLIEN",
                            value = it
                        )
                    }

                    booking.note?.let {
                        BriefDetailItem(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            label = "CATATAN BRIEF & RUNDOWN",
                            value = it
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(briefTextWhatsApp))
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Primary),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Salin Brief",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, briefTextWhatsApp)
                            }
                            context.startActivity(Intent.createChooser(intent, "Bagikan Event Brief"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bagikan ke WA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BriefDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.1f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
