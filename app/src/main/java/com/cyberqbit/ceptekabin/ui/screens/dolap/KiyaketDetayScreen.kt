package com.cyberqbit.ceptekabin.ui.screens.dolap

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiyaketDetayScreen(
    kiyaketId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: KiyaketDetayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = true
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(kiyaketId) { viewModel.loadKiyaket(kiyaketId) }
    LaunchedEffect(uiState.deleted) { if (uiState.deleted) onNavigateBack() }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("KÄ±yafeti Sil") },
            text  = { Text("Bu kÄ±yafeti dolabÄ±ndan silmek istediÄŸinden emin misin?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(); showDeleteDialog = false }) {
                    Text("Sil", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Ä°ptal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.kiyaket?.marka ?: "KÄ±yafet Detay") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    uiState.kiyaket?.let { k ->
                        IconButton(onClick = { onNavigateToEdit(k.id) }) {
                            Icon(Icons.Default.Edit, "DÃ¼zenle")
                        }
                        IconButton(onClick = { viewModel.toggleFavori(k) }) {
                            Icon(
                                if (k.favori) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                "Favori",
                                tint = if (k.favori) Error else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Sil", tint = Error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                )
            )
        }
    ) { padding ->
        when {
            uiState.yukleniyor -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = PrimaryLight)
            }
            uiState.kiyaket == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = Error)
                    Spacer(Modifier.height(12.dp))
                    Text("KÄ±yafet bulunamadÄ±", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                val k = uiState.kiyaket!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(
                            if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                        ))
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // â”€â”€ GÃ¶rsel kartÄ± â”€â”€ #11 FIX: aspectRatio + ContentScale.Fit â”€â”€
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        if (!k.imageUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model              = k.imageUrl,
                                contentDescription = k.marka,
                                modifier           = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3f / 4f)   // dikey oran â€” kaban/pantolon kesilmez
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale       = ContentScale.Fit  // asla kÄ±rpma
                            )
                        } else {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3f / 4f),
                                Alignment.Center
                            ) {
                                Icon(Icons.Default.Checkroom, null,
                                    Modifier.size(80.dp), tint = PrimaryLight)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(k.marka, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Grey100 else Grey900)
                        k.model?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Grey400 else Grey600)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // â”€â”€ ÃœrÃ¼n bilgileri â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ÃœrÃ¼n Bilgileri", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Grey100 else Grey900)
                        Spacer(Modifier.height(12.dp))
                        DetayRow(Icons.Default.GridView,    "Kategori", k.kategoriDisplayName(), isDark)
                        DetayRow(Icons.Default.Category,    "TÃ¼r",      k.tur.displayName, isDark)
                        if (k.beden.isNotBlank()) DetayRow(Icons.Default.Straighten, "Beden", k.beden, isDark)
                        k.renk?.let { DetayRow(Icons.Default.Palette,    "Renk",     it, isDark) }
                        DetayRow(Icons.Default.WbSunny,    "Mevsim",   k.mevsim.displayName, isDark)
                        k.sezon?.let { DetayRow(Icons.Default.DateRange, "Sezon",    it, isDark) }
                        k.barkod?.let { DetayRow(Icons.Default.QrCode,   "Barkod",   it, isDark) }
                    }

                    // â”€â”€ Cost Per Wear kartÄ± â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                    if (k.satinAlmaFiyati != null && k.satinAlmaFiyati > 0) {
                        Spacer(Modifier.height(12.dp))
                        CostPerWearCard(fiyat = k.satinAlmaFiyati, kullanimSayisi = k.kullanimSayisi, isDark = isDark)
                    }

                    k.not?.let { not ->
                        Spacer(Modifier.height(12.dp))
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Notlar", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Grey100 else Grey900)
                            Spacer(Modifier.height(8.dp))
                            Text(not, style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Grey300 else Grey700)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    GlassButton(
                        onClick  = { viewModel.incrementKullanim(k.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bu KÄ±yafeti Giydim (+1 kullanÄ±m)")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Toplam kullanÄ±m: ${k.kullanimSayisi} kez",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = if (isDark) Grey500 else Grey600,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

// â”€â”€ Cost Per Wear bileÅŸeni â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@Composable
fun CostPerWearCard(fiyat: Double, kullanimSayisi: Int, isDark: Boolean) {
    val cpw = if (kullanimSayisi > 0) fiyat / kullanimSayisi else fiyat
    val fmt = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text("GiyiniÅŸ BaÅŸÄ± Maliyet",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900)
                Text("${fmt.format(fiyat)} Ã· $kullanimSayisi kez",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey500 else Grey600)
            }
            Text(
                fmt.format(cpw),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = when {
                    cpw < 10  -> Success
                    cpw < 50  -> AccentGold
                    else      -> Error
                }
            )
        }
    }
}

// â”€â”€ YardÄ±mcÄ± â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
private fun Kiyaket.kategoriDisplayName(): String {
    return when (tur.name) {
        "TISORT","GOMLEK","KAZAK","HIRKA","SWEAT","SWEATSHIRT","ELBISE",
        "POLO","BLUZ","CROP_TOP","TANK_TOP","ATLET" -> "Ãœst Giyim"
        "PANTOLON","ETEK","SORT","ESOFMAN","TAYT","JOGGER",
        "CHINO","BERMUDA" -> "Alt Giyim"
        "CEKET","KABAN","MONTO","YAGMURLUK","KABAN_MONT",
        "BLAZER","PARKA","TRENC","DERI_CEKET","BOMBER" -> "DÄ±ÅŸ Giyim"
        "AYAKKABI","TERLIK","BOT","SANTRAFOR","KADIN_AYAKKABISI",
        "ERKEK_AYAKKABISI","COCUK_AYAKKABISI","SNEAKER","LOAFER",
        "CIZME","OXFORD","MOKASEN" -> "AyakkabÄ±"
        else -> "Aksesuar"
    }
}

@Composable
private fun DetayRow(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryLight, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text("$label:", style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Grey500 else Grey600,
            modifier = Modifier.width(72.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Grey100 else Grey900)
    }
}
