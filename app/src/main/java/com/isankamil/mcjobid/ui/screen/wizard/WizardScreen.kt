package com.isankamil.mcjobid.ui.screen.wizard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.components.feedback.*
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WizardScreen(
    viewModel: WizardViewModel,
    onWizardComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val stepError by viewModel.stepError.collectAsState()

    var showDraftDialog by remember { mutableStateOf(false) }

    val currentStep = when (val s = uiState) {
        is WizardUiState.Editing -> s.currentStep
        is WizardUiState.Saving -> s.currentStep
        is WizardUiState.Error -> s.currentStep
        is WizardUiState.Success -> 5
    }

    val draft = when (val s = uiState) {
        is WizardUiState.Editing -> s.draft
        is WizardUiState.Saving -> s.draft
        is WizardUiState.Error -> s.draft
        is WizardUiState.Success -> WizardState()
    }

    val progressAnimated by animateFloatAsState(
        targetValue = currentStep / 5f,
        label = "WizardProgress"
    )

    val wizardScrollState = rememberScrollState()

    LaunchedEffect(currentStep) {
        wizardScrollState.scrollTo(0)
    }

    LaunchedEffect(uiState) {
        if (uiState is WizardUiState.Success) {
            onWizardComplete()
        }
    }

    BackHandler(enabled = true) {
        if (currentStep > 1) {
            viewModel.prevStep()
        } else {
            showDraftDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profil MC Profesional",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = getStepSubtitle(currentStep),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.prevStep()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Primary)
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${draft.getCompletionPercentage()}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        TextButton(
                            onClick = { showDraftDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Lanjutkan Nanti",
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                fontSize = 13.sp
                            )
                        }
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
                .imePadding()
                .navigationBarsPadding()
                .background(Color(0xFFF8FAFC))
        ) {
            // Step Progress Bar
            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Primary,
                trackColor = Color(0xFFE2E8F0)
            )

            // Step Indicator Numbers Bar
            StepIndicatorHeader(currentStep = currentStep)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .imeNestedScroll()
                    .verticalScroll(wizardScrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step Content Switcher
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> (width * 0.15f * direction).toInt() } + fadeIn(animationSpec = tween(250)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> (-width * 0.15f * direction).toInt() } + fadeOut(animationSpec = tween(200)))
                    },
                    label = "WizardStepAnimation"
                ) { targetStep ->
                    when (targetStep) {
                        1 -> Step1Identitas(
                            draft = draft,
                            error = stepError,
                            onDraftChange = { viewModel.updateDraft(it) },
                            onNext = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.nextStep()
                            },
                            onSkip = { showDraftDialog = true }
                        )
                        2 -> Step2ProfilProfesional(
                            draft = draft,
                            error = stepError,
                            onDraftChange = { viewModel.updateDraft(it) },
                            onPrev = { viewModel.prevStep() },
                            onNext = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.nextStep()
                            }
                        )
                        3 -> Step3RateCard(
                            draft = draft,
                            error = stepError,
                            onDraftChange = { viewModel.updateDraft(it) },
                            onPrev = { viewModel.prevStep() },
                            onNext = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.nextStep()
                            }
                        )
                        4 -> Step4Rekening(
                            draft = draft,
                            error = stepError,
                            onDraftChange = { viewModel.updateDraft(it) },
                            onPrev = { viewModel.prevStep() },
                            onNext = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.nextStep()
                            }
                        )
                        5 -> Step5Preview(
                            draft = draft,
                            uiState = uiState,
                            error = (uiState as? WizardUiState.Error)?.message,
                            onPrev = { viewModel.prevStep() },
                            onSave = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.saveProfile()
                            }
                        )
                    }
                }
            }
        }
    }

    // Unsaved Draft Exit Dialog
    if (showDraftDialog) {
        MCJobUnsavedChangesDialog(
            title = "Lanjutkan Isi Profil Nanti?",
            description = "Data profil MC Anda otomatis tersimpan. Anda dapat langsung masuk ke aplikasi dan melengkapinya kapan saja dari menu Profil.",
            primaryCtaText = "Tetap Isi Profil",
            secondaryCtaText = "Lanjutkan Nanti (Lewati)",
            onStayEdit = { showDraftDialog = false },
            onExit = {
                showDraftDialog = false
                onCancel()
            }
        )
    }
}

