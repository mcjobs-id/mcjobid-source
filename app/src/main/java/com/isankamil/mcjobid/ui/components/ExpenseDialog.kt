package com.isankamil.mcjobid.ui.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.components.feedback.MCJobPrimaryButton
import com.isankamil.mcjobid.ui.components.feedback.MCJobSecondaryButton
import com.isankamil.mcjobid.ui.components.feedback.MCJobTextField
import com.isankamil.mcjobid.ui.theme.Primary
import com.isankamil.mcjobid.ui.theme.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    bookingName: String = "",
    bookingId: String = "",
    availableBookings: List<Booking> = emptyList(),
    onSaveExpense: (bookingId: String, category: String, amount: Long, date: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedBookingId by remember { mutableStateOf(bookingId.ifBlank { availableBookings.firstOrNull()?.id ?: "" }) }
    var selectedBookingName by remember {
        mutableStateOf(
            if (bookingName.isNotBlank()) bookingName
            else availableBookings.find { it.id == selectedBookingId }?.name ?: "Pilih Acara / Job"
        )
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Transport") }
    var noteText by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Transport", "Tol & Parkir", "Wardrobe", "MUA & Hairdo", "Sound / Mic", "Crew / Partner", "Akomodasi", "Konsumsi", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Catat Pengeluaran",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Primary,
                    textAlign = TextAlign.Center
                )
                if (bookingName.isNotBlank()) {
                    Text(
                        text = bookingName,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Booking Selector if multiple available
                if (bookingId.isBlank() && availableBookings.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        MCJobTextField(
                            value = selectedBookingName,
                            onValueChange = {},
                            readOnly = true,
                            label = "Acara / Job Terkait",
                            placeholder = "Pilih Acara",
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = Primary) },
                            trailingIcon = { IconButton(onClick = { dropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                            modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            availableBookings.forEach { b ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(b.name, fontWeight = FontWeight.Bold)
                                            Text("${b.client ?: "Client"} • ${b.date}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    },
                                    onClick = {
                                        selectedBookingId = b.id
                                        selectedBookingName = b.name
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Kategori Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(text = cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Primary.copy(alpha = 0.05f),
                                    labelColor = Color(0xFF64748B)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = Color.Transparent,
                                    borderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }

                // Date Selector
                val formattedDateStr = remember(expenseDate) {
                    try { Formatter.formatDateShort(LocalDate.parse(expenseDate)) } catch (_: Exception) { expenseDate }
                }
                Box(modifier = Modifier.fillMaxWidth().clickable {
                    showNativeDatePicker(context, expenseDate) { selected -> expenseDate = selected }
                }) {
                    MCJobTextField(
                        value = formattedDateStr,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = "Tanggal Pengeluaran",
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Amount Text Field
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MCJobTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { char -> char.isDigit() }
                            errorMessage = null
                        },
                        label = "Nominal Pengeluaran (Rp)",
                        isRequired = true,
                        placeholder = "Contoh: 50000",
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0) {
                        Text(
                            text = Formatter.formatCurrency(amt),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Notes Text Field
                MCJobTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = "Keterangan / Nama Vendor",
                    placeholder = "Contoh: Bensin & Tol via Cipali",
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Primary) }
                )

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MCJobPrimaryButton(
                    text = "Simpan Pengeluaran",
                    onClick = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (selectedBookingId.isBlank()) {
                            errorMessage = "Pilih job/acara terkait pengeluaran ini."
                            return@MCJobPrimaryButton
                        }
                        if (amt <= 0) {
                            errorMessage = "Nominal harus lebih besar dari Rp0."
                            return@MCJobPrimaryButton
                        }
                        onSaveExpense(selectedBookingId, selectedCategory, amt, expenseDate, noteText)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                MCJobSecondaryButton(
                    text = "Batal",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
