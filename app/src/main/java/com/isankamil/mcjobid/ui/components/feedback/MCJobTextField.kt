package com.isankamil.mcjobid.ui.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.theme.Error
import com.isankamil.mcjobid.ui.theme.OnSurfaceVariant
import com.isankamil.mcjobid.ui.theme.Primary

@Composable
fun MCJobInlineError(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !errorMessage.isNullOrBlank(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (!errorMessage.isNullOrBlank()) {
            Row(
                modifier = modifier.padding(top = 4.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    color = Error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MCJobTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    errorMessage: String? = null,
    placeholder: String = "",
    tooltipText: String? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val isError = !errorMessage.isNullOrBlank()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) Error else OnSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (!tooltipText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    MCJobInfoTooltip(tooltipText = tooltipText)
                }
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = if (singleLine) 1 else 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            },
            isError = isError,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A)
            ),
            modifier = if (singleLine) {
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 84.dp)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                errorBorderColor = Error,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Error.copy(alpha = 0.04f),
                disabledContainerColor = Color(0xFFF8FAFC),
                disabledBorderColor = Color(0xFFE2E8F0),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = Primary,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )

        MCJobInlineError(errorMessage = errorMessage)
    }
}

