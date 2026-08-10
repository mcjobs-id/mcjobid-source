package com.isankamil.mcjobid.ui.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.components.feedback.MCJobPrimaryButton
import com.isankamil.mcjobid.ui.components.feedback.MCJobSecondaryButton
import com.isankamil.mcjobid.ui.theme.Primary
import com.isankamil.mcjobid.util.Formatter
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

private fun showNativeDatePicker(context: Context, initialDateStr: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    try {
        val parts = initialDateStr.split("-")
        if (parts.size == 3) {
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
    } catch (_: Exception) {}

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun PaymentDialog(
    bookingName: String,
    totalFee: Long,
    currentPaid: Long,
    onSavePayment: (amount: Long, date: String, method: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val remaining = maxOf(0L, totalFee - currentPaid)
    var amountText by remember { mutableStateOf(remaining.toString()) }
    var selectedMethod by remember { mutableStateOf("Bank Transfer") }
    var notesText by remember { mutableStateOf("Pelunasan / DP") }
    var paymentDate by remember { mutableStateOf(LocalDate.now().toString()) }

    val methods = listOf("Bank Transfer", "Cash", "E-Wallet")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Catat Pembayaran Baru",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = bookingName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Honor:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatter.formatCurrency(totalFee), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sisa Piutang:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatter.formatCurrency(remaining), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Primary)
                        }
                    }
                }

                // Date Selector
                val formattedDateStr = remember(paymentDate) {
                    try { Formatter.formatDateShort(LocalDate.parse(paymentDate)) } catch (_: Exception) { paymentDate }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showNativeDatePicker(context, paymentDate) { selected ->
                                paymentDate = selected
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = formattedDateStr,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Tgl Pembayaran") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = Primary,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Nominal Pembayaran (Rp)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("METODE PEMBAYARAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        methods.forEach { method ->
                            val isSelected = selectedMethod == method
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMethod = method },
                                label = { Text(method, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Pembayaran") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            MCJobPrimaryButton(
                text = "Simpan Pembayaran",
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        onSavePayment(amount, paymentDate, selectedMethod, notesText)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            MCJobSecondaryButton(
                text = "Batal",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
