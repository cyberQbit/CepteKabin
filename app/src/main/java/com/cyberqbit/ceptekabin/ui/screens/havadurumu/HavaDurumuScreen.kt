package com.cyberqbit.ceptekabin.ui.screens.havadurumu

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.ForecastItem
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.HavaDurumuDurum
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.screens.home.HomeViewModel
import com.cyberqbit.ceptekabin.ui.theme.*
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

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.loadHavaDurumuWithLocation()
        } else {
            viewModel.loadHavaDurumuByCity("Istanbul")
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundGradient = if (isDark) {
        listOf(Grey900, SurfaceDark)
    } else {
        listOf(Grey100, White)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundGradient))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Başlık - Removed back button since this is a bottom nav tab
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hava Durumu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900,
                maxLines = 1,
                fontSize = 24.sp
            )
            IconButton(onClick = { viewModel.loadHavaDurumuWithLocation() }) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Konumumu Kullan",
                    tint = PrimaryLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryLight)
            }
        } else {
            havaDurumu?.let { hava ->
                // Ana Hava Durumu Kartı
                    MainWeatherCard(havaDurumu = hava, sonGuncelleme = sonGuncelleme, isDark = isDark)
                // Hava Durumu Detayları
                WeatherDetailsCard(havaDurumu = hava, isDark = isDark)

                Spacer(modifier = Modifier.height(16.dp))

                // 5 Günlük Tahmin - Now with LazyRow
                if (hava.forecastList.isNotEmpty()) {
                    Text(
                        text = "5 Günlük Tahmin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Grey100 else Grey900
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hava.forecastList) { forecast ->
                            ForecastCard(forecast = forecast, isDark = isDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kıyafet Önerisi
                OutfitSuggestionCard(havaDurumu = hava, isDark = isDark)
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Grey500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hava durumu yüklenemedi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Grey600
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadHavaDurumuWithLocation() }) {
                            Text("Tekrar Dene", color = PrimaryLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainWeatherCard(havaDurumu: HavaDurumu, sonGuncelleme: String?, isDark: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) {
            Grey900.copy(alpha = 0.4f)
        } else {
            White.copy(alpha = 0.7f)
        },
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Grey800.copy(alpha = 0.2f),
                                Grey900.copy(alpha = 0.1f)
                            )
                        } else {
                            listOf(
                                White.copy(alpha = 0.5f),
                                Grey100.copy(alpha = 0.3f)
                            )
                        }
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = sonGuncelleme ?: "Son Güncelleme: --.-- - --/--/----",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Grey400 else Grey600,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = havaDurumu.durum.toEmoji(),
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${havaDurumu.sicaklik.toInt()}°C",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Grey100 else Grey900
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = havaDurumu.durum.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Grey300 else Grey700,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "En yüksek: ${havaDurumu.sicaklik.toInt()}° / En düşük: ${(havaDurumu.sicaklik - 5).toInt()}°",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Grey500 else Grey600,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WeatherDetailsCard(havaDurumu: HavaDurumu, isDark: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) {
            Grey900.copy(alpha = 0.4f)
        } else {
            White.copy(alpha = 0.7f)
        },
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Grey800.copy(alpha = 0.2f),
                                Grey900.copy(alpha = 0.1f)
                            )
                        } else {
                            listOf(
                                White.copy(alpha = 0.5f),
                                Grey100.copy(alpha = 0.3f)
                            )
                        }
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "Detaylar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Thermostat,
                    label = "Hissedilen",
                    value = "${havaDurumu.hissedilenSicaklik.toInt()}°",
                    isDark = isDark
                )
                WeatherDetailItem(
                    icon = Icons.Default.WaterDrop,
                    label = "Nem",
                    value = "${havaDurumu.nemOrani}%",
                    isDark = isDark
                )
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = "Rüzgar",
                    value = "${havaDurumu.ruzgarHizi.toInt()} km/s",
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, isDark: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimaryLight.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Grey500 else Grey600,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Grey100 else Grey900
        )
    }
}

