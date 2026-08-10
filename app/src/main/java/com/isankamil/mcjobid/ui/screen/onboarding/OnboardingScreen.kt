package com.isankamil.mcjobid.ui.screen.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.isankamil.mcjobid.ui.components.McJobIdLogo
import com.isankamil.mcjobid.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

data class OnboardingPageData(
    val badge: String,
    val badgeColor: Color,
    val title: String,
    val description: String,
    val imageRes: String
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPageData(
        badge = "JADWAL PRESISE",
        badgeColor = Color(0xFF3B82F6),
        title = "Kelola Agenda MC Profesional",
        description = "Pencatatan lengkap jadwal acara, lokasi venue, jam gladi bersih, dresscode, dan kontak PIC WO terintegrasi.",
        imageRes = "file:///android_asset/images/slide1.png"
    ),
    OnboardingPageData(
        badge = "ANTI DOUBLE BOOKING",
        badgeColor = Color(0xFFEF4444),
        title = "Deteksi Bentrok Jadwal Real-Time",
        description = "Sistem otomatis mendeteksi jika terdapat dua job di jam dan tanggal yang sama untuk mencegah kesalahan komitmen.",
        imageRes = "file:///android_asset/images/slide2.png"
    ),
    OnboardingPageData(
        badge = "STANDAR INDUSTRI",
        badgeColor = Color(0xFF8B5CF6),
        title = "Invoice PDF & Kontrak Instan",
        description = "Cetak Invoice DP, Invoice Pelunasan, dan Kontrak Penawaran ber-QR Code profesional hanya dalam 1-tap.",
        imageRes = "file:///android_asset/images/slide3.png"
    ),
    OnboardingPageData(
        badge = "FINANCIAL CONTROL",
        badgeColor = Color(0xFF10B981),
        title = "Pelacakan DP & Piutang Klien",
        description = "Pantau omset bulanan, status DP 30%/50%, sisa pelunasan H-1 acara, dan riwayat pembayaran per klien.",
        imageRes = "file:///android_asset/images/slide4.png"
    ),
    OnboardingPageData(
        badge = "HAK CIPTA & LISENSI",
        badgeColor = Color(0xFFEF4444),
        title = "Info Penting & Peringatan",
        description = "Aplikasi khusus pencatatan job dan keuangan khusus para MC Professional yang aman dan rahasia",
        imageRes = "file:///android_asset/images/slide6.png"
    )
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pages = ONBOARDING_PAGES

    val pagerState = rememberPagerState(pageCount = { pages.size })

    val isLastPage by remember {
        derivedStateOf { pagerState.currentPage == pages.size - 1 }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Logo & Lewati Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    McJobIdLogo(iconSize = 44.dp)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onComplete()
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Lewati", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Pager Feature Content (6 Slides) with hardware-accelerated transformations and pre-rendering
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    val page = pages[pageIndex]

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .graphicsLayer {
                                val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                                val pageScale = lerp(0.92f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                val pageAlpha = lerp(0.4f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                                scaleX = pageScale
                                scaleY = pageScale
                                alpha = pageAlpha
                            }
                    ) {
                        if (pageIndex == pages.size - 1) {
                            // Custom Slide 5: Light Premium Security & Legal Warning Card
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 8.dp,
                                color = Color.White,
                                modifier = Modifier
                                    .width(340.dp)
                                    .height(260.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFFECDD3),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFFFFF1F2), Color(0xFFFFFFFF))
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Header Row: Warning Badge + Shield Icon
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFFFE4E6),
                                                border = BorderStroke(1.dp, Color(0xFFFDA4AF))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = Color(0xFFE11D48),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "INFO PENTING & PERINGATAN",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFBE123C),
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFEF3C7),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Security,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Legal Warning Body Text
                                        Text(
                                            text = buildAnnotatedString {
                                                append("Aplikasi ini adalah kekayaan intelektual eksklusif milik ")
                                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFFBE123C))) {
                                                    append("Career MC Academy")
                                                }
                                                append(". Segala bentuk pembajakan, penggandaan, penyebaran akses, atau komersialisasi tanpa izin tertulis akan kami urus dan tindak tegas ke ranah hukum sesuai ")
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF9F1239))) {
                                                    append("Pasal 40 ayat (1) UU Hak Cipta (UUHC)")
                                                }
                                                append(".")
                                            },
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155),
                                            lineHeight = 17.sp
                                        )

                                        // Bottom Footer Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Lisensi Resmi • Powered by @careermc.academy",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = page.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        } else {
                            // Standard Onboarding Feature Page
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 12.dp,
                                color = Color.White,
                                modifier = Modifier
                                    .width(340.dp)
                                    .height(260.dp)
                            ) {
                                AsyncImage(
                                    model = page.imageRes,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = page.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }

                // Bottom Navigation & Animated Pager Indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Page Indicator Dots Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        pages.indices.forEach { index ->
                            val isSelected = pagerState.targetPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 22.dp else 7.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "onboardingDotWidth"
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Primary else Color(0xFFCBD5E1),
                                modifier = Modifier.size(width, 7.dp)
                            ) {}
                        }
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = if (isLastPage) "Mulai Setup Profil MC" else "Lanjutkan",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

