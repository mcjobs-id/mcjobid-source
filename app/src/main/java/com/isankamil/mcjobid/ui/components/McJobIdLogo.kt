package com.isankamil.mcjobid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.isankamil.mcjobid.ui.theme.Primary

@Composable
fun McJobIdLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    showWordmark: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(iconSize),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "file:///android_asset/logo.png",
                    contentDescription = "MCJOBID Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(iconSize * 0.85f)
                )
            }
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "mcjob",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = (iconSize.value * 0.45).sp
                    )
                    Text(
                        text = ".id",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = (iconSize.value * 0.45).sp
                    )
                }
                Text(
                    text = "Powered by @careermc.academy",
                    fontSize = (iconSize.value * 0.22).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }
}
