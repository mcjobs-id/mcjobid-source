package com.isankamil.mcjobid.ui.components.feedback

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.theme.Error
import com.isankamil.mcjobid.ui.theme.Info
import com.isankamil.mcjobid.ui.theme.Primary
import com.isankamil.mcjobid.ui.theme.Success
import com.isankamil.mcjobid.ui.theme.Warning

@Composable
fun MCJobSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        val isUndo = data.visuals.actionLabel != null
        val containerColor = Color(0xFF1E293B)
        val contentColor = Color.White

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = containerColor,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.padding(end = 10.dp)
                )

                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )

                data.visuals.actionLabel?.let { label ->
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { data.performAction() }
                    ) {
                        Text(
                            text = label,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
