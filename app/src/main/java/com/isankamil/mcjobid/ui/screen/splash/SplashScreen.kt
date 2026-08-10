package com.isankamil.mcjobid.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.components.McJobIdLogo
import com.isankamil.mcjobid.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Single LaunchedEffect to guarantee smooth minimum splash duration and instant navigation
    LaunchedEffect(Unit) {
        delay(250)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            McJobIdLogo(iconSize = 72.dp, showWordmark = true)

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                color = Primary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = "Professional MC Management System",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}

