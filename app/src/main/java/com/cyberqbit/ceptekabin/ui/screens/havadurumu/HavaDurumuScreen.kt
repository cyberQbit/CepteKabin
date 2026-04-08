package com.cyberqbit.ceptekabin.ui.screens.havadurumu

import android.Manifest
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.engine.WeatherOutfitEngine
import com.cyberqbit.ceptekabin.domain.model.ForecastItem
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.HavaDurumuDurum
import com.cyberqbit.ceptekabin.ui.theme.*
import com.cyberqbit.ceptekabin.ui.screens.home.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HavaDurumuScreen(
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val havaDurumu by viewModel.havaDurumu.collectAsState()
    val sonGuncelleme by viewModel.sonGuncelleme.collectAsState()
    val isLoading by viewModel.havaDurumuYukleniyor.collectAsState()

    val isDark = isSystemInDarkTheme()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var secilenForecast by remember { mutableStateOf<ForecastItem?>(null) }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) viewModel.loadHavaDurumuWithLocation()
        else viewModel.loadHavaDurumuByCity("Ankara")
    }

    val bgColors = havaDurumu?.let { getDynamicBackground(it.durum, isDark) }
        ?: if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)

    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))
        .statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Hava Durumu", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
            IconButton(onClick = { viewModel.loadHavaDurumuWithLocation() }) {
                Icon(Icons.Default.MyLocation, "Konum", tint = PrimaryLight)
            }
        }
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator(color = PrimaryLight) }
        } else {
            havaDurumu?.let { hava ->
                MainWeatherCard(hava, sonGuncelleme, isDark)
                Spacer(Modifier.height(16.dp))
                WeatherDetailsRow(hava, isDark)
                Spacer(Modifier.height(20.dp))

                if (hava.forecastList.isNotEmpty()) {
                    Text("5 Günlük Tahmin", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
                    Spacer(Modifier.height(4.dp))
                    Text("Güne dokunarak kıyafet önerisini gör", style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Grey500 else Grey600)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hava.forecastList) { forecast ->
                            ForecastCard(forecast, secilenForecast == forecast, isDark) {
                                secilenForecast = if (secilenForecast == forecast) null else forecast
                            }
                        }
                    }
                    secilenForecast?.let { forecast ->
                        Spacer(Modifier.height(12.dp))
                        val rec = WeatherOutfitEngine.getRecommendationForForecast(forecast)
                        ForecastOutfitCard(forecast, rec, isDark)
                    }
                }

                Spacer(Modifier.height(20.dp))
                val recommendation = WeatherOutfitEngine.getRecommendation(hava)
                OutfitRecommendationCard(recommendation, isDark)
            } ?: run {
                Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, null, Modifier.size(48.dp), tint = if (isDark) Grey500 else Grey400)
                        Spacer(Modifier.height(12.dp))
                        Text("Hava durumu yüklenemedi", color = if (isDark) Grey400 else Grey600)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadHavaDurumuByCity("Ankara") }) {
                            Text("Tekrar dene", color = PrimaryLight)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MainWeatherCard(hava: HavaDurumu, sonGuncelleme: String?, isDark: Boolean) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = if (isDark) Grey900.copy(alpha = 0.5f) else White.copy(alpha = 0.8f), shadowElevation = 8.dp) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            sonGuncelleme?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600); Spacer(Modifier.height(8.dp)) }
            Text(hava.durum.toEmoji(), fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text("${hava.sicaklik.toInt()}°C", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
            Text(hava.durum.displayName, style = MaterialTheme.typography.bodyLarge, color = if (isDark) Grey300 else Grey700)
            Spacer(Modifier.height(4.dp))
            Text("Hissedilen: ${hava.hissedilenSicaklik.toInt()}°C", style = MaterialTheme.typography.labelMedium, color = if (isDark) Grey500 else Grey600)
        }
    }
}

@Composable
private fun WeatherDetailsRow(hava: HavaDurumu, isDark: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailChip(Icons.Default.Thermostat, "Hissedilen", "${hava.hissedilenSicaklik.toInt()}°", isDark, Modifier.weight(1f))
        DetailChip(Icons.Default.WaterDrop, "Nem", "${hava.nemOrani}%", isDark, Modifier.weight(1f))
        DetailChip(Icons.Default.Air, "Rüzgar", "${hava.ruzgarHizi.toInt()} km/s", isDark, Modifier.weight(1f))
    }
}

@Composable
private fun DetailChip(icon: ImageVector, label: String, value: String, isDark: Boolean, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp),
        color = if (isDark) Grey800.copy(alpha = 0.5f) else White.copy(alpha = 0.8f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(22.dp), tint = PrimaryLight)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ForecastCard(forecast: ForecastItem, isSelected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    Surface(Modifier.width(80.dp).clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PrimaryLight.copy(alpha = 0.2f) else if (isDark) Grey800.copy(alpha = 0.5f) else White.copy(alpha = 0.8f),
        border = if (isSelected) BorderStroke(1.5.dp, PrimaryLight) else null) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(forecast.gun.take(3), style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isDark) Grey300 else Grey700)
            Spacer(Modifier.height(4.dp))
            Text(forecast.durum.toEmoji(), fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text("${forecast.sicaklikMax.toInt()}°", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = if (isDark) Grey100 else Grey900)
            Text("${forecast.sicaklikMin.toInt()}°", style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600)
        }
    }
}

