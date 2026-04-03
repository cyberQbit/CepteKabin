package com.cyberqbit.ceptekabin.ui.screens.home

import android.Manifest
import android.content.Intent
import androidx.compose.animation.core.*
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
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.R
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.*
import com.cyberqbit.ceptekabin.ui.theme.*
import com.cyberqbit.ceptekabin.util.KombinShareHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// ── Shimmer yardımcısı ────────────────────────────────────────────────────────
@Composable
fun ShimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

@Composable
fun SkeletonBox(modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(ShimmerBrush()))
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToDolap: () -> Unit,
    onNavigateToKombin: () -> Unit,
    onNavigateToTarama: () -> Unit,
    onNavigateToHavaDurumu: () -> Unit,
    onNavigateToKiyaket: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val havaDurumu          by viewModel.havaDurumu.collectAsState()
    val sonEklenenler       by viewModel.sonEklenenler.collectAsState()
    val onerilenKombinler   by viewModel.onerilenKombinler.collectAsState()
    val weatherLoadState    by viewModel.weatherLoadState.collectAsState()
    val showingCachedWeather by viewModel.showingCachedWeather.collectAsState()
    val sonGuncelleme       by viewModel.sonGuncelleme.collectAsState()
    val konumIzniVerildi    by viewModel.konumIzniVerildi.collectAsState()
    val showShareDialog     by viewModel.showShareDialog.collectAsState()
    val manuelSehir         by viewModel.manuelSehir.collectAsState()

    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var permissionRequested by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkAndShowSharePrompt()
        if (!permissionRequested) {
            permissionRequested = true
            if (!locationPermission.status.isGranted) locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        viewModel.setKonumIzniDurumu(locationPermission.status.isGranted)
        if (locationPermission.status.isGranted) viewModel.loadHavaDurumuWithLocation()
        else viewModel.loadHavaDurumuByCity(manuelSehir ?: "Ankara")
    }

    // ── Manuel şehir diyaloğu ──────────────────────────────────────────────
    if (showCityDialog) {
        CityPickerDialog(
            onDismiss = { showCityDialog = false },
            onConfirm = { city ->
                viewModel.setManuelSehir(city)
                showCityDialog = false
            }
        )
    }

    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                if (isDark) listOf(Grey900, Grey900.copy(alpha = 0.95f)) else listOf(Grey100, White)
            ))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(R.drawable.app_logo), "Logo", Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("CepteKabin", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Grey100 else Grey900)
                    Text("Senin Dolabın, Senin Kombinin!",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Grey500 else Grey600)
                }
            }
            IconButton(onClick = onNavigateToTarama,
                modifier = Modifier.clip(CircleShape)
                    .background(if (isDark) GlassDark else GlassLight)) {
                Icon(Icons.Default.QrCodeScanner, "Tarama",
                    tint = if (isDark) Grey100 else Grey800)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Konum kapalı uyarı chip'i
        if (konumIzniVerildi == false) {
            Surface(
                onClick = { showCityDialog = true },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOff, null, Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Konum kapalı — gösterilen: ${manuelSehir ?: "Ankara"}. Şehri değiştir →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Hava Durumu Kartı (skeleton veya dolu)
        when (weatherLoadState) {
            WeatherLoadState.LOADING_SKELETON -> WeatherSkeletonCard()
            else -> WeatherCard(
                havaDurumu = havaDurumu,
                sonGuncelleme = sonGuncelleme,
                isLoading = weatherLoadState == WeatherLoadState.LOADING_FRESH,
                showingCached = showingCachedWeather,
                isDark = isDark,
                onClick = onNavigateToHavaDurumu,
                onRetry = {
                    if (locationPermission.status.isGranted) viewModel.loadHavaDurumuWithLocation()
                    else showCityDialog = true
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Hızlı İşlemler", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Icons.Default.QrCodeScanner, "Barkod Tara", "Ürün ekle",
                onNavigateToTarama, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.Style, "Kombin Yap", "Kıyafet kombinle",
                onNavigateToKombin, Modifier.weight(1f), isDark)
        }

        if (sonEklenenler.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Son Eklenenler", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sonEklenenler) { k ->
                    KiyaketMiniCard(kiyaket = k, isDark = isDark,
                        onClick = { onNavigateToKiyaket(k.id) })
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Önerilen Kombinler", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(12.dp))
        if (onerilenKombinler.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(onerilenKombinler) { k ->
                    KombinMiniCard(k.ad, onClick = { onNavigateToKombin() }, isDark)
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Style, null, Modifier.size(48.dp),
                        tint = if (isDark) Grey500 else Grey400)
                    Spacer(Modifier.height(8.dp))
                    Text("Henüz kombin oluşturmadın",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey400 else Grey600)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToKombin) {
                        Text("Kombin Oluştur", color = PrimaryLight)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ── Skeleton hava durumu kartı ────────────────────────────────────────────────
@Composable
fun WeatherSkeletonCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SkeletonBox(Modifier.fillMaxWidth(0.4f).height(14.dp))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(Modifier.size(60.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    SkeletonBox(Modifier.width(100.dp).height(20.dp))
                    Spacer(Modifier.height(8.dp))
                    SkeletonBox(Modifier.width(140.dp).height(14.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                repeat(5) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SkeletonBox(Modifier.width(32.dp).height(12.dp))
                        Spacer(Modifier.height(6.dp))
                        SkeletonBox(Modifier.size(28.dp))
                        Spacer(Modifier.height(6.dp))
                        SkeletonBox(Modifier.width(24.dp).height(12.dp))
                    }
                }
            }
        }
    }
}

// ── Dolu hava durumu kartı ────────────────────────────────────────────────────
@Composable
fun WeatherCard(
    havaDurumu: HavaDurumu?,
    sonGuncelleme: String?,
    isLoading: Boolean,
    showingCached: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onRetry: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        when {
            havaDurumu != null -> {
                if (showingCached) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)) {
                        Icon(Icons.Default.History, null, Modifier.size(13.dp),
                            tint = if (isDark) Grey500 else Grey600)
                        Spacer(Modifier.width(4.dp))
                        Text(sonGuncelleme ?: "", style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Grey500 else Grey600)
                        if (isLoading) {
                            Spacer(Modifier.width(8.dp))
                            CircularProgressIndicator(Modifier.size(11.dp), PrimaryLight, strokeWidth = 1.5.dp)
                        }
                    }
                } else {
                    Text(sonGuncelleme ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Grey400 else Grey600,
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${havaDurumu.sicaklik.toInt()}°",
                                fontSize = 36.sp, fontWeight = FontWeight.Bold,
                                color = if (isDark) Grey100 else Grey900)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(havaDurumu.durum.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Grey300 else Grey700)
                                Text("Hissedilen: ${havaDurumu.hissedilenSicaklik.toInt()}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Grey500 else Grey600)
                            }
                        }
                    }
                    Text(havaDurumu.durum.toEmoji(), fontSize = 48.sp)
                }
                if (havaDurumu.forecastList.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) GlassDarkBorder else Grey200)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                        havaDurumu.forecastList.take(5).forEach { f ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(f.gun.take(3), style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Grey400 else Grey600)
                                Text(f.durum.toEmoji(), fontSize = 20.sp)
                                Text("${f.sicaklikMax.toInt()}°",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Grey100 else Grey800)
                            }
                        }
                    }
                }
            }
            isLoading -> WeatherSkeletonCard()
            else -> {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, Modifier.size(32.dp),
                        tint = if (isDark) Grey500 else Grey400)
                    Spacer(Modifier.height(8.dp))
                    Text("Hava durumu yüklenemedi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey400 else Grey600)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onRetry) { Text("Tekrar dene", color = PrimaryLight) }
                }
            }
        }
    }
}

