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
        icon = Icons.Default.Checkroom,
        title = "Dijital Dolabın",
        description = "Tüm kıyafetlerini fotoğraflayarak\ndigital ortamda düzenle.\nHer an, her yerden eriş!",
        accentColor = Color(0xFF2196F3)
    ),
    OnboardingPage(
        icon = Icons.Default.QrCodeScanner,
        title = "Barkod ile Ekle",
        description = "Kıyafetinin barkodunu okut,\ntüm bilgiler otomatik gelsin.\nYa da fotoğraf çekip manuel ekle!",
        accentColor = Color(0xFF4CAF50)
    ),
    OnboardingPage(
        icon = Icons.Default.WbSunny,
        title = "AI Kombin Asistanı",
        description = "Yapay zeka, hava durumunu analiz edip\nsenin dolabından kombin önerir.\nArtık \"ne giysem?\" derdi yok!",
        accentColor = Color(0xFFFF9800)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val totalPages = onboardingPages.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val scope = rememberCoroutineScope()
    val isSignInPage = pagerState.currentPage == totalPages - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Grey900, SurfaceDark)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true
        ) { page ->
            if (page < onboardingPages.size) {
                OnboardingPageContent(page = onboardingPages[page])
            } else {
                SignInBenefitsPage()
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalPages) { index ->
                    val isActive = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        label = "dot_width"
                    )
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (isActive) PrimaryLight else Grey600)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            if (isSignInPage) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Grey900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Google ile Giriş Yap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Devam", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
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
        Text(page.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Grey100, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(page.description, style = MaterialTheme.typography.bodyLarge, color = Grey400, textAlign = TextAlign.Center, lineHeight = 26.sp)
    }
}

@Composable
private fun SignInBenefitsPage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = PrimaryLight.copy(alpha = 0.12f), modifier = Modifier.size(120.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Lock, null, Modifier.size(52.dp), tint = PrimaryLight)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Hesabınla Daha Güçlü", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Grey100, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Google hesabınla giriş yaparak\ntüm cihazlardan gardırobuna eriş", style = MaterialTheme.typography.bodyMedium, color = Grey400, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(Modifier.height(32.dp))
        listOf(
            Triple(Icons.Default.Sync, "Cihazlar Arası Senkronizasyon", "Telefon değiştirsen de tüm kıyafetlerin burada"),
            Triple(Icons.Default.CloudDone, "Otomatik Yedekleme", "Verilerini kaybetme riski sıfır"),
            Triple(Icons.Default.Share, "Kombin Paylaşımı", "Kombinlerini arkadaşlarınla paylaş"),
            Triple(Icons.Default.Security, "Güvenli & Gizli", "Verilerini yalnızca sen görürsün")
        ).forEach { (icon, title, desc) ->
            BenefitRow(icon = icon, title = title, description = desc)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, title: String, description: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = Grey800.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryLight.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(20.dp), tint = PrimaryLight) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Grey100)
                Text(description, style = MaterialTheme.typography.labelSmall, color = Grey400, lineHeight = 16.sp)
            }
        }
    }
}