@Composable
private fun StepIndicatorHeader(currentStep: Int) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val steps = listOf("Branding", "Spesialisasi", "Rate Card", "Rekening", "Preview")
            steps.forEachIndexed { index, label ->
                val stepNum = index + 1
                val isActive = currentStep == stepNum
                val isDone = currentStep > stepNum

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isDone -> Success
                            isActive -> Primary
                            else -> Color(0xFFE2E8F0)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text(
                                    text = "$stepNum",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) Primary else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

private fun getStepSubtitle(step: Int): String {
    return when (step) {
        1 -> "Step 1 dari 5: Identitas & Stage Branding"
        2 -> "Step 2 dari 5: Portfolio & Spesialisasi"
        3 -> "Step 3 dari 5: Base Rate Card & DP"
        4 -> "Step 4 dari 5: Rekening Bank & Pajak"
        5 -> "Step 5 dari 5: Preview & Konfirmasi Profil MC"
        else -> ""
    }
}

// ==========================================
// STEP 1: IDENTITAS & STAGE BRANDING
// ==========================================
@Composable
private fun Step1Identitas(
    draft: WizardState,
    error: String?,
    onDraftChange: ((WizardState) -> WizardState) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "IDENTITAS & BRANDING MC",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            HorizontalDivider(color = SurfaceVariant)

            val photoLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let { onDraftChange { d -> d.copy(photoUri = it.toString()) } }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(88.dp)
                        .clickable { photoLauncher.launch("image/*") }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(84.dp),
                        border = BorderStroke(2.dp, Primary)
                    ) {
                        com.isankamil.mcjobid.ui.components.MCJobAvatarImage(
                            photoUri = draft.photoUri,
                            contentDescription = "Foto Profil MC",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Secondary,
                        modifier = Modifier.size(28.dp),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Ubah Foto", tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(onClick = { photoLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!draft.photoUri.isNullOrBlank()) "Ganti Foto Profil MC" else "+ Pilih Foto Profil MC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            MCJobTextField(
                value = draft.displayName,
                onValueChange = { valText -> onDraftChange { it.copy(displayName = valText) } },
                label = "Nama Lengkap",
                isRequired = true,
                placeholder = "Contoh: Isan Kamil, S.Kom, C.MC",
                tooltipText = "Gelar & nama lengkap resmi akan dicantumkan di Penawaran Kerjasama & Kontrak Legal."
            )

            MCJobTextField(
                value = draft.stageName,
                onValueChange = { valText -> onDraftChange { it.copy(stageName = valText) } },
                label = "Nama Panggung",
                placeholder = "Contoh: MC Isan Kamil",
                tooltipText = "Nama panggung komersial yang ditampilkan pada Kartu Profil & Header Invoice Klien."
            )

            MCJobTextField(
                value = draft.phoneNumber,
                onValueChange = { valText -> onDraftChange { it.copy(phoneNumber = valText) } },
                label = "WhatsApp",
                isRequired = true,
                placeholder = "Contoh: 081234567890",
                tooltipText = "Nomor WhatsApp aktif untuk menerima konfirmasi booking dan pesan instruksi otomatis.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            MCJobTextField(
                value = draft.secondaryPhone,
                onValueChange = { valText -> onDraftChange { it.copy(secondaryPhone = valText) } },
                label = "WhatsApp Manager",
                placeholder = "Contoh: 089876543210 (Manager Rina)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            MCJobTextField(
                value = draft.email,
                onValueChange = { valText -> onDraftChange { it.copy(email = valText) } },
                label = "Email",
                placeholder = "booking@isankamil.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            MCJobTextField(
                value = draft.instagramHandle,
                onValueChange = { valText -> onDraftChange { it.copy(instagramHandle = valText) } },
                label = "Instagram",
                placeholder = "Contoh: @mcisankamil",
                tooltipText = "Username Instagram profesional MC yang ditampilkan di Invoice PDF & Kartu Profil Digital."
            )

            if (!error.isNullOrBlank()) {
                Text(
                    text = error,
                    color = Error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            MCJobPrimaryButton(
                text = "Lanjut ke Spesialisasi",
                onClick = onNext,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = draft.displayName.trim().length >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Lanjutkan Nanti (Langsung ke Beranda)",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==========================================
// STEP 2: PROFIL PROFESIONAL & SPESIALISASI
// ==========================================
@Composable
private fun Step2ProfilProfesional(
    draft: WizardState,
    error: String?,
    onDraftChange: ((WizardState) -> WizardState) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PORTFOLIO & SPESIALISASI",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            HorizontalDivider(color = SurfaceVariant)

            MCJobTextField(
                value = draft.bio,
                onValueChange = { valText -> onDraftChange { it.copy(bio = valText) } },
                label = "Bio",
                placeholder = "Contoh: Professional Wedding & Corporate MC",
                singleLine = false,
                minLines = 2,
                tooltipText = "Tagline komersial yang menggambarkan 'selling point' kamu di depan klien/WO."
            )

            MCJobTextField(
                value = draft.city,
                onValueChange = { valText -> onDraftChange { it.copy(city = valText) } },
                label = "Domisili",
                isRequired = true,
                placeholder = "Contoh: Jakarta Selatan / Surabaya"
            )

            MCJobTextField(
                value = draft.areaCoverage,
                onValueChange = { valText -> onDraftChange { it.copy(areaCoverage = valText) } },
                label = "Cakupan Area",
                placeholder = "Contoh: Jabodetabek, Luar Kota & Overseas"
            )

            // Multi-Select Specializations
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Spesialisasi Jenis Acara (Pilih Banyak)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
                val defaultSpecs = listOf("Wedding Formal", "Wedding Adat", "Corporate Gala", "Seminar & Workshop", "Concert & Festival", "Private Party", "Government & Protocol")
                val allSpecs = (defaultSpecs + draft.customSpecializations).distinct()
                
                FlowRowLayout(horizontalGap = 8.dp, verticalGap = 8.dp) {
                    allSpecs.forEach { spec ->
                        val selected = draft.selectedSpecializations.contains(spec)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val current = draft.selectedSpecializations.toMutableSet()
                                if (selected) current.remove(spec) else current.add(spec)
                                onDraftChange { it.copy(selectedSpecializations = current) }
                            },
                            label = { Text(spec, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            leadingIcon = if (selected) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null,
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Primary.copy(alpha = 0.07f),
                                labelColor = Primary.copy(alpha = 0.8f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Primary.copy(alpha = 0.25f)
                            )
                        )
                    }
                }

                var customSpecInput by remember { mutableStateOf("") }
                
                // Helper function to add custom spec
                val addCustomSpec = {
                    if (customSpecInput.isNotBlank()) {
                        val trimmed = customSpecInput.trim()
                        onDraftChange {
                            it.copy(
                                customSpecializations = (it.customSpecializations + trimmed).distinct(),
                                selectedSpecializations = it.selectedSpecializations + trimmed
                            )
                        }
                        customSpecInput = ""
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MCJobTextField(
                        value = customSpecInput,
                        onValueChange = { customSpecInput = it },
                        label = "Tambah Spesialisasi Lain (Manual)",
                        placeholder = "Ketik spesialisasi baru...",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { addCustomSpec() })
                    )
                    IconButton(
                        onClick = { addCustomSpec() },
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .size(48.dp)
                            .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Primary)
                    }
                }
            }

            MCJobTextField(
                value = draft.languages,
                onValueChange = { valText -> onDraftChange { it.copy(languages = valText) } },
                label = "Bahasa Pengantar Acara",
                placeholder = "Contoh: Bahasa Indonesia, English, Bahasa Jawa"
            )

            // Experience Tier Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Jam Terbang / Pengalaman", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                val expTiers = listOf("< 2 Tahun", "2-5 Tahun", "5+ Tahun", "100+ Event")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(expTiers) { tier ->
                        val selected = draft.experienceYears == tier
                        FilterChip(
                            selected = selected,
                            onClick = { onDraftChange { it.copy(experienceYears = tier) } },
                            label = { Text(tier, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Primary.copy(alpha = 0.07f),
                                labelColor = Primary.copy(alpha = 0.8f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Primary.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }

            if (!error.isNullOrBlank()) {
                Text(text = error, color = Error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MCJobSecondaryButton(
                    text = "Kembali",
                    onClick = onPrev,
                    modifier = Modifier.weight(1f)
                )
                MCJobPrimaryButton(
                    text = "Lanjut ke Rate Card",
                    onClick = onNext,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

// ==========================================
// STEP 3: BASE RATE CARD & KEBIJAKAN FINANSIAL
// ==========================================
@Composable
private fun Step3RateCard(
    draft: WizardState,
    error: String?,
    onDraftChange: ((WizardState) -> WizardState) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RATE CARD & KEBIJAKAN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            HorizontalDivider(color = SurfaceVariant)

            MCJobTextField(
                value = draft.baseFeeText,
                onValueChange = { valText ->
                    val cleanDigits = valText.filter { it.isDigit() }
                    onDraftChange { it.copy(baseFeeText = cleanDigits) }
                },
                label = "Tarif Dasar (Rp)",
                isRequired = true,
                placeholder = "Contoh: 3500000",
                tooltipText = "Honor acuan standar per event yang digunakan untuk estimasi penawaran harga cepat.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (draft.baseFeeText.isNotBlank()) {
                val feeVal = draft.baseFeeText.toLongOrNull() ?: 0L
                Text(
                    text = "Format Honor: ${Formatter.formatCurrency(feeVal)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // Quick Choice Base Fee Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pilihan Cepat Rate Card Acuan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                val quickRates = listOf("2000000" to "Rp 2 Juta", "3500000" to "Rp 3.5 Juta", "5000000" to "Rp 5 Juta", "7500000" to "Rp 7.5 Juta")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickRates) { (valStr, label) ->
                        val selected = draft.baseFeeText == valStr
                        FilterChip(
                            selected = selected,
                            onClick = { onDraftChange { it.copy(baseFeeText = valStr) } },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Primary.copy(alpha = 0.07f),
                                labelColor = Primary.copy(alpha = 0.8f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Primary.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }

            // Standar DP % Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ketentuan DP Minimum (%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30 to "DP 30%", 50 to "DP 50%", 100 to "Lunas 100%").forEach { (pct, label) ->
                        val selected = draft.defaultDpPercentage == pct
                        FilterChip(
                            selected = selected,
                            onClick = { onDraftChange { it.copy(defaultDpPercentage = pct) } },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                                containerColor = Primary.copy(alpha = 0.07f),
                                labelColor = Primary.copy(alpha = 0.8f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Primary.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }

            MCJobTextField(
                value = draft.npwpNumber,
                onValueChange = { valText -> onDraftChange { it.copy(npwpNumber = valText) } },
                label = "NPWP",
                placeholder = "Contoh: 12.345.678.9-012.000",
                tooltipText = "Nomor NPWP/NIK untuk penerbitan bukti potong PPh 21 oleh Klien Korporat/Perusahaan.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (!error.isNullOrBlank()) {
                Text(text = error, color = Error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MCJobSecondaryButton(
                    text = "Kembali",
                    onClick = onPrev,
                    modifier = Modifier.weight(1f)
                )
                MCJobPrimaryButton(
                    text = "Lanjut ke Rekening",
                    onClick = onNext,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

// ==========================================
// STEP 4: REKENING BANK & PAJAK
// ==========================================
@Composable
private fun Step4Rekening(
    draft: WizardState,
    error: String?,
    onDraftChange: ((WizardState) -> WizardState) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REKENING PEMBAYARAN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            HorizontalDivider(color = SurfaceVariant)

            MCJobTextField(
                value = draft.bankName,
                onValueChange = { valText -> onDraftChange { it.copy(bankName = valText) } },
                label = "Nama Bank",
                isRequired = true,
                placeholder = "Contoh: BCA / Mandiri / BNI"
            )

            MCJobTextField(
                value = draft.bankAccountNumber,
                onValueChange = { valText -> onDraftChange { it.copy(bankAccountNumber = valText) } },
                label = "Nomor Rekening",
                isRequired = true,
                placeholder = "Contoh: 1234567890",
                tooltipText = "Nomor rekening tujuan transfer DP & Pelunasan yang akan otomatis dicetak pada Invoice PDF.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            MCJobTextField(
                value = draft.bankAccountHolder,
                onValueChange = { valText -> onDraftChange { it.copy(bankAccountHolder = valText) } },
                label = "Pemilik Rekening",
                isRequired = true,
                placeholder = "Contoh: Isan Kamil"
            )

            MCJobTextField(
                value = draft.secondaryBankInfo,
                onValueChange = { valText -> onDraftChange { it.copy(secondaryBankInfo = valText) } },
                label = "Rekening Sekunder",
                placeholder = "Contoh: Mandiri 0987654321 / QRIS GoPay"
            )

            MCJobTextField(
                value = draft.termsAndConditions,
                onValueChange = { valText -> onDraftChange { it.copy(termsAndConditions = valText) } },
                label = "Syarat & Ketentuan",
                placeholder = "Contoh: DP yang sudah dibayarkan tidak dapat dikembalikan jika pembatalan H-7 acara.",
                tooltipText = "Aturan kebijakan pembatalan & pengembalian DP yang otomatis dicantumkan di bagian bawah Invoice.",
                singleLine = false,
                minLines = 2
            )

            if (!error.isNullOrBlank()) {
                Text(text = error, color = Error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MCJobSecondaryButton(
                    text = "Kembali",
                    onClick = onPrev,
                    modifier = Modifier.weight(1f)
                )
                MCJobPrimaryButton(
                    text = "Lanjut ke Preview",
                    onClick = onNext,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

// ==========================================
// STEP 5: PREVIEW HASIL WIZARD & KONFIRMASI
// ==========================================
@Composable
private fun Step5Preview(
    draft: WizardState,
    uiState: WizardUiState,
    error: String?,
    onPrev: () -> Unit,
    onSave: () -> Unit
) {
    val isSaving = uiState is WizardUiState.Saving

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Card: Confirmation Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Preview Profil MC Anda",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Primary
                    )
                    Text(
                        text = "Periksa kembali seluruh data yang telah Anda isi sebelum menyimpan dan mengaktifkan profil.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 1: Identitas & Branding MC
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("IDENTITAS & BRANDING MC", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Avatar & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        border = BorderStroke(2.dp, Primary.copy(alpha = 0.2f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (draft.photoUri.isNotBlank()) {
                            AsyncImage(
                                model = draft.photoUri,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = draft.displayName.ifEmpty { "Belum diisi" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        if (draft.stageName.isNotBlank() && draft.stageName != draft.displayName) {
                            Text(
                                text = "Nama Panggung: ${draft.stageName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }
                    }
                }

                if (draft.bio.isNotBlank()) {
                    PreviewFieldItem(label = "Bio / Tagline", value = draft.bio)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PreviewFieldItem(
                        label = "Kota Domisili",
                        value = draft.city.ifEmpty { "-" },
                        modifier = Modifier.weight(1f)
                    )
                    PreviewFieldItem(
                        label = "Jangkauan Area",
                        value = draft.areaCoverage.ifEmpty { "-" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 2: Kontak & Media Sosial
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("KONTAK & MEDIA SOSIAL", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PreviewFieldItem(
                        label = "No. WhatsApp",
                        value = draft.phoneNumber.ifEmpty { "-" },
                        modifier = Modifier.weight(1f)
                    )
                    PreviewFieldItem(
                        label = "Email",
                        value = draft.email.ifEmpty { "-" },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (draft.instagramHandle.isNotBlank()) {
                    PreviewFieldItem(label = "Instagram", value = draft.instagramHandle)
                }
            }
        }

        // Section 3: Spesialisasi & Jam Terbang
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SPESIALISASI & JAM TERBANG", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                if (draft.selectedSpecializations.isNotEmpty()) {
                    Text("Spesialisasi Acara:", fontSize = 11.sp, color = Color(0xFF64748B))
                    FlowRowLayout(horizontalGap = 6.dp, verticalGap = 6.dp) {
                        draft.selectedSpecializations.forEach { spec ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Primary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = spec,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PreviewFieldItem(
                        label = "Bahasa Pengantar",
                        value = draft.languages.ifEmpty { "-" },
                        modifier = Modifier.weight(1f)
                    )
                    PreviewFieldItem(
                        label = "Jam Terbang",
                        value = draft.experienceYears.ifEmpty { "-" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 4: Tarif & Rekening Invoice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TARIF & REKENING INVOICE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                val baseFeeNum = draft.baseFeeText.filter { it.isDigit() }.toLongOrNull() ?: 0L
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PreviewFieldItem(
                        label = "Tarif Dasar (Base Fee)",
                        value = if (baseFeeNum > 0) Formatter.formatCurrency(baseFeeNum) else "-",
                        modifier = Modifier.weight(1f)
                    )
                    PreviewFieldItem(
                        label = "DP Minimal",
                        value = "${draft.defaultDpPercentage}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (draft.bankName.isNotBlank() || draft.bankAccountNumber.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PreviewFieldItem(
                            label = "Bank & No. Rekening",
                            value = "${draft.bankName} ${draft.bankAccountNumber}".trim().ifEmpty { "-" },
                            modifier = Modifier.weight(1f)
                        )
                        PreviewFieldItem(
                            label = "Atas Nama (a.n)",
                            value = draft.bankAccountHolder.ifEmpty { "-" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (draft.secondaryBankInfo.isNotBlank()) {
                    PreviewFieldItem(label = "Rekening Sekunder", value = draft.secondaryBankInfo)
                }

                if (draft.termsAndConditions.isNotBlank()) {
                    PreviewFieldItem(label = "Ketentuan Pembatalan", value = draft.termsAndConditions)
                }
            }
        }

        if (!error.isNullOrBlank()) {
            Text(text = error, color = Error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // CTA Buttons
        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Menyimpan Profil...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan & Aktifkan Profil MC", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        OutlinedButton(
            onClick = onPrev,
            enabled = !isSaving,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Kembali ke Step Sebelumnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PreviewFieldItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
    }
}

@Composable
private fun PreviewRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowLayout(
    horizontalGap: androidx.compose.ui.unit.Dp,
    verticalGap: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
        modifier = Modifier.fillMaxWidth(),
        content = { content() }
    )
}
