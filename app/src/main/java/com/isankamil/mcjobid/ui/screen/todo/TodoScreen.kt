package com.isankamil.mcjobid.ui.screen.todo

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.TodoCategory
import com.isankamil.mcjobid.domain.model.TodoItem
import com.isankamil.mcjobid.domain.model.TodoPriority
import com.isankamil.mcjobid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    onBackClick: () -> Unit
) {
    val todos by viewModel.filteredTodos.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var todoToDelete by remember { mutableStateOf<TodoItem?>(null) }
    var showClearCompletedDialog by remember { mutableStateOf(false) }
    var showTemplateConfirmDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tugas & To-Do MC",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Checklist Persiapan, Gladi Resik & Karier",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showTemplateConfirmDialog = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Template MC", tint = Primary)
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Muat Template Persiapan MC ✨") },
                                onClick = {
                                    showMoreMenu = false
                                    showTemplateConfirmDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary) }
                            )
                            if (stats.completed > 0) {
                                DropdownMenuItem(
                                    text = { Text("Hapus Semua Tugas Selesai 🗑️") },
                                    onClick = {
                                        showMoreMenu = false
                                        showClearCompletedDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Error) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingTodo = null
                    showAddEditDialog = true
                },
                containerColor = Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Tambah Tugas") },
                text = { Text("Tambah Tugas MC", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Progress & Statistics Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Progress Checklist MC",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                                Text(
                                    text = "${stats.completed} dari ${stats.total} tugas selesai (${(stats.percentage * 100).toInt()}%)",
                                    fontSize = 11.5.sp,
                                    color = OnSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (stats.percentage >= 1.0f && stats.total > 0) Success.copy(alpha = 0.12f) else Primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (stats.total == 0) "Belum Ada Tugas" else if (stats.percentage >= 1.0f) "Siap Tampil! 🎉" else "${stats.pending} Tertunda",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stats.percentage >= 1.0f && stats.total > 0) Success else Primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { stats.percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (stats.percentage >= 1.0f) Success else Primary,
                            trackColor = Primary.copy(alpha = 0.12f),
                        )

                        // Quick Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatMiniBadge(
                                label = "Total Tugas",
                                value = "${stats.total}",
                                color = Primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniBadge(
                                label = "Tertunda",
                                value = "${stats.pending}",
                                color = if (stats.pending > 0) Warning else OnSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniBadge(
                                label = "Prioritas Tinggi",
                                value = "${stats.highPriorityPending}",
                                color = if (stats.highPriorityPending > 0) Error else OnSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (stats.total == 0) {
                            Button(
                                onClick = { viewModel.applyMcTemplates() },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f), contentColor = Primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Muat Paket Checklist Persiapan MC Otomatis ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Search & Tab Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari tugas / catatan MC...", fontSize = 12.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Status Tabs (Semua, Tertunda, Selesai)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TodoTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val count = when (tab) {
                                TodoTab.SEMUA -> stats.total
                                TodoTab.BELUM_SELESAI -> stats.pending
                                TodoTab.SELESAI -> stats.completed
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Primary else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) Primary else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setTab(tab) }
                            ) {
                                Text(
                                    text = "${tab.title} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.padding(vertical = 9.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Horizontal Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Semua Kategori" Chip
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("Semua Kategori", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF64748B)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == null,
                                selectedBorderColor = Primary,
                                borderColor = Color(0xFFE2E8F0)
                            )
                        )

                        TodoCategory.entries.forEach { cat ->
                            val isCatSelected = selectedCategory == cat
                            FilterChip(
                                selected = isCatSelected,
                                onClick = {
                                    viewModel.setCategory(if (isCatSelected) null else cat)
                                },
                                label = { Text("${cat.emoji} ${cat.label}", fontSize = 11.5.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF64748B)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isCatSelected,
                                    selectedBorderColor = Primary,
                                    borderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }
            }

            // 3. Task List Items
            if (todos.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Primary.copy(alpha = 0.08f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (selectedTab == TodoTab.SELESAI) Icons.Default.DoneAll else Icons.Default.AssignmentLate,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (selectedTab == TodoTab.SELESAI) "Belum ada tugas yang diselesaikan" else "Tidak ada tugas dalam kategori ini",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Gunakan tombol (+) di bawah untuk membuat tugas persiapan acara atau muat template MC otomatis.",
                                fontSize = 11.5.sp,
                                color = OnSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(todos, key = { it.id }) { todo ->
                    TodoItemCard(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onEdit = {
                            editingTodo = todo
                            showAddEditDialog = true
                        },
                        onDelete = { todoToDelete = todo }
                    )
                }
            }
        }
    }

    // Dialog Tambah / Edit Tugas
    if (showAddEditDialog) {
        TodoAddEditDialog(
            initialTodo = editingTodo,
            onDismiss = { showAddEditDialog = false },
            onSave = { title, notes, category, priority, dueDate ->
                if (editingTodo != null) {
                    viewModel.updateTodo(
                        editingTodo!!.copy(
                            title = title,
                            notes = notes,
                            category = category,
                            priority = priority,
                            dueDate = dueDate
                        )
                    )
                } else {
                    viewModel.addTodo(title, notes, category, priority, dueDate)
                }
                showAddEditDialog = false
            }
        )
    }

    // Dialog Konfirmasi Hapus Tugas
    todoToDelete?.let { todo ->
        AlertDialog(
            onDismissRequest = { todoToDelete = null },
            title = { Text("Hapus Tugas?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Apakah Anda yakin ingin menghapus tugas '${todo.title}'?", fontSize = 13.sp, color = OnSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTodo(todo.id)
                        todoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { todoToDelete = null }) { Text("Batal") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog Konfirmasi Hapus Semua Selesai
    if (showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showClearCompletedDialog = false },
            title = { Text("Bersihkan Tugas Selesai?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Seluruh tugas yang sudah berstatus selesai akan dihapus dari daftar.", fontSize = 13.sp, color = OnSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCompletedTodos()
                        showClearCompletedDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Bersihkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCompletedDialog = false }) { Text("Batal") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog Konfirmasi Muat Template MC
    if (showTemplateConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateConfirmDialog = false },
            title = { Text("Muat Template Persiapan MC?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    "Sistem akan menambahkan 8 checklist tugas esensial persiapan MC profesional (Rundown, Cue Card, Gladi Resik, Sound Check, Fitting Dresscode, Invoice & Testimoni).",
                    fontSize = 12.5.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyMcTemplates()
                        showTemplateConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Muat Sekarang ✨", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateConfirmDialog = false }) { Text("Batal") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun StatMiniBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TodoItemCard(
    todo: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = Color(todo.priority.colorHex)
    val isDone = todo.isCompleted

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFFF8FAFC) else Color.White
        ),
        border = BorderStroke(
            width = if (isDone) 1.dp else 1.2.dp,
            color = if (isDone) Color(0xFFE2E8F0) else priorityColor.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Primary,
                    checkmarkColor = Color.White,
                    uncheckedColor = priorityColor
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = todo.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) OnSurfaceVariant else OnSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (todo.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = todo.notes,
                        fontSize = 11.5.sp,
                        color = OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Primary.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "${todo.category.emoji} ${todo.category.label}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = todo.priority.label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Due Date if any
                    if (todo.dueDate > 0) {
                        val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
                            .format(Date(todo.dueDate))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(10.dp), tint = OnSurfaceVariant)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = dateFormatted, fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Quick Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Hapus",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoAddEditDialog(
    initialTodo: TodoItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, category: TodoCategory, priority: TodoPriority, dueDate: Long) -> Unit
) {
    var title by remember { mutableStateOf(initialTodo?.title ?: "") }
    var notes by remember { mutableStateOf(initialTodo?.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTodo?.category ?: TodoCategory.PERSIAPAN) }
    var selectedPriority by remember { mutableStateOf(initialTodo?.priority ?: TodoPriority.SEDANG) }
    var dueDate by remember { mutableStateOf(initialTodo?.dueDate ?: 0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (dueDate > 0) dueDate else System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) {
                    Text("Pilih Tanggal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTodo != null) "Edit Tugas MC" else "Tambah Tugas Baru 🚀",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Judul Tugas
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Nama / Judul Tugas *") },
                    placeholder = { Text("Contoh: Cek rundown final dengan WO") },
                    singleLine = true,
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Judul tugas wajib diisi", color = Error, fontSize = 11.sp) } } else null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Kategori
                Text("Kategori Tugas:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TodoCategory.entries.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.emoji} ${cat.label}", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Prioritas
                Text("Prioritas Tugas:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TodoPriority.entries.forEach { prio ->
                        val isSelected = selectedPriority == prio
                        val color = Color(prio.colorHex)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) color else Color.White,
                            border = BorderStroke(1.dp, color),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPriority = prio }
                        ) {
                            Text(
                                text = prio.label.replace("Prioritas ", ""),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else color,
                                modifier = Modifier.padding(vertical = 7.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Tanggal Tenggat Waktu
                Text("Tenggat Waktu:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (dueDate > 0) SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(dueDate)) else "Atur Tenggat Waktu (Opsional)",
                                fontSize = 12.5.sp,
                                color = if (dueDate > 0) OnSurface else OnSurfaceVariant,
                                fontWeight = if (dueDate > 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (dueDate > 0) {
                            IconButton(onClick = { dueDate = 0L }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus tanggal", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Catatan / Deskripsi Tambahan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Instruksi Tambahan (Opsional)") },
                    placeholder = { Text("Detail teks opening, dresscode, PIC...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        titleError = true
                    } else {
                        onSave(title.trim(), notes.trim(), selectedCategory, selectedPriority, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simpan Tugas", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
