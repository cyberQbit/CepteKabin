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

    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.loadHavaDurumuWithLocation()
        } else {
            // İzin yoksa varsayılan şehir
            viewModel.loadHavaDurumuByCity("Istanbul")
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundGradient = if (isDark) {
        listOf(Grey900, Grey900.copy(alpha = 0.95f))
    } else {
        listOf(Grey100, White)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundGradient))
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
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "CepteKabin Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CepteKabin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Grey100 else Grey900
                    )
                    Text(
                        text = "Senin Dolabın, Senin Kombinin!",
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
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = "Tarama",
                    tint = if (isDark) Grey100 else Grey800
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hava Durumu Card
        WeatherCard(
            havaDurumu = havaDurumu,
            sehirAdi = sehirAdi,
            isLoading = havaDurumuYukleniyor,
            isDark = isDark,
            onClick = onNavigateToHavaDurumu,
            onRetry = { viewModel.loadHavaDurumuWithLocation() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Hızlı İşlemler",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Grey100 else Grey900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.QrCodeScanner,
                title = "Barkod Tara",
                subtitle = "Ürün ekle",
                onClick = onNavigateToTarama,
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
            QuickActionCard(
                icon = Icons.Default.Add,
                title = "Kıyafet Ekle",
                subtitle = "Manuel ekle",
                onClick = onNavigateToDolap,
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Son Eklenenler
        if (sonEklenenler.isNotEmpty()) {
            Text(
                text = "Son Eklenenler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sonEklenenler.take(5)) { kiyaket ->
                    KiyaketMiniCard(kiyaket = kiyaket, isDark = isDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kombin Önerisi
        Text(
            text = "Önerilen Kombinler",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Grey100 else Grey900
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (favoriKombinler.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriKombinler.take(3)) { kombin ->
                    KombinMiniCard(
                        kombinAdi = kombin.ad,
                        onClick = { },
                        isDark = isDark
                    )
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isDark) Grey500 else Grey400
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Henüz kombin oluşturmadın",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey400 else Grey600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToKombin) {
                        Text("Kombin Oluştur", color = PrimaryLight)
                    }
                }
            }
        }
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
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        when {
            isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Hava durumu yükleniyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey400 else Grey600
                    )
                }
            }
            havaDurumu != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = sehirAdi ?: havaDurumu.sehir,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Grey100 else Grey900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${havaDurumu.sicaklik.toInt()}°",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Grey100 else Grey900
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = havaDurumu.durum.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Grey300 else Grey700
                                )
                                Text(
                                    text = "Hissedilen: ${havaDurumu.hissedilenSicaklik.toInt()}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Grey500 else Grey600
                                )
                            }
                        }
                    }
                    Text(
                        text = havaDurumu.durum.icon,
                        fontSize = 48.sp
                    )
                }

                if (havaDurumu.forecastList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) GlassDarkBorder else Grey200)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        havaDurumu.forecastList.take(5).forEach { forecast ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = forecast.gun.take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Grey400 else Grey600
                                )
                                Text(text = forecast.durum.icon, fontSize = 20.sp)
                                Text(
                                    text = "${forecast.sicaklikMax.toInt()}°",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Grey100 else Grey800
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isDark) Grey500 else Grey400
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hava durumu yüklenemedi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey400 else Grey600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRetry) {
                        Text("Tekrar dene", color = PrimaryLight)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    GlassSurface(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = PrimaryLight,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Grey500 else Grey600
            )
        }
    }
}

@Composable
fun KiyaketMiniCard(kiyaket: Kiyaket, isDark: Boolean) {
    GlassSurface(
        modifier = Modifier.size(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Checkroom,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = PrimaryLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kiyaket.marka,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Grey100 else Grey800,
                maxLines = 1
            )
            Text(
                text = kiyaket.tur.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Grey500 else Grey600,
                maxLines = 1
            )
        }
    }
}

@Composable
fun KombinMiniCard(
    kombinAdi: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    GlassSurface(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Style,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = AccentGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = kombinAdi,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Grey100 else Grey800,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}
