package com.isankamil.mcjobid.ui.screen.pricelist

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.RateCard
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListScreen(
    viewModel: PriceListViewModel,
    onBackClick: () -> Unit,
    onAddNewRateCard: () -> Unit,
    onEditRateCard: (String) -> Unit,
    onUseRateCardForJob: (RateCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val rateCards by viewModel.filteredRateCards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var cardToDelete by remember { mutableStateOf<RateCard?>(null) }
    var showShareModalForCard by remember { mutableStateOf<RateCard?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Price List & Rate Card", fontWeight = FontWeight.Bold)
                        Text("Katalog Paket & Penawaran Harga MC", fontSize = 12.sp, color = OnSurfaceVariant)
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
        floatingActionButton = {
            if (rateCards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddNewRateCard,
                    containerColor = Primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Paket Harga Baru 🚀", fontWeight = FontWeight.Bold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Search & Category Filters
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Cari paket, fasilitas, atau deskripsi...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Tabs
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategorySelect(cat) },
                                label = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = OnBackground
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = null
                            )
                        }
                    }
                }
            }

            // Rate Card List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (rateCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Sell,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Katalog Rate Card Kosong",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Buat paket harga pertama Anda untuk memudahkan pengiriman penawaran ke klien & integrasi job.",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddNewRateCard,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buat Paket Harga Pertama 🚀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rateCards, key = { it.id }) { card ->
                        RateCardItemCard(
                            rateCard = card,
                            onShareClick = {
                                showShareModalForCard = card
                            },
                            onUseForJobClick = {
                                onUseRateCardForJob(card)
                            },
                            onEditClick = {
                                onEditRateCard(card.id)
                            },
                            onDuplicateClick = {
                                viewModel.duplicateRateCard(card)
                            },
                            onDeleteClick = {
                                cardToDelete = card
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Share WA Quote & Live Preview (Polesan Bersih, Background Putih & Interaktif)
    showShareModalForCard?.let { card ->
        var inputClientName by remember { mutableStateOf("") }
        var inputEventDate by remember { mutableStateOf("") }
        var inputCustomNote by remember { mutableStateOf("") }

        val dynamicQuoteText = remember(card, inputClientName, inputEventDate, inputCustomNote) {
            viewModel.generateWhatsAppQuote(
                card = card,
                clientName = inputClientName,
                eventDate = inputEventDate,
                customNote = inputCustomNote
            )
        }

        AlertDialog(
            onDismissRequest = { showShareModalForCard = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Kirim Penawaran WhatsApp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(card.title, fontSize = 12.sp, color = OnSurfaceVariant, maxLines = 1)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputClientName,
                        onValueChange = { inputClientName = it },
                        label = { Text("Nama Klien / Calon Pengantin") },
                        placeholder = { Text("misal: Bpk. Kevin & Vania") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputEventDate,
                        onValueChange = { inputEventDate = it },
                        label = { Text("Estimasi Tanggal Acara (Opsional)") },
                        placeholder = { Text("misal: 12 September 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Pratinjau Pesan WhatsApp:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )

                    // WhatsApp Message Card Preview
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0FDF4), // WhatsApp Soft Emerald Tint
                        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 190.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = dynamicQuoteText,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(dynamicQuoteText))
                            Toast.makeText(context, "Teks penawaran disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                val encoded = URLEncoder.encode(dynamicQuoteText, StandardCharsets.UTF_8.toString())
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=$encoded")).apply {
                                    setPackage("com.whatsapp")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback generic send intent if WhatsApp specific package is not resolved
                                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, dynamicQuoteText)
                                }
                                context.startActivity(Intent.createChooser(genericIntent, "Kirim Penawaran via"))
                            }
                            showShareModalForCard = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka WA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareModalForCard = null }) {
                    Text("Tutup", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Modal Konfirmasi Hapus Paket
    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Error) },
            title = { Text("Hapus Paket Rate Card?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus paket '${card.title}'? Paket ini tidak akan muncul lagi di katalog penawaran.",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRateCard(card.id)
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hapus Paket", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun RateCardItemCard(
    rateCard: RateCard,
    onShareClick: () -> Unit,
    onUseForJobClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Overflow Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = rateCard.category.uppercase(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = OnSurfaceVariant)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = Color.White
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Paket") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplikat Paket") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDuplicateClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Hapus Paket", color = Error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = rateCard.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnBackground
            )

            // Price & Duration Tag Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Formatter.formatCurrency(rateCard.price),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (rateCard.durationHours > 0) "${rateCard.durationHours} Jam" else "Fleksibel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            if (rateCard.description.isNotBlank()) {
                Text(
                    text = rateCard.description,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Inclusions Checklist Snippet
            if (rateCard.inclusions.isNotEmpty()) {
                HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    rateCard.inclusions.take(4).forEach { inc ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(inc, fontSize = 12.sp, color = Color(0xFF334155), maxLines = 1)
                        }
                    }
                    if (rateCard.inclusions.size > 4) {
                        Text(
                            text = "+ ${rateCard.inclusions.size - 4} fasilitas inklusi lainnya...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Share WA Quote + Gunakan untuk Job Baru
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF16A34A)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share WA Quote", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onUseForJobClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gunakan utk Job", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