@Composable
fun ForecastCard(forecast: ForecastItem, isDark: Boolean) {
    Surface(
        modifier = Modifier
            .width(100.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) {
            Grey900.copy(alpha = 0.3f)
        } else {
            White.copy(alpha = 0.6f)
        },
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Grey800.copy(alpha = 0.15f),
                                Grey900.copy(alpha = 0.05f)
                            )
                        } else {
                            listOf(
                                White.copy(alpha = 0.4f),
                                Grey100.copy(alpha = 0.2f)
                            )
                        }
                    )
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = forecast.gun,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey200 else Grey800,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = forecast.durum.toEmoji(),
                fontSize = 24.sp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${forecast.sicaklikMax.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Grey100 else Grey900,
                    fontSize = 12.sp
                )
                Text(
                    text = "${forecast.sicaklikMin.toInt()}°",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Grey400 else Grey600,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun OutfitSuggestionCard(havaDurumu: HavaDurumu, isDark: Boolean) {
    val sicaklik = havaDurumu.sicaklik
    val ruzgarHizi = havaDurumu.ruzgarHizi
    val weatherIcon = havaDurumu.durum.icon.lowercase()
    val durum = havaDurumu.durum.displayName.lowercase()
    
    // Check if weather has rain or wind
    val hasRain = weatherIcon.contains("rain") || durum.contains("yağmur") || durum.contains("rain")
    val hasStrongWind = ruzgarHizi > 15

    val (ustGiyim, altGiyim, disGiyim, ayakkabi, aksesuar) = when {
        sicaklik >= 30 -> {
            val aksesuar = when {
                hasRain -> "Şemsiye"
                hasStrongWind -> "Şapka"
                else -> "Güneş Kremi"
            }
            listOf("Tişört", "Şort", null, "Sandalet", aksesuar)
        }
        sicaklik >= 20 -> {
            val aksesuar = when {
                hasRain -> "Şemsiye"
                hasStrongWind -> "Rüzgarlık"
                else -> null
            }
            listOf("Tişört", "Pantolon", null, "Spor Ayakkabı", aksesuar)
        }
        sicaklik >= 10 -> {
            val disGiyim = if (hasRain) "Su Geçirmez Mont" else "Ceket"
            val aksesuar = if (hasRain) "Şemsiye" else null
            listOf("Sweatshirt", "Pantolon", disGiyim, "Ayakkabı", aksesuar)
        }
        sicaklik >= 0 -> {
            val disGiyim = if (hasRain) "Su Geçirmez Mont" else "Mont"
            val aksesuar = "Bot"
            listOf("Kazak", "Pantolon", disGiyim, aksesuar, null)
        }
        else -> {
            val aksesuar = if (hasRain) "Su Geçirmez Bot" else "Bot"
            listOf("Kalın Kazak", "Kalın Pantolon", "Kaban", aksesuar, null)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) {
            Grey900.copy(alpha = 0.4f)
        } else {
            White.copy(alpha = 0.7f)
        },
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Grey800.copy(alpha = 0.2f),
                                Grey900.copy(alpha = 0.1f)
                            )
                        } else {
                            listOf(
                                White.copy(alpha = 0.5f),
                                Grey100.copy(alpha = 0.3f)
                            )
                        }
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Kıyafet Önerisi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Grey100 else Grey900
                    )
                    Text(
                        text = "Bu hava için önerilen kıyafetler:",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Grey500 else Grey600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(
                color = if (isDark) Grey800 else Grey200,
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOfNotNull(ustGiyim, altGiyim, disGiyim, ayakkabi, aksesuar).forEach { item ->
                    OutfitChip(label = item, isDark = isDark)
                }
            }
        }
    }
}

@Composable
fun OutfitChip(label: String, isDark: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) {
            PrimaryLight.copy(alpha = 0.2f)
        } else {
            PrimaryLight.copy(alpha = 0.15f)
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (isDark) PrimaryLight else PrimaryLight,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

