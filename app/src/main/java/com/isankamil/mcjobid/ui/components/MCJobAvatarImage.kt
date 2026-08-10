package com.isankamil.mcjobid.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.isankamil.mcjobid.ui.theme.Primary
import java.io.File

@Composable
fun MCJobAvatarImage(
    photoUri: String?,
    contentDescription: String = "Foto Profil",
    modifier: Modifier = Modifier,
    fallbackTint: Color = Primary
) {
    val context = LocalContext.current
    val model = remember(photoUri) {
        if (photoUri.isNullOrBlank()) null
        else try {
            val uri = Uri.parse(photoUri)
            when {
                // HTTPS URL dari Firebase Storage — langsung pakai, tidak perlu cek file lokal
                uri.scheme == "https" || uri.scheme == "http" -> uri
                // file:// URI — validasi apakah file masih ada
                uri.scheme == "file" && !uri.path.isNullOrBlank() -> {
                    val file = File(uri.path!!)
                    if (file.exists() && file.length() > 0) file else null
                }
                // Path absolut lokal
                photoUri.startsWith("/") -> {
                    val file = File(photoUri)
                    if (file.exists() && file.length() > 0) file else null
                }
                // content:// URI atau lainnya
                else -> uri
            }
        } catch (_: Exception) {
            null
        }
    }


    if (model != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape),
            error = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = fallbackTint, modifier = Modifier.fillMaxSize(0.6f))
                }
            },
            loading = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.1f))) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize(0.4f), color = Primary, strokeWidth = 2.dp)
                }
            }
        )
    } else {
        Box(contentAlignment = Alignment.Center, modifier = modifier.background(Primary.copy(alpha = 0.1f))) {
            Icon(Icons.Default.Person, contentDescription = null, tint = fallbackTint, modifier = Modifier.fillMaxSize(0.6f))
        }
    }
}
