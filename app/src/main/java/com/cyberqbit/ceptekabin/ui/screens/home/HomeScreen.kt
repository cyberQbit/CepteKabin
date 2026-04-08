package com.cyberqbit.ceptekabin.ui.screens.home

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.R
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val havaDurumu by viewModel.havaDurumu.collectAsState()
    val sonEklenenler by viewModel.sonEklenenler.collectAsState()
    val onerilenKombinler by viewModel.onerilenKombinler.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val havaDurumuYukleniyor by viewModel.havaDurumuYukleniyor.collectAsState()

    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var permissionRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkAndShowSharePrompt()
        if (!permissionRequested) {
            permissionRequested = true
            if (!locationPermission.status.isGranted) {
                locationPermission.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.loadHavaDurumuWithLocation()
        } else {
            viewModel.loadHavaDurumuByCity("Ankara")
        }
    }

    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Grey900, Grey900.copy(alpha = 0.95f))
                    else listOf(Grey100, White)
                )
            )
            // Yüzen bottom bar için alt padding eklendi
            .padding(bottom = 80.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "CepteKabin Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("CepteKabin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
                    Text("Senin Dolabın, Senin Kombinin!", style = MaterialTheme.typography.bodySmall, color = if (isDark) Grey500 else Grey600)
                }
            }
            IconButton(
                onClick = onNavigateToTarama,
                modifier = Modifier.clip(CircleShape).background(if (isDark) GlassDark else GlassLight)
            ) {
                Icon(Icons.Default.QrCodeScanner, "Tarama", tint = if (isDark) Grey100 else Grey800)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hava Durumu
        WeatherCompactBar(
            havaDurumu = havaDurumu, 
            isLoading = havaDurumuYukleniyor, 
            isDark = isDark, 
            onClick = onNavigateToHavaDurumu
        )

        Spacer(Modifier.height(24.dp))

        // Hızlı İşlemler
        Text("Hızlı İşlemler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Icons.Default.QrCodeScanner, "Barkod Tara", onNavigateToTarama, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.Style, "Kombin Yap", onNavigateToKombin, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.CalendarMonth, "Takvim", onNavigateToKombinTakvim, Modifier.weight(1f), isDark)
        }

        Spacer(Modifier.height(24.dp))

        // Sanal Kabin Kartı
        GlassCard(modifier = Modifier.fillMaxWidth().clickable { onNavigateToVirtualTryOn() }) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(AccentGold.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccessibilityNew, contentDescription = "Sanal Kabin", tint = AccentGold, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sanal Kabin (AR)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
                    Text("Kombinlerini kendi üzerinde gör!", style = MaterialTheme.typography.bodySmall, color = if (isDark) Grey400 else Grey600)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Git", tint = if (isDark) Grey400 else Grey600)
            }
        }

        Spacer(Modifier.height(24.dp))

        // YENİ: SADECE BİR TANE AI KOMBİN ÖNERİSİ BÖLÜMÜ
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, "Yapay Zeka", tint = PrimaryCyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("AI Günün Kombinleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (onerilenKombinler.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(onerilenKombinler) { kombin ->
                    SmartKombinMiniCard(kombin = kombin, onClick = { onNavigateToKombin() }, isDark = isDark)
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoAwesomeMosaic, null, Modifier.size(40.dp), tint = if (isDark) Grey600 else Grey400)
                    Spacer(Modifier.height(8.dp))
                    Text("Yapay Zeka dolabını inceliyor...", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600, textAlign = TextAlign.Center)
                    Text("Daha iyi öneriler için dolabına yeni kıyafetler ekle.", style = MaterialTheme.typography.labelSmall, color = PrimaryLight, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Son Eklenenler
        if (sonEklenenler.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Son Eklenenler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
                TextButton(onClick = onNavigateToDolap) { Text("Tümünü Gör", style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sonEklenenler.take(5)) { kiyaket ->
                    KiyaketMiniCard(kiyaket = kiyaket, isDark = isDark, onClick = { onNavigateToKiyaket(kiyaket.id) })
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SmartKombinMiniCard(kombin: Kombin, onClick: () -> Unit, isDark: Boolean) {
    GlassSurface(modifier = Modifier.width(160.dp).height(120.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(24.dp), tint = AccentGold)
                Text(kombin.ad.replace("AI Önerisi: ", ""), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey800, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start=4.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Uyum Puanı: %${kombin.puan}", style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey400 else Grey600, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text("Detay", style = MaterialTheme.typography.labelSmall, color = PrimaryLight, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun WeatherCompactBar(havaDurumu: HavaDurumu?, isLoading: Boolean, isDark: Boolean, onClick: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                Text("Hava durumu güncelleniyor...", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600)
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryLight)
            } else if (havaDurumu != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(havaDurumu.durum.toEmoji(), fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${havaDurumu.sicaklik.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
                            Spacer(Modifier.width(8.dp))
                            Text(havaDurumu.durum.displayName, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey300 else Grey700)
                        }
                        Text("Detaylı tahmin ve öneriler için dokun", style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey500)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Detay", tint = if (isDark) Grey400 else Grey600)
            } else {
                Text("Hava durumu alınamadı.", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600)
                Icon(Icons.Default.Refresh, contentDescription = "Yenile", tint = PrimaryLight)
            }
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, title: String, onClick: () -> Unit, modifier: Modifier = Modifier, isDark: Boolean) {
    GlassSurface(modifier = modifier.height(90.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, title, tint = PrimaryLight, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun KiyaketMiniCard(kiyaket: Kiyaket, isDark: Boolean, onClick: () -> Unit) {
    GlassSurface(modifier = Modifier.size(100.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (!kiyaket.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = kiyaket.imageUrl, contentDescription = kiyaket.marka, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Checkroom, null, Modifier.size(32.dp), tint = PrimaryLight)
            }
            Spacer(Modifier.height(8.dp))
            Text(kiyaket.marka, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = if (isDark) Grey100 else Grey800, maxLines = 1)
            Text(kiyaket.tur.displayName, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600, maxLines = 1)
        }
    }
}