// ── Şehir seçici diyalog ───────────────────────────────────────────────────────
@Composable
fun CityPickerDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    val cities = listOf(
        "Adana","Ankara","Antalya","Bursa","Diyarbakır","Eskişehir","Gaziantep",
        "İstanbul","İzmir","Kayseri","Konya","Malatya","Mersin","Samsun","Trabzon"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Şehir Seç") },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Şehir adı yaz") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text("Hızlı seçim:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cities) { city ->
                        FilterChip(
                            selected = input == city,
                            onClick = { input = city },
                            label = { Text(city, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (input.isNotBlank()) onConfirm(input.trim()) },
                enabled = input.isNotBlank()
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

// ── Küçük bileşenler ──────────────────────────────────────────────────────────
@Composable
fun QuickActionCard(icon: ImageVector, title: String, subtitle: String,
    onClick: () -> Unit, modifier: Modifier, isDark: Boolean) {
    GlassSurface(modifier = modifier.height(100.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.Center) {
            Icon(icon, title, tint = PrimaryLight, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Grey500 else Grey600)
        }
    }
}

@Composable
fun KiyaketMiniCard(kiyaket: Kiyaket, isDark: Boolean, onClick: () -> Unit) {
    GlassSurface(Modifier.size(100.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(8.dp), Arrangement.Center,
            Alignment.CenterHorizontally) {
            if (!kiyaket.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = kiyaket.imageUrl, contentDescription = kiyaket.marka,
                    modifier = Modifier.size(48.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            } else {
                Icon(Icons.Default.Checkroom, null, Modifier.size(32.dp), tint = PrimaryLight)
            }
            Spacer(Modifier.height(4.dp))
            Text(kiyaket.marka, style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Grey100 else Grey800, maxLines = 1)
            Text(kiyaket.tur.displayName, style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Grey500 else Grey600, maxLines = 1)
        }
    }
}

@Composable
fun KombinMiniCard(kombinAdi: String, onClick: () -> Unit, isDark: Boolean) {
    GlassSurface(Modifier.width(120.dp).height(100.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(12.dp), Arrangement.Center,
            Alignment.CenterHorizontally) {
            Icon(Icons.Default.Style, null, Modifier.size(32.dp), tint = AccentGold)
            Spacer(Modifier.height(8.dp))
            Text(kombinAdi, style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Grey100 else Grey800,
                maxLines = 2, textAlign = TextAlign.Center)
        }
    }
}
