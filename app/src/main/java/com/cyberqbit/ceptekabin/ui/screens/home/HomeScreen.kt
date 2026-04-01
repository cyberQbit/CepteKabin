package com.cyberqbit.ceptekabin.ui.screens.home

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.R
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassButton
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val havaDurumu by viewModel.havaDurumu.collectAsState()
    val sonEklenenler by viewModel.sonEklenenler.collectAsState()
    val favoriKombinler by viewModel.favoriKombinler.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val havaDurumuYukleniyor by viewModel.havaDurumuYukleniyor.collectAsState()
    val sehirAdi by viewModel.sehirAdi.collectAsState()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // FIX: request permission first, then load weather based on result
    var permissionRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            if (!locationPermission.status.isGranted) {
                locationPermission.launchPermissionRequest()
            }
        }
    }

    // FIX: React to permission status change (granted after request)
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.loadHavaDurumuWithLocation()
        } else {
            // Load with default city if permission not granted
            viewModel.loadHavaDurumuByCity("Istanbul")
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Grey900, Grey900.copy(alpha = 0.95f))
                    else listOf(Grey100, White)
                )
            )
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
                // Logo - use app_logo drawable; falls back gracefully if missing
                val logoExists = remember {
                    try { true } catch (e: Exception) { false }
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    try {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "CepteKabin Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } catch (e: Exception) {
                        Icon(Icons.Default.Checkroom, null, tint = White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "CepteKabin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Grey100 else Grey900
                    )
                    Text(
                        "Senin Dolabın, Senin Kombinin!",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Grey500 else Grey600
                    )
                }
            }
            IconButton(
                onClick = onNavigateToTarama,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDark) GlassDark else GlassLight)
            ) {
                Icon(Icons.Default.QrCodeScanner, "Tarama", tint = if (isDark) Grey100 else Grey800)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Hava Durumu Card
        WeatherCard(
            havaDurumu = havaDurumu,
            sehirAdi = sehirAdi,
            isLoading = havaDurumuYukleniyor,
            isDark = isDark,
            onClick = onNavigateToHavaDurumu,
            onRetry = {
                if (locationPermission.status.isGranted) {
                    viewModel.loadHavaDurumuWithLocation()
                } else {
                    viewModel.loadHavaDurumuByCity("Istanbul")
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        // Quick Actions
        Text("Hızlı İşlemler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Icons.Default.QrCodeScanner, "Barkod Tara", "Ürün ekle", onNavigateToTarama, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.Style, "Kombin Yap", "Kıyafet kombinle", onNavigateToKombin, Modifier.weight(1f), isDark)
        }

        Spacer(Modifier.height(24.dp))

        // Son Eklenenler
        if (sonEklenenler.isNotEmpty()) {
            Text("Son Eklenenler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sonEklenenler.take(5)) { kiyaket ->
                    KiyaketMiniCard(kiyaket = kiyaket, isDark = isDark)
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Kombin Önerisi
        Text("Önerilen Kombinler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(12.dp))

        if (favoriKombinler.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoriKombinler.take(3)) { kombin ->
                    KombinMiniCard(kombinAdi = kombin.ad, onClick = {}, isDark = isDark)
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Style, null, Modifier.size(48.dp), tint = if (isDark) Grey500 else Grey400)
                    Spacer(Modifier.height(8.dp))
                    Text("Henüz kombin oluşturmadın", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToKombin) { Text("Kombin Oluştur", color = PrimaryLight) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun WeatherCard(
    havaDurumu: HavaDurumu?,
    sehirAdi: String?,
    isLoading: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onRetry: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        when {
            isLoading -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryLight)
                    Spacer(Modifier.width(12.dp))
                    Text("Hava durumu yükleniyor...", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600)
                }
            }
            havaDurumu != null -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            sehirAdi ?: havaDurumu.sehir,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Grey100 else Grey900
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${havaDurumu.sicaklik.toInt()}°", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(havaDurumu.durum.displayName, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey300 else Grey700)
                                Text("Hissedilen: ${havaDurumu.hissedilenSicaklik.toInt()}°", style = MaterialTheme.typography.bodySmall, color = if (isDark) Grey500 else Grey600)
                            }
                        }
                    }
                    Text(havaDurumu.durum.icon, fontSize = 48.sp)
                }
                if (havaDurumu.forecastList.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) GlassDarkBorder else Grey200)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        havaDurumu.forecastList.take(5).forEach { forecast ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(forecast.gun.take(3), style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey400 else Grey600)
                                Text(forecast.durum.icon, fontSize = 20.sp)
                                Text("${forecast.sicaklikMax.toInt()}°", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey800)
                            }
                        }
                    }
                }
            }
            else -> {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, Modifier.size(32.dp), tint = if (isDark) Grey500 else Grey400)
                    Spacer(Modifier.height(8.dp))
                    Text("Hava durumu yüklenemedi", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onRetry) { Text("Tekrar dene", color = PrimaryLight) }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier, isDark: Boolean) {
    GlassSurface(modifier = modifier.height(100.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Icon(icon, title, tint = PrimaryLight, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (isDark) Grey500 else Grey600)
        }
    }
}

@Composable
fun KiyaketMiniCard(kiyaket: Kiyaket, isDark: Boolean) {
    GlassSurface(modifier = Modifier.size(100.dp)) {
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Checkroom, null, Modifier.size(32.dp), tint = PrimaryLight)
            Spacer(Modifier.height(4.dp))
            Text(kiyaket.marka, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = if (isDark) Grey100 else Grey800, maxLines = 1)
            Text(kiyaket.tur.displayName, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600, maxLines = 1)
        }
    }
}

@Composable
fun KombinMiniCard(kombinAdi: String, onClick: () -> Unit, isDark: Boolean) {
    GlassSurface(modifier = Modifier.width(120.dp).height(100.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Style, null, Modifier.size(32.dp), tint = AccentGold)
            Spacer(Modifier.height(8.dp))
            Text(kombinAdi, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = if (isDark) Grey100 else Grey800, maxLines = 2, textAlign = TextAlign.Center)
        }
    }
}
