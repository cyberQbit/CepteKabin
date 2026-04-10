package com.cyberqbit.ceptekabin.ui.screens.home

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.domain.engine.SmartKombinSuggester
import com.cyberqbit.ceptekabin.domain.engine.WeatherOutfitEngine
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.components.ShimmerCard
import com.cyberqbit.ceptekabin.ui.components.TutorialOverlay
import com.cyberqbit.ceptekabin.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToDolap: () -> Unit,
    onNavigateToKombin: () -> Unit,
    onNavigateToTarama: () -> Unit,
    onNavigateToHavaDurumu: () -> Unit,
    onNavigateToKiyaket: (Long) -> Unit = {},
    onNavigateToKombinTakvim: () -> Unit = {},
    onNavigateToVirtualTryOn: () -> Unit = {},
    onNavigateToKombinOlustur: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val havaDurumu by viewModel.havaDurumu.collectAsState()
    val sonEklenenler by viewModel.sonEklenenler.collectAsState()
    val onerilenKombinler by viewModel.onerilenKombinler.collectAsState()
    val dolapIstatistikleri by viewModel.dolapIstatistikleri.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val havaDurumuYukleniyor by viewModel.havaDurumuYukleniyor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isDark = true
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        viewModel.checkAndShowSharePrompt()
        // Konum izni iste (ilk açılışta)
        if (!locationPermission.status.isGranted) locationPermission.launchPermissionRequest()
    }

    // İzin yeni verildiğinde: HavaDurumuScreen zaten staleness kontrolü yapacak
    // HomeScreen'den API çağrısı yapılmaz — cache reaktif flow'dan otomatik güncellenir

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                if (isDark) listOf(Grey900, Grey900.copy(alpha = 0.95f)) else listOf(Grey100, White)
            ))
            .padding(bottom = 96.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        WelcomeHeader(userName = userName, isDark = isDark)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            ShimmerCard(modifier = Modifier.fillMaxWidth().height(64.dp), isDark = isDark) {}
        } else {
            TodayWeatherBar(havaDurumu = havaDurumu, isLoading = havaDurumuYukleniyor, isDark = isDark, onClick = onNavigateToHavaDurumu)
        }
        Spacer(Modifier.height(20.dp))

        Text("Hızlı İşlemler", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(Icons.Default.QrCodeScanner, "Barkod Tara", "Ürün ekle",
                onNavigateToTarama, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.Style, "Kombin Yap", "Kıyafet kombinle",
                onNavigateToKombin, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.CalendarMonth, "Takvim", "Kombin planla",
                onNavigateToKombinTakvim, Modifier.weight(1f), isDark)
        }

        Spacer(Modifier.height(24.dp))

        DailyOutfitSection(
            oneriler = onerilenKombinler, havaDurumu = havaDurumu, isDark = isDark,
            dolapBos = dolapIstatistikleri.toplamKiyafet == 0,
            onNavigateToDolap = onNavigateToDolap,
            onNavigateToKombinOlustur = onNavigateToKombinOlustur,
            isLoading = isLoading
        )

        Spacer(Modifier.height(24.dp))

        if (dolapIstatistikleri.toplamKiyafet > 0 && !isLoading) {
            DolapStatsCard(stats = dolapIstatistikleri, isDark = isDark, onNavigateToDolap = onNavigateToDolap)
            Spacer(Modifier.height(24.dp))
        }

        if (sonEklenenler.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Son Eklenenler", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
                TextButton(onClick = onNavigateToDolap) { Text("Tümünü Gör") }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sonEklenenler) { kiyaket ->
                    RecentClothingCard(kiyaket = kiyaket, isDark = isDark, onClick = { onNavigateToKiyaket(kiyaket.id) })
                }
            }
        } else if (dolapIstatistikleri.toplamKiyafet == 0 && !isLoading) {
            // Show nothing if empty and not loading
        }
        Spacer(Modifier.height(24.dp))
    }

    // İlk açılışta uygulamayı tanıtan tutorial (sadece bir kez gösterilir)
    TutorialOverlay(isDark = isDark)
}