@Composable
private fun ForecastOutfitCard(forecast: ForecastItem, rec: WeatherOutfitEngine.OutfitRecommendation, isDark: Boolean) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        color = if (isDark) Grey800.copy(alpha = 0.4f) else PrimaryLight.copy(alpha = 0.06f)) {
        Column(Modifier.padding(14.dp)) {
            Text("${forecast.gun} için öneri", style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold, color = PrimaryLight)
            Spacer(Modifier.height(6.dp))
            Text(rec.detayliAciklama, style = MaterialTheme.typography.bodySmall, color = if (isDark) Grey300 else Grey700)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (rec.ustGiyim + rec.altGiyim + rec.disGiyim + rec.ayakkabi + rec.aksesuar).take(5).forEach { item ->
                    Surface(shape = RoundedCornerShape(8.dp), color = PrimaryLight.copy(alpha = 0.15f)) {
                        Text(item, style = MaterialTheme.typography.labelSmall, color = PrimaryLight, fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OutfitRecommendationCard(rec: WeatherOutfitEngine.OutfitRecommendation, isDark: Boolean) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = if (isDark) Grey900.copy(alpha = 0.5f) else White.copy(alpha = 0.8f), shadowElevation = 4.dp) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = AccentGold.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                    Box(Alignment.Center) { Icon(Icons.Default.Checkroom, null, Modifier.size(22.dp), tint = AccentGold) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Bugün Ne Giymeliyim?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
                    Text(rec.kisaAciklama, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey400 else Grey600)
                }
                KonforBadge(rec.konforEndeksi, isDark)
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = if (isDark) Grey800 else Grey200, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Text("${rec.katmanSayisi} katman öneriliyor", style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600)
            Spacer(Modifier.height(8.dp))
            if (rec.ustGiyim.isNotEmpty()) OutfitRow("Üst:", rec.ustGiyim, isDark)
            if (rec.altGiyim.isNotEmpty()) OutfitRow("Alt:", rec.altGiyim, isDark)
            if (rec.disGiyim.isNotEmpty()) OutfitRow("Dış:", rec.disGiyim, isDark)
            if (rec.ayakkabi.isNotEmpty()) OutfitRow("Ayak:", rec.ayakkabi, isDark)
            if (rec.aksesuar.isNotEmpty()) OutfitRow("Aks:", rec.aksesuar, isDark)
            Spacer(Modifier.height(12.dp))
            Text(rec.detayliAciklama, style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Grey400 else Grey600, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun OutfitRow(label: String, items: List<String>, isDark: Boolean) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600, modifier = Modifier.width(36.dp))
        items.forEach { item ->
            Surface(shape = RoundedCornerShape(8.dp), color = PrimaryLight.copy(alpha = 0.15f), modifier = Modifier.padding(end = 4.dp)) {
                Text(item, style = MaterialTheme.typography.labelSmall, color = PrimaryLight, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun KonforBadge(endeks: Int, isDark: Boolean) {
    val color = when { endeks >= 70 -> Color(0xFF4CAF50); endeks >= 40 -> AccentGold; else -> Color(0xFFE53935) }
    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.15f)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$endeks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text("konfor", fontSize = 9.sp, color = color.copy(alpha = 0.7f))
        }
    }
}

private fun getDynamicBackground(durum: HavaDurumuDurum, isDark: Boolean): List<Color> {
    return if (isDark) {
        when (durum) {
            HavaDurumuDurum.GUNESLI, HavaDurumuDurum.AZ_BULUTLU -> listOf(Color(0xFF1A2744), Color(0xFF0D1B2A))
            HavaDurumuDurum.YAGMURLU, HavaDurumuDurum.YAGIS_HAKLI -> listOf(Color(0xFF1A1F2E), Color(0xFF0F1318))
            HavaDurumuDurum.KARLI -> listOf(Color(0xFF1E2A3A), Color(0xFF121B25))
            HavaDurumuDurum.FIRTINALI -> listOf(Color(0xFF1A1520), Color(0xFF0A0810))
            else -> listOf(Grey900, SurfaceDark)
        }
    } else {
        when (durum) {
            HavaDurumuDurum.GUNESLI, HavaDurumuDurum.AZ_BULUTLU -> listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
            HavaDurumuDurum.YAGMURLU, HavaDurumuDurum.YAGIS_HAKLI -> listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
            HavaDurumuDurum.KARLI -> listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9))
            HavaDurumuDurum.FIRTINALI -> listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9))
            else -> listOf(Grey100, White)
        }
    }
}
