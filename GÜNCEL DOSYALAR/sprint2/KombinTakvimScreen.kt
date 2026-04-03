package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToKombinDetay: (Long) -> Unit,
    viewModel: KombinTakvimViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsState()
    val isDark    = isSystemInDarkTheme()
    val cal       = remember { Calendar.getInstance() }

    var displayedYear  by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }

    LaunchedEffect(displayedYear, displayedMonth) {
        viewModel.loadMonth(displayedYear, displayedMonth)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kombin Takvimi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                MonthHeader(
                    year  = displayedYear,
                    month = displayedMonth,
                    onPrev = {
                        if (displayedMonth == 0) { displayedMonth = 11; displayedYear-- }
                        else displayedMonth--
                    },
                    onNext = {
                        if (displayedMonth == 11) { displayedMonth = 0; displayedYear++ }
                        else displayedMonth++
                    },
                    isDark = isDark
                )
            }

            item {
                CalendarGrid(
                    year      = displayedYear,
                    month     = displayedMonth,
                    aktivGunler = uiState.aktivGunler,
                    secilenGun = uiState.secilenGun,
                    onDayClick = { viewModel.selectDay(it) },
                    isDark    = isDark
                )
            }

            if (uiState.secilenGunKombinler.isNotEmpty()) {
                item {
                    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
                    Text(
                        sdf.format(Date(uiState.secilenGun ?: 0)),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isDark) Grey100 else Grey900
                    )
                }
                items(uiState.secilenGunKombinler) { kullanimItem ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onNavigateToKombinDetay(kullanimItem.kombinId) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Style, null, Modifier.size(24.dp), tint = AccentGold)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(kullanimItem.kombinAd,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Grey100 else Grey900)
                                val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                                Text(timeFmt.format(Date(kullanimItem.tarih)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Grey500 else Grey600)
                            }
                            Icon(Icons.Default.ChevronRight, null,
                                tint = if (isDark) Grey500 else Grey400)
                        }
                    }
                }
            } else if (uiState.secilenGun != null) {
                item {
                    Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                        Text("Bu gün için kombin kaydı yok.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Grey500 else Grey600)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Ay başlığı ────────────────────────────────────────────────────────────────
@Composable
private fun MonthHeader(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit, isDark: Boolean) {
    val months = listOf("Ocak","Şubat","Mart","Nisan","Mayıs","Haziran",
        "Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık")
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Önceki ay",
                tint = if (isDark) Grey300 else Grey700)
        }
        Text("${months[month]} $year",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = if (isDark) Grey100 else Grey900)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Sonraki ay",
                tint = if (isDark) Grey300 else Grey700)
        }
    }
}

// ── Ay ızgarası ───────────────────────────────────────────────────────────────
@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    aktivGunler: Set<Int>,
    secilenGun: Long?,
    onDayClick: (Long) -> Unit,
    isDark: Boolean
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDow  = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7  // Pazartesi başlangıç
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()
    val todayYear  = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay   = today.get(Calendar.DAY_OF_MONTH)

    val dayHeaders = listOf("Pt","Sa","Ça","Pe","Cu","Ct","Pz")

    Column {
        Row(Modifier.fillMaxWidth()) {
            dayHeaders.forEach { d ->
                Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Grey500 else Grey600)
            }
        }
        Spacer(Modifier.height(4.dp))

        val totalCells = firstDow + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum    = cellIndex - firstDow + 1
                    Modifier.weight(1f)

                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(Modifier.weight(1f).height(40.dp))
                    } else {
                        val dayMs = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayNum)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        val isToday   = year == todayYear && month == todayMonth && dayNum == todayDay
                        val hasKombin = dayNum in aktivGunler
                        val isSelected = secilenGun?.let {
                            val selCal = Calendar.getInstance().apply { timeInMillis = it }
                            selCal.get(Calendar.DAY_OF_MONTH) == dayNum &&
                            selCal.get(Calendar.MONTH) == month &&
                            selCal.get(Calendar.YEAR)  == year
                        } ?: false

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> PrimaryLight
                                        isToday    -> PrimaryLight.copy(alpha = 0.15f)
                                        else       -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isToday && !isSelected)
                                        Modifier.border(1.dp, PrimaryLight, CircleShape)
                                    else Modifier
                                )
                                .clickable { onDayClick(dayMs) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text  = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> White
                                        isToday    -> PrimaryLight
                                        isDark     -> Grey200
                                        else       -> Grey800
                                    }
                                )
                                if (hasKombin) {
                                    Box(
                                        Modifier.size(5.dp).clip(CircleShape)
                                            .background(if (isSelected) White else AccentGold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
