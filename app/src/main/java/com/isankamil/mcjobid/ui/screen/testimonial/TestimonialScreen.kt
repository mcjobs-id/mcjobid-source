package com.isankamil.mcjobid.ui.screen.testimonial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.isankamil.mcjobid.domain.model.Testimonial
import com.isankamil.mcjobid.ui.theme.Background
import com.isankamil.mcjobid.ui.theme.OnSurface
import com.isankamil.mcjobid.ui.theme.OnSurfaceVariant
import com.isankamil.mcjobid.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Palet avatar warna-warni natural
private val avatarColors = listOf(
    Color(0xFF4F46E5), // Indigo (Primary)
    Color(0xFF7C3AED), // Violet
    Color(0xFFDB2777), // Pink
    Color(0xFFD97706), // Amber
    Color(0xFF059669), // Emerald
    Color(0xFF0284C7), // Sky
    Color(0xFFDC2626), // Red
    Color(0xFF0891B2), // Cyan
    Color(0xFF65A30D), // Lime
    Color(0xFF9333EA), // Purple
    Color(0xFFF59E0B), // Yellow
    Color(0xFF16A34A), // Green
)

private fun avatarColorFor(name: String): Color {
    val hash = name.hashCode()
    return avatarColors[Math.abs(hash) % avatarColors.size]
}

private fun avatarInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        name.startsWith("PT.") || name.startsWith("CV.") -> {
            val nonPT = parts.drop(1)
            if (nonPT.size >= 2) "${nonPT[0].first().uppercaseChar()}${nonPT[1].first().uppercaseChar()}"
            else if (nonPT.isNotEmpty()) "${nonPT[0].first().uppercaseChar()}"
            else "PT"
        }
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.size == 1 && parts[0].isNotEmpty() -> "${parts[0].first().uppercaseChar()}"
        else -> "?"
    }
}

private fun isPT(name: String) = name.startsWith("PT.") || name.startsWith("CV.")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestimonialScreen(
    viewModel: TestimonialViewModel,
    onBackClick: () -> Unit
) {
    val testimonials by viewModel.testimonials.collectAsState()
    val myTestimonial by viewModel.myTestimonial.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val currentUserId = viewModel.currentUserId
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val submitStatus by viewModel.submitStatus.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(submitStatus) {
        submitStatus?.let { result ->
            showDialog = false // Tutup dialog secara otomatis
            if (result.isSuccess) {
                snackbarHostState.showSnackbar("Testimoni Anda berhasil dikirim & dipublikasikan! 🎉")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: ""
                if (errorMsg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    snackbarHostState.showSnackbar("Ulasan tersimpan lokal. Perbarui aturan Firestore Rules di Firebase Console agar live untuk semua user.")
                } else {
                    snackbarHostState.showSnackbar("Gagal mengirim testimoni: $errorMsg")
                }
            }
            viewModel.clearSubmitStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Testimoni & Saran MC", fontWeight = FontWeight.Bold)
                        Text("Ulasan Publik Komunitas MC Indonesia", fontSize = 12.sp, color = OnSurfaceVariant)
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
            if (myTestimonial != null) {
                ExtendedFloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Edit Ulasan Saya") },
                    text = { Text("Edit Ulasan Saya ✍️", fontWeight = FontWeight.Bold) }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Tulis Ulasan") },
                    text = { Text("Tulis Testimoni 🚀", fontWeight = FontWeight.Bold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            if (isLoading && testimonials.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
            } else if (errorMessage != null && testimonials.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.loadTestimonials() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Coba Lagi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Ulasan & Masukan Pengguna",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                            Text(
                                text = "Pengalaman nyata dari para MC profesional se-Indonesia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    items(testimonials, key = { it.id }) { testimonial ->
                        val isOwn = currentUserId.isNotBlank() && testimonial.userId == currentUserId
                        TestimonialItem(
                            testimonial = testimonial,
                            isOwn = isOwn,
                            onEdit = if (isOwn) { { showDialog = true } } else null
                        )
                    }
                }
            }
        }

        if (showDialog) {
            TestimonialDialog(
                onDismiss = { showDialog = false },
                onSubmit = { name, rating, review, suggestion, photoUri ->
                    viewModel.submitTestimonial(
                        userName = name,
                        rating = rating,
                        review = review,
                        suggestion = suggestion,
                        customPhotoUrl = photoUri
                    )
                },
                isLoading = isLoading,
                existingTestimonial = myTestimonial,
                userProfile = currentUserProfile
            )
        }
    }
}

@Composable
fun TestimonialItem(
    testimonial: Testimonial,
    isOwn: Boolean = false,
    onEdit: (() -> Unit)? = null
) {
    val name = testimonial.userName
    val isCompany = isPT(name)
    val initials = avatarInitials(name)
    val avatarColor = avatarColorFor(name)
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale.forLanguageTag("id-ID"))
        .format(Date(testimonial.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isOwn && onEdit != null) Modifier.clickable { onEdit() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwn) Color(0xFFF8FAFC) else Color.White
        ),
        border = if (isOwn) BorderStroke(1.5.dp, Primary.copy(alpha = 0.4f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOwn) 3.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Avatar + Name + Rating + Own Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                val photoUrl = testimonial.photoUrl
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                    )
                } else if (testimonial.avatarResId != null) {
                    Image(
                        painter = painterResource(id = testimonial.avatarResId),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = avatarColor,
                        modifier = Modifier.size(46.dp)
                    ) {
                        if (isCompany) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = initials,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = OnSurface,
                            maxLines = 1
                        )
                        if (isOwn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Ulasan Anda ✨",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = OnSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star Rating
                    Row {
                        repeat(testimonial.rating) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        repeat(5 - testimonial.rating) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    if (isOwn && onEdit != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Ulasan",
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = testimonial.review,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1E293B),
                lineHeight = 20.sp
            )

            if (testimonial.suggestion.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = testimonial.suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