data class DolapIstatistikleri(
    val toplamKiyafet: Int = 0,
    val toplamKombin: Int = 0,
    val enCokGiyilen: String? = null,
    val kategoriler: Map<String, Int> = emptyMap()
)

@Composable
private fun WelcomeHeader(userName: String?, isDark: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = if (!userName.isNullOrBlank()) "Merhaba, $userName" else "Merhaba",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                color = if (isDark) Grey100 else Grey900)
            Text("Bugün ne giyeceksin?", style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Grey400 else Grey600)
        }
    }
}

@Composable
private fun TodayWeatherBar(havaDurumu: HavaDurumu?, isLoading: Boolean, isDark: Boolean, onClick: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() }) {
        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                Text("Hava durumu güncelleniyor...", style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Grey400 else Grey600)
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryLight)
            } else if (havaDurumu != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(havaDurumu.durum.toEmoji(), fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${havaDurumu.sicaklik.toInt()}°C", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
                            Spacer(Modifier.width(8.dp))
                            Text(havaDurumu.durum.displayName, style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Grey300 else Grey700)
                        }
                        val kisaOneri = WeatherOutfitEngine.getRecommendation(havaDurumu).kisaAciklama
                        Text(kisaOneri, style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Grey500 else Grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp),
                    tint = if (isDark) Grey500 else Grey400)
            } else {
                Text("Hava durumu alınamadı.", style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Grey400 else Grey600)
                Icon(Icons.Default.Refresh, null, tint = PrimaryLight)
            }
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, subtitle: String,
    onClick: () -> Unit, modifier: Modifier, isDark: Boolean) {
    GlassSurface(modifier = modifier.height(90.dp).cardPressEffect().clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
            Icon(icon, title, tint = PrimaryLight, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Grey400 else Grey600, maxLines = 1, fontSize = 10.sp)
        }
    }
}

