package com.isankamil.mcjobid.ui.screen.pricelist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.components.feedback.MCJobTextField
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRateCardScreen(
    viewModel: AddEditRateCardViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title by viewModel.title.collectAsState()
    val category by viewModel.category.collectAsState()
    val price by viewModel.price.collectAsState()
    val durationHours by viewModel.durationHours.collectAsState()
    val description by viewModel.description.collectAsState()
    val inclusions by viewModel.inclusions.collectAsState()
    val addOns by viewModel.addOns.collectAsState()
    val terms by viewModel.terms.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showAddInclusionDialog by remember { mutableStateOf(false) }
    var showAddAddOnDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { success ->
            if (success) onSaveSuccess()
        }
    }

    val categories = listOf("Wedding", "Corporate", "Private Event", "Government", "Other")

    val standardInclusionSuggestions = listOf(
        "Panduan Rundown & Susunan Acara",
        "Standby 1 Jam Sebelum Acara Dimulai",
        "Cue Card Cetak Eksklusif",
        "Briefing Teknis & TM Bersama Vendor",
        "Wardrobe Menyesuaikan Dresscode Acara",
        "Konsep Games & Interaksi Audiens",
        "Voice Over / Opening Acara Formal"
    )

    val standardAddOnSuggestions = listOf(
        "Gladi Resik / Rehearsal H-1 (+Rp 500k)",
        "Overtime per Jam (+Rp 350k/jam)",
        "Sound Effect & Jingle Operator",
        "Konsep Games & Doorprize Custom",
        "Duo Partner Host (+Fee Partner)"
    )

    val standardTermsSuggestions = listOf(
        "DP 30% untuk kunci tanggal acara.",
        "Pelunasan maksimal H-3 sebelum hari H acara.",
        "Reschedule bebas biaya maksimal H-14 acara.",
        "Konsultasi teknis online via Zoom/GMeet."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (viewModel.isEditMode) "Edit Paket Rate Card" else "Tambah Paket Rate Card",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Katalog Harga & Penawaran MC Profesional",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveRateCard() },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (viewModel.isEditMode) "Simpan Perubahan Paket 🚀" else "Simpan Paket Harga 🚀",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: INFORMASI UTAMA PAKET
            item {
                FormSectionCard(
                    title = "INFORMASI UTAMA PAKET",
                    icon = Icons.Default.Sell
                ) {
                    MCJobTextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = "Nama / Judul Paket",
                        isRequired = true,
                        errorMessage = fieldErrors["title"],
                        placeholder = "Contoh: Paket Wedding Akad & Resepsi (Gold)"
                    )

                    // Kategori Acara Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Kategori Acara *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                val isSelected = cat == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateCategory(cat) },
                                    label = {
                                        Text(
                                            text = cat,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Primary.copy(alpha = 0.07f),
                                        labelColor = Primary.copy(alpha = 0.8f)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = Color.Transparent,
                                        borderColor = Primary.copy(alpha = 0.25f)
                                    )
                                )
                            }
                        }
                    }

                    MCJobTextField(
                        value = description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = "Deskripsi Ringkas & Keunggulan",
                        placeholder = "Contoh: Layanan pemandu acara prosesi akad nikah khidmat dilanjutkan resepsi megah interaktif...",
                        minLines = 2,
                        singleLine = false
                    )
                }
            }

            // Section 2: HONORARIUM & DURASI ACARA
            item {
                FormSectionCard(
                    title = "HONORARIUM & ESTIMASI DURASI",
                    icon = Icons.Default.Payments
                ) {
                    MCJobTextField(
                        value = price,
                        onValueChange = { viewModel.updatePrice(it) },
                        label = "Tarif Harga Paket (Rp)",
                        isRequired = true,
                        errorMessage = fieldErrors["price"],
                        placeholder = "Contoh: 3500000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Primary) }
                    )

                    val parsedPrice = price.toLongOrNull() ?: 0L
                    if (parsedPrice > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Format Tarif: ${Formatter.formatCurrency(parsedPrice)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                    }

                    // Durasi Standar Acara (Jam)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Estimasi Durasi Acara (Jam):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                        val durationPresets = listOf("2.0" to "2 Jam", "3.0" to "3 Jam", "4.0" to "4 Jam", "5.0" to "5 Jam", "8.0" to "Full Day")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(durationPresets) { (valStr, label) ->
                                val isSelected = durationHours == valStr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateDurationHours(valStr) },
                                    label = { Text(label, fontSize = 11.5.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: CHECKLIST FASILITAS & INKLUSI LAYANAN
            item {
                FormSectionCard(
                    title = "CHECKLIST FASILITAS & INKLUSI (${inclusions.size})",
                    icon = Icons.Default.Checklist
                ) {
                    Text(
                        text = "Fasilitas apa saja yang sudah termasuk dalam paket ini:",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )

                    // Quick Suggestion Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Saran Cepat Inklusi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(standardInclusionSuggestions) { sug ->
                                val isAlreadyAdded = inclusions.contains(sug)
                                if (!isAlreadyAdded) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable { viewModel.addInclusion(sug) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(sug, fontSize = 11.sp, color = OnBackground)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (inclusions.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada poin fasilitas ditambahkan. Ketuk saran di atas atau tombol di bawah.",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            inclusions.forEachIndexed { index, itemText ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(itemText, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeInclusion(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddInclusionDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Poin Fasilitas Kustom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 4: OPSIONAL TAMBAHAN / ADD-ON LAYANAN
            item {
                FormSectionCard(
                    title = "OPSI TAMBAHAN / ADD-ON (${addOns.size})",
                    icon = Icons.Default.AddBusiness
                ) {
                    Text(
                        text = "Opsi layanan tambahan jika klien membutuhkan upgrade:",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )

                    // Quick Suggestion Chips for Add-Ons
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Saran Add-on Cepat:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(standardAddOnSuggestions) { sug ->
                                val isAlreadyAdded = addOns.contains(sug)
                                if (!isAlreadyAdded) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable { viewModel.addAddOn(sug) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(sug, fontSize = 11.sp, color = OnBackground)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (addOns.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            addOns.forEachIndexed { index, itemText ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(itemText, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeAddOn(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddAddOnDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Add-on Kustom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 5: SYARAT & KETENTUAN (S&K) BOOKING
            item {
                FormSectionCard(
                    title = "SYARAT & KETENTUAN (S&K) BOOKING",
                    icon = Icons.AutoMirrored.Filled.Notes
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Saran Ketentuan Cepat:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(standardTermsSuggestions) { sug ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        val newTerms = if (terms.isBlank()) sug else "$terms\n$sug"
                                        viewModel.updateTerms(newTerms)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(sug, fontSize = 11.sp, color = OnBackground)
                                    }
                                }
                            }
                        }
                    }

                    MCJobTextField(
                        value = terms,
                        onValueChange = { viewModel.updateTerms(it) },
                        label = "Catatan Ketentuan & Pembayaran",
                        placeholder = "Contoh: DP 30% untuk booking tanggal. Pelunasan H-3 acara...",
                        minLines = 3,
                        singleLine = false
                    )
                }
            }
        }
    }

    // Modal Tambah Fasilitas Kustom
    if (showAddInclusionDialog) {
        var customInclusionInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddInclusionDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Tambah Poin Fasilitas", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = customInclusionInput,
                    onValueChange = { customInclusionInput = it },
                    label = { Text("Fasilitas / Inklusi *") },
                    placeholder = { Text("misal: 2x Gladi Bersih Online") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customInclusionInput.isNotBlank()) {
                            viewModel.addInclusion(customInclusionInput)
                            showAddInclusionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tambahkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddInclusionDialog = false }) {
                    Text("Batal", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Modal Tambah Add-On Kustom
    if (showAddAddOnDialog) {
        var customAddOnInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAddOnDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Tambah Opsi Add-on", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = customAddOnInput,
                    onValueChange = { customAddOnInput = it },
                    label = { Text("Nama Layanan Add-on & Estimasi Fee *") },
                    placeholder = { Text("misal: Konsep Games (+Rp 250k)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customAddOnInput.isNotBlank()) {
                            viewModel.addAddOn(customAddOnInput)
                            showAddAddOnDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tambahkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddOnDialog = false }) {
                    Text("Batal", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun FormSectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary, fontSize = 13.sp)
            }
            HorizontalDivider(color = SurfaceVariant)
            content()
        }
    }
}
