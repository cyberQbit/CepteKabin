package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KombinTakvimScreen(
    onNavigateBack: () -> Unit,
    onNavigateToKombinDetay: (Long) -> Unit = {},
    viewModel: KombinTakvimViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val gunlukGirisler by viewModel.gunlukGirisler.collectAsState()
    val tumKombinler by viewModel.tumKombinler.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val calendarDays = remember {
        val days = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5)
        for (i in 0..35) { days.add(cal.timeInMillis); cal.add(Calendar.DAY_OF_YEAR, 1) }
        days
    }

    val todayMidnight = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kombin Takvimi") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isDark) SurfaceDark else SurfaceLight))
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize()
            .background(Brush.verticalGradient(if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)))
            .padding(paddingValues)) {

            LazyRow(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(calendarDays) { dayTimestamp ->
                    val isSelected = selectedDate == dayTimestamp
                    val isPast = dayTimestamp < todayMidnight
                    val dayCal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
                    val dayName = SimpleDateFormat("EEE", Locale("tr")).format(dayCal.time)
                    val dayNum = dayCal.get(Calendar.DAY_OF_MONTH).toString()

                    Column(Modifier.clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryCyan else if (isDark) GlassDark else GlassLight)
                        .clickable { viewModel.setSelectedDate(dayTimestamp) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayName, color = if (isSelected) White else if (isPast) Grey500 else if (isDark) Grey300 else Grey700,
                            style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(dayNum, color = if (isSelected) White else if (isPast) Grey500 else if (isDark) Grey100 else Grey900,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = if (isDark) GlassDarkBorder else Grey200)

            LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date(selectedDate))
                    Text(dateStr, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        color = if (isDark) Grey100 else Grey900)
                    Spacer(Modifier.height(16.dp))
                }

                items(gunlukGirisler) { giris ->
                    GlassCard(modifier = Modifier.fillMaxWidth().clickable { onNavigateToKombinDetay(giris.kombinId) }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(AccentGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center) { Icon(Icons.Default.Style, null, tint = AccentGold) }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(giris.kombinAd, style = MaterialTheme.typography.titleMedium, color = if (isDark) Grey100 else Grey900)
                                    Text("Planlandı", style = MaterialTheme.typography.bodySmall, color = PrimaryLight)
                                }
                            }
                            if (selectedDate >= todayMidnight) {
                                IconButton(onClick = { viewModel.removeTakvimGirisi(giris) }) {
                                    Icon(Icons.Default.Delete, "Sil", tint = Error)
                                }
                            }
                        }
                    }
                }

                item {
                    if (selectedDate >= todayMidnight && gunlukGirisler.size < 3) {
                        Button(onClick = { showBottomSheet = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp))
                            Text("Kombin Ekle (${3 - gunlukGirisler.size} hak kaldı)")
                        }
                    } else if (selectedDate < todayMidnight) {
                        Text("Geçmiş günlere yeni kombin eklenemez. (Arşiv Modu)",
                            style = MaterialTheme.typography.bodyMedium, color = Grey500)
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Kombin Seç", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    if (tumKombinler.isEmpty()) {
                        Text("Henüz oluşturulmuş bir kombin yok.", modifier = Modifier.padding(bottom = 32.dp))
                    } else {
                        LazyColumn(modifier = Modifier.padding(bottom = 32.dp)) {
                            items(tumKombinler) { kombin ->
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .clickable { viewModel.addKombinToDate(kombin); showBottomSheet = false },
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceVariantDark else Grey100)) {
                                    Text(kombin.ad, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