@Composable
private fun DailyOutfitSection(oneriler: List<SmartKombinSuggester.KombinOnerisi>,
    havaDurumu: HavaDurumu?, isDark: Boolean, dolapBos: Boolean,
    onNavigateToDolap: () -> Unit, onNavigateToKombinOlustur: () -> Unit,
    isLoading: Boolean = false) {
    Text("Bugünkü Önerin", style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
    Spacer(Modifier.height(10.dp))

    if (dolapBos) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(36.dp), tint = AccentGold)
                Spacer(Modifier.height(10.dp))
                Text("Dolabına kıyafet ekleyerek\nkişisel öneriler almaya başla!",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                    color = if (isDark) Grey200 else Grey800, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onNavigateToDolap, colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                    Text("İlk Kıyafetini Ekle")
                }
            }
        }
    } else if (oneriler.isEmpty()) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbCloudy, null, Modifier.size(28.dp), tint = if (isDark) Grey500 else Grey400)
                Spacer(Modifier.width(12.dp))
                Text("Hava durumu yüklenince kombinler önerilecek",
                    style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey300 else Grey700)
            }
        }
    } else {
        SlideUpFadeIn(visible = !isLoading) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(oneriler) { oneri ->
                    SuggestionCard(oneri = oneri, isDark = isDark, onClick = onNavigateToKombinOlustur)
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(oneri: SmartKombinSuggester.KombinOnerisi, isDark: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.width(220.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Grey800.copy(alpha = 0.6f) else White.copy(alpha = 0.9f),
        shadowElevation = 4.dp) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp),
                    color = when {
                        oneri.puanDetay.toplamPuan >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        oneri.puanDetay.toplamPuan >= 60 -> AccentGold.copy(alpha = 0.15f)
                        else -> Color(0xFFFF9800).copy(alpha = 0.15f)
                    }) {
                    Text("${oneri.puanDetay.toplamPuan}%", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            oneri.puanDetay.toplamPuan >= 80 -> Color(0xFF4CAF50)
                            oneri.puanDetay.toplamPuan >= 60 -> AccentGold
                            else -> Color(0xFFFF9800)
                        }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("uyum", style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600)
            }
            Spacer(Modifier.height(12.dp))
            oneri.ustGiyim?.let { KiyafetSatir("Üst", "${it.marka} ${it.tur.displayName}", isDark) }
            oneri.altGiyim?.let { KiyafetSatir("Alt", "${it.marka} ${it.tur.displayName}", isDark) }
            oneri.disGiyim?.let { KiyafetSatir("Dış", "${it.marka} ${it.tur.displayName}", isDark) }
            oneri.ayakkabi?.let { KiyafetSatir("Ayak", "${it.marka} ${it.tur.displayName}", isDark) }
            Spacer(Modifier.height(10.dp))
            Text(oneri.aciklama, style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Grey400 else Grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun KiyafetSatir(label: String, text: String, isDark: Boolean) {
    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryLight,
            modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Grey200 else Grey800, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DolapStatsCard(stats: DolapIstatistikleri, isDark: Boolean, onNavigateToDolap: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToDolap)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(stats.toplamKiyafet.toString(), "Kıyafet", Icons.Default.Checkroom, isDark)
            StatItem(stats.toplamKombin.toString(), "Kombin", Icons.Default.Style, isDark)
            stats.enCokGiyilen?.let { StatItem(it, "En çok giyilen", Icons.Default.Favorite, isDark, true) }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, icon: ImageVector, isDark: Boolean, isText: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, Modifier.size(20.dp), tint = PrimaryLight)
        Spacer(Modifier.height(4.dp))
        Text(value, style = if (isText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600)
    }
}

@Composable
private fun RecentClothingCard(kiyaket: Kiyaket, isDark: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.width(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Grey800.copy(alpha = 0.5f) else White.copy(alpha = 0.9f),
        shadowElevation = 2.dp) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(10.dp))
                .background(if (isDark) Grey700 else Grey200), contentAlignment = Alignment.Center) {
                if (kiyaket.imageUrl != null) {
                    AsyncImage(model = kiyaket.imageUrl, contentDescription = kiyaket.tur.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
                } else {
                    Icon(Icons.Default.Checkroom, null, Modifier.size(32.dp), tint = if (isDark) Grey500 else Grey400)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!kiyaket.renk.isNullOrBlank()) {
                    RenkDairesi(renk = kiyaket.renk!!, size = 10); Spacer(Modifier.width(4.dp))
                }
                Text(kiyaket.tur.displayName, style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium, color = if (isDark) Grey200 else Grey800, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(kiyaket.marka, style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Grey500 else Grey600, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp)
        }
    }
}

@Composable
fun RenkDairesi(renk: String, size: Int = 12) {
    val renkKodu = renkToColor(renk)
    Box(modifier = Modifier.size(size.dp).clip(CircleShape).background(renkKodu)
        .then(if (renk in listOf("Beyaz", "Krem", "Ekru")) Modifier.border(0.5.dp, Grey400, CircleShape) else Modifier))
}

private fun renkToColor(renk: String): Color = when (renk) {
    "Siyah" -> Color(0xFF1A1A1A); "Beyaz" -> Color(0xFFF5F5F5); "Gri" -> Color(0xFF9E9E9E)
    "Lacivert" -> Color(0xFF1A237E); "Mavi" -> Color(0xFF2196F3); "Kırmızı" -> Color(0xFFE53935)
    "Bordo" -> Color(0xFF880E4F); "Pembe" -> Color(0xFFE91E63); "Mor" -> Color(0xFF9C27B0)
    "Yeşil" -> Color(0xFF4CAF50); "Haki" -> Color(0xFF827717); "Sarı" -> Color(0xFFFDD835)
    "Turuncu" -> Color(0xFFFF9800); "Kahverengi" -> Color(0xFF795548); "Bej" -> Color(0xFFD7CCC8)
    "Krem" -> Color(0xFFFFF8E1); "Ekru" -> Color(0xFFF5F0E1); "Hardal" -> Color(0xFFF9A825)
    "Petrol" -> Color(0xFF006064); "Çoklu / Desenli" -> Color(0xFFFF6F00)
    else -> Color(0xFFBDBDBD)
}
