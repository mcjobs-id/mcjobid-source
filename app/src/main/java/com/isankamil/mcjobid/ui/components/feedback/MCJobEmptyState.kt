package com.isankamil.mcjobid.ui.components.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.isankamil.mcjobid.ui.components.EmptyStateView

@Composable
fun MCJobEmptyState(
    title: String,
    description: String,
    icon: ImageVector,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = icon,
        title = title,
        description = description,
        actionText = actionText,
        onActionClick = onActionClick,
        modifier = modifier
    )
}
