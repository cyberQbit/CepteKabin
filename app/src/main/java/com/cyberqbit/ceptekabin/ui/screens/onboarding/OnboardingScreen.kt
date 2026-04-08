package com.cyberqbit.ceptekabin.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberqbit.ceptekabin.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)

val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.QrCodeScanner,
        title = "Barkod ile Ekle",
        description = "Kıyafetinin barkodunu okut,\ntüm bilgiler otomatik gelsin.\nYa da fotoğraf çekip manuel ekle!",
        accentColor = Color(0xFF2196F3)
    ),
    OnboardingPage(
        icon = Icons.Default.Style,
        title = "Kombin Oluştur",
        description = "Dolabındaki kıyafetleri birleştir,\nharika kombinler yarat.\nRenk uyumu otomatik hesaplanır!",
        accentColor = Color(0xFF9C27B0)
    ),
    OnboardingPage(
        icon = Icons.Default.WbSunny,
        title = "Hava Durumuna Göre Giyin",
        description = "Yapay zeka, hava durumunu analiz edip\nsenin dolabından kombin önerir.\nArtık \"ne giysem?\" derdi yok!",
        accentColor = Color(0xFFFF9800)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                if (isDark) listOf(Grey900, SurfaceDark) else listOf(White, Grey100)
            ))
            .statusBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onComplete) {
                Text("Atla", color = if (isDark) Grey400 else Grey600)
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            OnboardingPageContent(page = onboardingPages[page], isDark = isDark)
        }

        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(onboardingPages.size) { index ->
                    val isActive = pagerState.currentPage == index
                    val width by animateDpAsState(targetValue = if (isActive) 24.dp else 8.dp)
                    Box(Modifier.height(8.dp).width(width).clip(CircleShape)
                        .background(if (isActive) PrimaryLight else if (isDark) Grey600 else Grey300))
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (isLastPage) onComplete()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isLastPage) "Başlayalım!" else "Devam",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = page.accentColor.copy(alpha = 0.12f), modifier = Modifier.size(140.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(page.icon, null, Modifier.size(64.dp), tint = page.accentColor)
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(page.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = if (isDark) Grey100 else Grey900, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(page.description, style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Grey400 else Grey600, textAlign = TextAlign.Center, lineHeight = 24.sp)
    }
}
