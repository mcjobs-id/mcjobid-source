package com.isankamil.mcjobid.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.ui.components.MCJobAvatarImage
import com.isankamil.mcjobid.ui.components.feedback.MCJobAccountDeleteDialog
import com.isankamil.mcjobid.ui.components.feedback.MCJobErrorDialog
import com.isankamil.mcjobid.ui.components.feedback.MCJobLoadingDialog
import com.isankamil.mcjobid.ui.components.feedback.MCJobSuccessDialog
import com.isankamil.mcjobid.ui.components.feedback.MCJobTextField
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToWizard: () -> Unit = {},
    onAccountDeleted: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val deletionError by viewModel.deletionError.collectAsState()
    val isAccountDeleted by viewModel.isAccountDeleted.collectAsState()

    LaunchedEffect(isAccountDeleted) {
        if (isAccountDeleted) {
            onAccountDeleted()
        }
    }

    val p = profile ?: UserProfile(userId = "")

    var showEditIdentitas by remember { mutableStateOf(false) }
    var showEditPortofolio by remember { mutableStateOf(false) }
    var showEditRateCard by remember { mutableStateOf(false) }
    var showEditRekening by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updatePhotoUri(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi Profil MC", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Header Avatar & Info Utama Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
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
                                    MCJobAvatarImage(
                                        photoUri = p.photoUri,
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
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = "Ubah Foto",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            TextButton(onClick = { photoLauncher.launch("image/*") }) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah Foto Profil MC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = p.stageName?.ifBlank { null } ?: p.name?.ifBlank { null } ?: "MC Professional",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (!p.bio.isNullOrBlank()) {
                                Text(
                                    text = p.bio!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatBox(value = stats.totalEvents.toString(), label = "Total Event", modifier = Modifier.weight(0.8f))
                                StatBox(value = stats.totalClients.toString(), label = "Total Klien", modifier = Modifier.weight(0.8f))
                                StatBox(value = Formatter.formatCurrency(stats.totalRevenue), label = "Total Omset", modifier = Modifier.weight(1.4f), color = Success)
                            }
                        }
                    }
                }

                // 2. Jika profil BELUM selesai/lengkap, tampilkan Banner Setup Wizard
                if (!p.profileCompleted) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToWizard() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.5.dp, Primary.copy(alpha = 0.3f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Primary,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoFixHigh,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Profil MC Belum Lengkap",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Primary
                                        )
                                        Text(
                                            text = "Klik di sini untuk melengkapi data via Wizard Setup",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Primary)
                                }
                            }
                        }
                    }
                }

                // 3. PREVIEW CARD 1: IDENTITAS & BRANDING MC
                item {
                    ProfilePreviewCard(
                        title = "IDENTITAS & BRANDING MC",
                        icon = Icons.Default.Badge,
                        onEditClick = { showEditIdentitas = true }
                    ) {
                        ProfilePreviewRow(label = "Nama Lengkap", value = p.name)
                        ProfilePreviewRow(label = "Nama Panggung", value = p.stageName)
                        ProfilePreviewRow(label = "WhatsApp Utama", value = p.phoneNumber)
                        ProfilePreviewRow(label = "WhatsApp Manager", value = p.secondaryPhone)
                        ProfilePreviewRow(label = "Email", value = p.email)
                        ProfilePreviewRow(label = "Instagram", value = p.instagramHandle)
                    }
                }

                // 4. PREVIEW CARD 2: PORTFOLIO & SPESIALISASI
                item {
                    ProfilePreviewCard(
                        title = "PORTFOLIO & SPESIALISASI",
                        icon = Icons.Default.Stars,
                        onEditClick = { showEditPortofolio = true }
                    ) {
                        ProfilePreviewRow(label = "Bio Tagline", value = p.bio)
                        ProfilePreviewRow(label = "Domisili Utama", value = p.city)
                        ProfilePreviewRow(label = "Cakupan Area", value = p.areaCoverage)
                        ProfilePreviewRow(label = "Spesialisasi Event", value = p.specialization)
                        ProfilePreviewRow(label = "Bahasa Pengantar", value = p.languages)
                        ProfilePreviewRow(label = "Jam Terbang / Pengalaman", value = p.experienceYears)
                    }
                }

                // 5. PREVIEW CARD 3: RATE CARD & KEBIJAKAN FINANSIAL
                item {
                    ProfilePreviewCard(
                        title = "RATE CARD & KEBIJAKAN",
                        icon = Icons.Default.Payments,
                        onEditClick = { showEditRateCard = true }
                    ) {
                        ProfilePreviewRow(
                            label = "Tarif Dasar (Honor Acuan)",
                            value = if (p.baseFee > 0) Formatter.formatCurrency(p.baseFee) else null
                        )
                        ProfilePreviewRow(
                            label = "Ketentuan DP Minimum",
                            value = "DP ${p.defaultDpPercentage}%"
                        )
                        ProfilePreviewRow(label = "Nomor NPWP", value = p.npwpNumber)
                    }
                }

                // 6. PREVIEW CARD 4: REKENING PEMBAYARAN & SK
                item {
                    ProfilePreviewCard(
                        title = "REKENING PEMBAYARAN",
                        icon = Icons.Default.AccountBalance,
                        onEditClick = { showEditRekening = true }
                    ) {
                        ProfilePreviewRow(label = "Nama Bank", value = p.bankName)
                        ProfilePreviewRow(label = "Nomor Rekening", value = p.accountNumber)
                        ProfilePreviewRow(label = "Pemilik Rekening", value = p.accountName)
                        ProfilePreviewRow(label = "Rekening Sekunder / QRIS", value = p.secondaryBankInfo)
                        ProfilePreviewRow(label = "Syarat & Ketentuan (T&C)", value = p.termsAndConditions)
                    }
                }

                // 7. Danger Zone - Hapus Akun Permanen
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clickable { showDeleteAccountDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Error.copy(alpha = 0.08f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "Hapus Akun",
                                        tint = Error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hapus Akun MCJOB.id Permanen",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Error
                                )
                                Text(
                                    text = "Hapus profil, agenda, invoice & data keuangan permanen.",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Error.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // --- EDIT MODAL DIALOGS ---

            if (showEditIdentitas) {
                EditIdentitasDialog(
                    currentProfile = p,
                    onSave = { name, stage, phone, secPhone, email, ig ->
                        viewModel.saveProfile(
                            name = name,
                            stageName = stage,
                            bio = p.bio ?: "",
                            city = p.city ?: "",
                            areaCoverage = p.areaCoverage,
                            specialization = p.specialization ?: "",
                            languages = p.languages,
                            experienceYears = p.experienceYears,
                            phone = phone,
                            secondaryPhone = secPhone,
                            email = email,
                            bankName = p.bankName ?: "",
                            accountNumber = p.accountNumber ?: "",
                            accountName = p.accountName ?: "",
                            secondaryBankInfo = p.secondaryBankInfo,
                            baseFee = p.baseFee,
                            defaultDpPercentage = p.defaultDpPercentage,
                            npwpNumber = p.npwpNumber,
                            instagramHandle = ig,
                            termsAndConditions = p.termsAndConditions
                        )
                        showEditIdentitas = false
                    },
                    onDismiss = { showEditIdentitas = false }
                )
            }

            if (showEditPortofolio) {
                EditPortofolioDialog(
                    currentProfile = p,
                    onSave = { bio, city, area, spec, lang, exp ->
                        viewModel.saveProfile(
                            name = p.name ?: "",
                            stageName = p.stageName,
                            bio = bio,
                            city = city,
                            areaCoverage = area,
                            specialization = spec,
                            languages = lang,
                            experienceYears = exp,
                            phone = p.phoneNumber ?: "",
                            secondaryPhone = p.secondaryPhone,
                            email = p.email ?: "",
                            bankName = p.bankName ?: "",
                            accountNumber = p.accountNumber ?: "",
                            accountName = p.accountName ?: "",
                            secondaryBankInfo = p.secondaryBankInfo,
                            baseFee = p.baseFee,
                            defaultDpPercentage = p.defaultDpPercentage,
                            npwpNumber = p.npwpNumber,
                            instagramHandle = p.instagramHandle,
                            termsAndConditions = p.termsAndConditions
                        )
                        showEditPortofolio = false
                    },
                    onDismiss = { showEditPortofolio = false }
                )
            }

            if (showEditRateCard) {
                EditRateCardDialog(
                    currentProfile = p,
                    onSave = { fee, dpPct, npwp ->
                        viewModel.saveProfile(
                            name = p.name ?: "",
                            stageName = p.stageName,
                            bio = p.bio ?: "",
                            city = p.city ?: "",
                            areaCoverage = p.areaCoverage,
                            specialization = p.specialization ?: "",
                            languages = p.languages,
                            experienceYears = p.experienceYears,
                            phone = p.phoneNumber ?: "",
                            secondaryPhone = p.secondaryPhone,
                            email = p.email ?: "",
                            bankName = p.bankName ?: "",
                            accountNumber = p.accountNumber ?: "",
                            accountName = p.accountName ?: "",
                            secondaryBankInfo = p.secondaryBankInfo,
                            baseFee = fee,
                            defaultDpPercentage = dpPct,
                            npwpNumber = npwp,
                            instagramHandle = p.instagramHandle,
                            termsAndConditions = p.termsAndConditions
                        )
                        showEditRateCard = false
                    },
                    onDismiss = { showEditRateCard = false }
                )
            }

            if (showEditRekening) {
                EditRekeningDialog(
                    currentProfile = p,
                    onSave = { bank, accNum, accName, secBank, terms ->
                        viewModel.saveProfile(
                            name = p.name ?: "",
                            stageName = p.stageName,
                            bio = p.bio ?: "",
                            city = p.city ?: "",
                            areaCoverage = p.areaCoverage,
                            specialization = p.specialization ?: "",
                            languages = p.languages,
                            experienceYears = p.experienceYears,
                            phone = p.phoneNumber ?: "",
                            secondaryPhone = p.secondaryPhone,
                            email = p.email ?: "",
                            bankName = bank,
                            accountNumber = accNum,
                            accountName = accName,
                            secondaryBankInfo = secBank,
                            baseFee = p.baseFee,
                            defaultDpPercentage = p.defaultDpPercentage,
                            npwpNumber = p.npwpNumber,
                            instagramHandle = p.instagramHandle,
                            termsAndConditions = terms
                        )
                        showEditRekening = false
                    },
                    onDismiss = { showEditRekening = false }
                )
            }

            if (showDeleteAccountDialog) {
                MCJobAccountDeleteDialog(
                    validationPhrase = "HAPUS AKUN SAYA",
                    onConfirm = { viewModel.deleteAccountPermanently() },
                    onDismiss = { showDeleteAccountDialog = false }
                )
            }

            if (isDeleting) {
                MCJobLoadingDialog(message = "Sedang menghapus akun Anda...")
            }

            statusMessage?.let { msg ->
                MCJobSuccessDialog(
                    title = "Data Berhasil Disimpan",
                    description = msg,
                    primaryCtaText = "Selesai",
                    secondaryCtaText = null,
                    onPrimary = { viewModel.clearStatusMessage() },
                    onDismiss = { viewModel.clearStatusMessage() }
                )
            }

            deletionError?.let { error ->
                MCJobErrorDialog(
                    title = "Gagal Menghapus Akun",
                    description = error,
                    primaryCtaText = "OK",
                    onRetry = { viewModel.clearDeletionError() }
                ) { viewModel.clearDeletionError() }
            }
        }
    }
}

