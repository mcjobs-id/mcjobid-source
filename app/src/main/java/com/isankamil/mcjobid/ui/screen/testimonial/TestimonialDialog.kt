package com.isankamil.mcjobid.ui.screen.testimonial

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.ui.components.feedback.MCJobTextField
import com.isankamil.mcjobid.ui.theme.Primary

private const val MAX_NAME_LENGTH = 60
private const val MAX_REVIEW_LENGTH = 500
private const val MAX_SUGGESTION_LENGTH = 300

@Composable
fun TestimonialDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, rating: Int, review: String, suggestion: String, photoUri: String?) -> Unit,
    isLoading: Boolean,
    existingTestimonial: Testimonial? = null,
    userProfile: UserProfile? = null
) {
    val initialName = existingTestimonial?.userName?.ifBlank { null }
        ?: userProfile?.displayName?.ifBlank { null }
        ?: userProfile?.stageName?.ifBlank { null }
        ?: ""

    val initialPhoto = existingTestimonial?.photoUrl
        ?: userProfile?.photoUrl
        ?: userProfile?.photoUri

    var name by remember { mutableStateOf(initialName) }
    var review by remember { mutableStateOf(existingTestimonial?.review ?: "") }
    var suggestion by remember { mutableStateOf(existingTestimonial?.suggestion ?: "") }
    var rating by remember { mutableStateOf(existingTestimonial?.rating ?: 5) }
    var selectedPhotoUri by remember { mutableStateOf<String?>(initialPhoto) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedPhotoUri = it.toString() }
    }

    val isNameOverLimit = name.length > MAX_NAME_LENGTH
    val isReviewOverLimit = review.length > MAX_REVIEW_LENGTH
    val isSuggestionOverLimit = suggestion.length > MAX_SUGGESTION_LENGTH
    val isAnyOverLimit = isNameOverLimit || isReviewOverLimit || isSuggestionOverLimit

    val isSubmitEnabled = !isLoading &&
            name.isNotBlank() &&
            review.isNotBlank() &&
            !isAnyOverLimit

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (existingTestimonial != null) "Edit Ulasan Anda" else "Tulis Testimoni MC",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (existingTestimonial != null)
                        "Perbarui ulasan, foto, dan penilaian pengalaman Anda."
                    else
                        "Bagikan testimoni & pengalaman profesional Anda bersama MCJob.ID.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Profile Photo Avatar Picker
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clickable { photoPickerLauncher.launch("image/*") }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.1f),
                            border = BorderStroke(2.dp, Primary),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (!selectedPhotoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = selectedPhotoUri,
                                    contentDescription = "Foto Profil Testimoni",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else if (existingTestimonial?.avatarResId != null) {
                                Image(
                                    painter = painterResource(id = existingTestimonial.avatarResId),
                                    contentDescription = "Avatar Testimoni",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(36.dp))
                                }
                            }
                        }

                        // Mini Camera Icon Badge
                        Surface(
                            shape = CircleShape,
                            color = Primary,
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Ganti Foto", tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    TextButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedPhotoUri.isNullOrBlank()) "Pasang Foto Profil" else "Ganti Foto Avatar",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                // Rating Stars Selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { i ->
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Rating $i Bintang",
                                tint = Color(0xFFFFB800),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { rating = i }
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                // Name field
                MCJobTextField(
                    value = name,
                    onValueChange = { if (it.length <= MAX_NAME_LENGTH + 10) name = it },
                    label = "Nama Lengkap / Nama MC",
                    isRequired = true,
                    placeholder = "Contoh: Kevin MC / PT Sinar Pangan",
                    errorMessage = if (isNameOverLimit) "Maksimal $MAX_NAME_LENGTH karakter" else null
                )

                // Review field
                MCJobTextField(
                    value = review,
                    onValueChange = { if (it.length <= MAX_REVIEW_LENGTH + 20) review = it },
                    label = "Ulasan & Pengalaman",
                    isRequired = true,
                    placeholder = "Tuliskan pengalaman Anda menggunakan MCJob.ID...",
                    singleLine = false,
                    minLines = 3,
                    errorMessage = if (isReviewOverLimit) "Maksimal $MAX_REVIEW_LENGTH karakter" else null
                )

                // Suggestion field
                MCJobTextField(
                    value = suggestion,
                    onValueChange = { if (it.length <= MAX_SUGGESTION_LENGTH + 20) suggestion = it },
                    label = "Saran Fitur / Perbaikan (Opsional)",
                    placeholder = "Masukan untuk fitur baru yang Anda butuhkan...",
                    singleLine = false,
                    minLines = 2,
                    errorMessage = if (isSuggestionOverLimit) "Maksimal $MAX_SUGGESTION_LENGTH karakter" else null
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isSubmitEnabled) {
                            onSubmit(name, rating, review, suggestion, selectedPhotoUri)
                        }
                    },
                    enabled = isSubmitEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (existingTestimonial != null) "Perbarui Testimoni 🚀" else "Kirim Testimoni 🚀",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
            }
        }
    )
}
