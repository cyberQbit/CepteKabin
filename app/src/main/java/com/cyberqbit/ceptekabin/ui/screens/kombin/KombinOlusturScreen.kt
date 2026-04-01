package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KombinOlusturScreen(
    kombinId: Long = 0L,
    onNavigateBack: () -> Unit,
    onKombinSaved: () -> Unit,
    viewModel: KombinOlusturViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    var kombinAdi by remember { mutableStateOf("") }
    var existingKombin by remember { mutableStateOf<Kombin?>(null) }

    // Düzenleme modunu başlat
    LaunchedEffect(kombinId) {
        if (kombinId != 0L) {
            viewModel.loadKombinForEdit(kombinId)
        }
    }
    var showKiyaketSecim by remember { mutableStateOf<KiyaketSlot?>(null) }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) onKombinSaved()
    }

    if (showKiyaketSecim != null) {
        KiyaketSecimBottomSheet(
            slot = showKiyaketSecim!!,
            kiyaketler = uiState.kiyaketler,
            seciliKiyaket = when (showKiyaketSecim) {
                KiyaketSlot.UST   -> uiState.seciliUst
                KiyaketSlot.ALT   -> uiState.seciliAlt
                KiyaketSlot.DIS   -> uiState.seciliDis
                KiyaketSlot.AYAK  -> uiState.seciliAyak
                KiyaketSlot.AKSESUAR -> uiState.seciliAksesuar
                null -> null
            },
            onSelect = { kiyaket ->
                viewModel.secKiyaket(showKiyaketSecim!!, kiyaket)
                showKiyaketSecim = null
            },
            onClear = {
                viewModel.temizleSlot(showKiyaketSecim!!)
                showKiyaketSecim = null
            },
            onDismiss = { showKiyaketSecim = null },
            isDark = isDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kombin Oluştur") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                    )
                )
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Kombin adı
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Kombin Adı",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = kombinAdi,
                    onValueChange = { kombinAdi = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("örn: Günlük Casual") },
                    leadingIcon = { Icon(Icons.Default.Style, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Kıyafet Seçimi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Grey100 else Grey900
            )
            Spacer(Modifier.height(12.dp))

            // Slot kartları
            SlotCard(
                slot = KiyaketSlot.UST,
                label = "Üst Giyim",
                icon = Icons.Default.Checkroom,
                kiyaket = uiState.seciliUst,
                onClick = { showKiyaketSecim = KiyaketSlot.UST },
                isDark = isDark
            )
            Spacer(Modifier.height(10.dp))
            SlotCard(
                slot = KiyaketSlot.ALT,
                label = "Alt Giyim",
                icon = Icons.Default.Straighten,
                kiyaket = uiState.seciliAlt,
                onClick = { showKiyaketSecim = KiyaketSlot.ALT },
                isDark = isDark
            )
            Spacer(Modifier.height(10.dp))
            SlotCard(
                slot = KiyaketSlot.DIS,
                label = "Dış Giyim (Opsiyonel)",
                icon = Icons.Default.Layers,
                kiyaket = uiState.seciliDis,
                onClick = { showKiyaketSecim = KiyaketSlot.DIS },
                isDark = isDark
            )
            Spacer(Modifier.height(10.dp))
            SlotCard(
                slot = KiyaketSlot.AYAK,
                label = "Ayakkabı",
                icon = Icons.Default.DirectionsWalk,
                kiyaket = uiState.seciliAyak,
                onClick = { showKiyaketSecim = KiyaketSlot.AYAK },
                isDark = isDark
            )
            Spacer(Modifier.height(10.dp))
            SlotCard(
                slot = KiyaketSlot.AKSESUAR,
                label = "Aksesuar (Opsiyonel)",
                icon = Icons.Default.Watch,
                kiyaket = uiState.seciliAksesuar,
                onClick = { showKiyaketSecim = KiyaketSlot.AKSESUAR },
                isDark = isDark
            )

            Spacer(Modifier.height(24.dp))

            uiState.hata?.let {
                Text(it, color = Error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            GlassButton(
                onClick = {
                    viewModel.kaydet(
                        Kombin(
                            id = if(kombinId != 0L) kombinId else 0L,
                            ad = kombinAdi.ifBlank { "Yeni Kombin" },
                            ustGiyim = uiState.seciliUst,
                            altGiyim = uiState.seciliAlt,
                            disGiyim = uiState.seciliDis,
                            ayakkabi = uiState.seciliAyak,
                            aksesuar = uiState.seciliAksesuar
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.yukleniyor && (uiState.seciliUst != null || uiState.seciliAlt != null)
            ) {
                if (uiState.yukleniyor) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = White)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Kombini Kaydet")
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("İptal", color = if (isDark) Grey400 else Grey600)
            }
        }
    }
}

@Composable
private fun SlotCard(
    slot: KiyaketSlot,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    kiyaket: Kiyaket?,
    onClick: () -> Unit,
    isDark: Boolean
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassSurface(modifier = Modifier.size(56.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (kiyaket != null) PrimaryLight else if (isDark) Grey500 else Grey400,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Grey500 else Grey600
                )
                if (kiyaket != null) {
                    Text(
                        "${kiyaket.marka} — ${kiyaket.tur.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Grey100 else Grey900
                    )
                    Text(
                        "${kiyaket.beden}${if (!kiyaket.renk.isNullOrBlank()) " · ${kiyaket.renk}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Grey400 else Grey600
                    )
                } else {
                    Text(
                        "Seçmek için dokun",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey600 else Grey400
                    )
                }
            }
            Icon(
                if (kiyaket != null) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = if (kiyaket != null) PrimaryLight else if (isDark) Grey600 else Grey400,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KiyaketSecimBottomSheet(
    slot: KiyaketSlot,
    kiyaketler: List<Kiyaket>,
    seciliKiyaket: Kiyaket?,
    onSelect: (Kiyaket) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val filtreli = kiyaketler.filter { k ->
        when (slot) {
            KiyaketSlot.UST  -> k.tur.name in listOf("TISORT", "GOMLEK", "SWEAT", "HIRKA", "ELBISE")
            KiyaketSlot.ALT  -> k.tur.name in listOf("PANTOLON", "ETEK", "SORT")
            KiyaketSlot.DIS  -> k.tur.name in listOf("CEKET", "KABAN", "MONTO", "YAGMURLUK")
            KiyaketSlot.AYAK -> k.tur.name in listOf("AYAKKABI", "TERLIK", "SANTRAFOR", "BOT")
            KiyaketSlot.AKSESUAR -> k.tur.name in listOf("CANTA", "SAPKA", "ESARP", "TAKI", "CORAP")
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kıyafet Seç",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (seciliKiyaket != null) {
                    TextButton(onClick = onClear) {
                        Text("Temizle", color = Error)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            if (filtreli.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Bu kategoride kıyafet bulunamadı.\nÖnce dolabına kıyafet ekle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey500 else Grey600
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtreli) { kiyaket ->
                        val isSelected = seciliKiyaket?.id == kiyaket.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(kiyaket) }
                                .clip(RoundedCornerShape(12.dp)),
                            color = if (isSelected)
                                PrimaryLight.copy(alpha = 0.15f)
                            else
                                if (isDark) GlassDark else GlassLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) GlassDarkSurface else GlassLightSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Checkroom,
                                        null,
                                        tint = if (isSelected) PrimaryLight else if (isDark) Grey500 else Grey400,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        kiyaket.marka,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${kiyaket.tur.displayName} · ${kiyaket.beden}" +
                                                if (!kiyaket.renk.isNullOrBlank()) " · ${kiyaket.renk}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Grey400 else Grey600
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, null, tint = PrimaryLight)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

enum class KiyaketSlot { UST, ALT, DIS, AYAK, AKSESUAR }