// --- PREVIEW COMPONENTS ---

@Composable
fun ProfilePreviewCard(
    title: String,
    icon: ImageVector,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Edit $title",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            content()
        }
    }
}

@Composable
fun ProfilePreviewRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    val displayValue = if (value.isNullOrBlank()) "Belum diisi" else value
    val isPlaceholder = value.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Text(
            text = displayValue,
            fontSize = 13.sp,
            fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold,
            color = if (isPlaceholder) Color(0xFF94A3B8) else Color(0xFF1E293B)
        )
    }
}

// --- SECTION EDIT DIALOGS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIdentitasDialog(
    currentProfile: UserProfile,
    onSave: (name: String, stage: String, phone: String, secPhone: String, email: String, ig: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.name ?: "") }
    var stage by remember { mutableStateOf(currentProfile.stageName ?: "") }
    var phone by remember { mutableStateOf(currentProfile.phoneNumber ?: "") }
    var secPhone by remember { mutableStateOf(currentProfile.secondaryPhone ?: "") }
    var email by remember { mutableStateOf(currentProfile.email ?: "") }
    var ig by remember { mutableStateOf(currentProfile.instagramHandle ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Identitas & Branding", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MCJobTextField(value = name, onValueChange = { name = it }, label = "Nama Lengkap", isRequired = true)
                MCJobTextField(value = stage, onValueChange = { stage = it }, label = "Nama Panggung")
                MCJobTextField(value = phone, onValueChange = { phone = it }, label = "WhatsApp Utama", isRequired = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                MCJobTextField(value = secPhone, onValueChange = { secPhone = it }, label = "WhatsApp Manager", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                MCJobTextField(value = email, onValueChange = { email = it }, label = "Email", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                MCJobTextField(value = ig, onValueChange = { ig = it }, label = "Instagram Handle")
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, stage, phone, secPhone, email, ig) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPortofolioDialog(
    currentProfile: UserProfile,
    onSave: (bio: String, city: String, area: String, spec: String, lang: String, exp: String) -> Unit,
    onDismiss: () -> Unit
) {
    var bio by remember { mutableStateOf(currentProfile.bio ?: "") }
    var city by remember { mutableStateOf(currentProfile.city ?: "") }
    var area by remember { mutableStateOf(currentProfile.areaCoverage ?: "") }
    var spec by remember { mutableStateOf(currentProfile.specialization ?: "") }
    var lang by remember { mutableStateOf(currentProfile.languages ?: "") }
    var exp by remember { mutableStateOf(currentProfile.experienceYears ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Portofolio & Spesialisasi", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MCJobTextField(value = bio, onValueChange = { bio = it }, label = "Bio Tagline", singleLine = false, minLines = 2)
                MCJobTextField(value = city, onValueChange = { city = it }, label = "Domisili Utama", isRequired = true)
                MCJobTextField(value = area, onValueChange = { area = it }, label = "Cakupan Area")
                MCJobTextField(value = spec, onValueChange = { spec = it }, label = "Spesialisasi Event")
                MCJobTextField(value = lang, onValueChange = { lang = it }, label = "Bahasa Pengantar")
                MCJobTextField(value = exp, onValueChange = { exp = it }, label = "Jam Terbang / Pengalaman")
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(bio, city, area, spec, lang, exp) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRateCardDialog(
    currentProfile: UserProfile,
    onSave: (baseFee: Long, defaultDp: Int, npwp: String) -> Unit,
    onDismiss: () -> Unit
) {
    var baseFeeText by remember { mutableStateOf(if (currentProfile.baseFee > 0) currentProfile.baseFee.toString() else "") }
    var defaultDpText by remember { mutableStateOf(currentProfile.defaultDpPercentage.toString()) }
    var npwp by remember { mutableStateOf(currentProfile.npwpNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Rate Card & Kebijakan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MCJobTextField(
                    value = baseFeeText,
                    onValueChange = { baseFeeText = it.filter { c -> c.isDigit() } },
                    label = "Tarif Dasar (Rp)",
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                MCJobTextField(
                    value = defaultDpText,
                    onValueChange = { defaultDpText = it.filter { c -> c.isDigit() } },
                    label = "Ketentuan DP Minimum (%)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                MCJobTextField(
                    value = npwp,
                    onValueChange = { npwp = it },
                    label = "Nomor NPWP",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = baseFeeText.toLongOrNull() ?: 0L
                    val dpPct = defaultDpText.toIntOrNull() ?: 30
                    onSave(fee, dpPct, npwp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRekeningDialog(
    currentProfile: UserProfile,
    onSave: (bank: String, accNum: String, accName: String, secBank: String, terms: String) -> Unit,
    onDismiss: () -> Unit
) {
    var bank by remember { mutableStateOf(currentProfile.bankName ?: "") }
    var accNum by remember { mutableStateOf(currentProfile.accountNumber ?: "") }
    var accName by remember { mutableStateOf(currentProfile.accountName ?: "") }
    var secBank by remember { mutableStateOf(currentProfile.secondaryBankInfo ?: "") }
    var terms by remember { mutableStateOf(currentProfile.termsAndConditions ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Rekening & Syarat Ketentuan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MCJobTextField(value = bank, onValueChange = { bank = it }, label = "Nama Bank", isRequired = true)
                MCJobTextField(value = accNum, onValueChange = { accNum = it }, label = "Nomor Rekening", isRequired = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                MCJobTextField(value = accName, onValueChange = { accName = it }, label = "Pemilik Rekening", isRequired = true)
                MCJobTextField(value = secBank, onValueChange = { secBank = it }, label = "Rekening Sekunder / QRIS")
                MCJobTextField(value = terms, onValueChange = { terms = it }, label = "Syarat & Ketentuan (T&C)", singleLine = false, minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(bank, accNum, accName, secBank, terms) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Primary
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
