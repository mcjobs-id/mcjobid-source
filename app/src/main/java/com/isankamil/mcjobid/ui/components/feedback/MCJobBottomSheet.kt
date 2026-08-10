package com.isankamil.mcjobid.ui.components.feedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.theme.Error
import com.isankamil.mcjobid.ui.theme.Primary
import com.isankamil.mcjobid.ui.theme.Success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MCJobBottomSheet(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
            content()
        }
    }
}

// WHATSAPP MESSAGE PREVIEW SHEET
@Composable
fun MCJobWhatsAppPreviewSheet(
    clientName: String,
    phone: String,
    initialMessage: String,
    onSend: (message: String) -> Unit,
    onDismiss: () -> Unit
) {
    var editableMessage by remember { mutableStateOf(initialMessage) }

    MCJobBottomSheet(
        onDismissRequest = onDismiss,
        title = "Kirim Pesan WhatsApp"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Success.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Penerima:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("$clientName ($phone)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                }
            }

            Text("Preview Pesan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))

            OutlinedTextField(
                value = editableMessage,
                onValueChange = { editableMessage = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 4,
                maxLines = 6
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MCJobPrimaryButton(
                    text = "Buka WhatsApp",
                    onClick = {
                        onSend(editableMessage)
                        onDismiss()
                    },
                    containerColor = Primary,
                    icon = Icons.AutoMirrored.Filled.Send,
                    modifier = Modifier.weight(1f)
                )
                MCJobSecondaryButton(
                    text = "Batal",
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}

// JOB DETAIL ACTION SHEET ("Kelola Job")
data class ActionSheetOption(
    val title: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun MCJobActionSheet(
    title: String = "Kelola Job",
    options: List<ActionSheetOption>,
    onDismiss: () -> Unit
) {
    MCJobBottomSheet(
        onDismissRequest = onDismiss,
        title = title
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                val tint = if (option.isDestructive) Error else Color(0xFF111827)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (option.isDestructive) Error.copy(alpha = 0.06f) else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            option.onClick()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option.title,
                            fontSize = 14.sp,
                            fontWeight = if (option.isDestructive) FontWeight.Bold else FontWeight.SemiBold,
                            color = tint
                        )
                    }
                }
            }
        }
    }
}
