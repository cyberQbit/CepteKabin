package com.cyberqbit.ceptekabin.ui.screens.kombin

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*
import com.cyberqbit.ceptekabin.util.KombinShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KombinDetayScreen(
    kombinId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToKiyaket: (Long) -> Unit = {},
    viewModel: KombinViewModel = hiltViewModel()
) {
    var kombin by remember { mutableStateOf<Kombin?>(null) }
    var yukleniyor by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    LaunchedEffect(kombinId) {
        yukleniyor = true
        kombin = viewModel.getKombinById(kombinId)
        yukleniyor = false
    }

    // Silme diyaloğu
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Kombini Sil") },
            text  = { Text("Bu kombini silmek istediğinden emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    kombin?.let { viewModel.deleteKombin(it); onNavigateBack() }
                }) { Text("Sil", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal") }
            }
        )
    }

    // Paylaşım alt sayfası
    if (showShareSheet && kombin != null) {
        KombinShareBottomSheet(
            kombin    = kombin!!,
            onDismiss = { showShareSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(kombin?.ad ?: "Kombin Detay") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    kombin?.let { k ->
                        IconButton(onClick = {
                            kombin = k.copy(favori = !k.favori)
                            viewModel.toggleFavori(k)
                        }) {
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
                }
            )
        }
    ) { padding ->
        when {
            yukleniyor -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryLight) }
            }
            kombin == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("Kombin bulunamadı.") }
            }
            else -> {
                val k = kombin!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // ── Kombin detay kartı ────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                k.ad,
                                style    = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                KombinSlotCard(
                                    "Üst", k.ustGiyim, Modifier.weight(1f)
                                ) { k.ustGiyim?.let { onNavigateToKiyaket(it.id) } }
                                KombinSlotCard(
                                    "Alt", k.altGiyim, Modifier.weight(1f)
                                ) { k.altGiyim?.let { onNavigateToKiyaket(it.id) } }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                KombinSlotCard(
                                    "Dış", k.disGiyim, Modifier.weight(1f)
                                ) { k.disGiyim?.let { onNavigateToKiyaket(it.id) } }
                                KombinSlotCard(
                                    "Ayakkabı", k.ayakkabi, Modifier.weight(1f)
                                ) { k.ayakkabi?.let { onNavigateToKiyaket(it.id) } }
                            }
                            k.aksesuar?.let { aks ->
                                Spacer(Modifier.height(10.dp))
                                KombinSlotCard(
                                    "Aksesuar", aks, Modifier.fillMaxWidth()
                                ) { onNavigateToKiyaket(aks.id) }
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star, null,
                                    Modifier.size(18.dp), tint = AccentGold
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${k.puan} puan",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Giydim butonu ─────────────────────────────────────────
                    Button(
                        onClick = {
                            viewModel.incrementPuan(k.id)
                            kombin = k.copy(puan = k.puan + 1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Icon(Icons.Default.ThumbUp, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bu Kombini Giydim (+1 puan)")
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Paylaş butonu ─────────────────────────────────────────
                    GlassButton(
                        onClick  = { showShareSheet = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş")
                        Spacer(Modifier.width(8.dp))
                        Text("Kombini Gönder")
                    }
                }
            }
        }
    }
}

// ── Paylaşım Alt Sayfası ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KombinShareBottomSheet(
    kombin: Kombin,
    onDismiss: () -> Unit
) {
    val context       = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark        = isSystemInDarkTheme()
    var isBuilding    by remember { mutableStateOf(false) }

    val kombinKiyafetleri = remember(kombin) {
        listOfNotNull(
            kombin.ustGiyim, kombin.altGiyim, kombin.disGiyim,
            kombin.ayakkabi, kombin.aksesuar
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            Text(
                "Kombini Paylaş",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "\"${kombin.ad}\" kombinini nasıl göndermek istersin?",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Grey400 else Grey600
            )

            Spacer(Modifier.height(24.dp))

            // ── Seçenek 1: .kmb dosyası ───────────────────────────────────────
            ShareOptionCard(
                emoji       = if (isBuilding) null else "📦",
                isLoading   = isBuilding,
                title       = "Kombin Dosyası Gönder",
                description = "Arkadaşın CepteKabin kuruluysa bunu seç",
                tint        = PrimaryLight,
                isDark      = isDark,
                onClick     = {
                    if (!isBuilding) {
                        isBuilding = true
                        coroutineScope.launch {
                            val intent = KombinShareHelper.createFileShareIntent(
                                context, kombin, kombinKiyafetleri
                            )
                            isBuilding = false
                            onDismiss()
                            if (intent != null) {
                                context.startActivity(
                                    Intent.createChooser(intent, "${kombin.ad} Paylaş")
                                )
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // ── Seçenek 2: Davet linki ────────────────────────────────────────
            ShareOptionCard(
                emoji       = "🔗",
                isLoading   = false,
                title       = "Davet Linki Gönder",
                description = "Arkadaşın henüz uygulamayı yoksa bunu seç",
                tint        = AccentGold,
                isDark      = isDark,
                onClick     = {
                    val intent = KombinShareHelper.createInviteIntent(kombin)
                    onDismiss()
                    context.startActivity(Intent.createChooser(intent, "Davet Linki Paylaş"))
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ShareOptionCard(
    emoji       : String?,
    isLoading   : Boolean,
    title       : String,
    description : String,
    tint        : androidx.compose.ui.graphics.Color,
    isDark      : Boolean,
    onClick     : () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = if (isDark) GlassDarkSurface else GlassLightSurface
    ) {
        Row(
            modifier           = Modifier.padding(16.dp),
            verticalAlignment  = Alignment.CenterVertically
        ) {
            // İkon kutusu
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color       = tint
                        )
                    } else {
                        Text(emoji ?: "📦", fontSize = 26.sp)
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDark) Grey100 else Grey900
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey500 else Grey600
                )
            }

            Icon(
                Icons.Default.ChevronRight, null,
                tint = if (isDark) Grey500 else Grey400
            )
        }
    }
}

// ── Slot kartı ────────────────────────────────────────────────────────────────

@Composable
private fun KombinSlotCard(
    label    : String,
    kiyaket  : Kiyaket?,
    modifier : Modifier = Modifier,
    onClick  : (() -> Unit)? = null
) {
    val mod = if (onClick != null) modifier.clickable { onClick() } else modifier
    GlassSurface(modifier = mod) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (kiyaket != null && !kiyaket.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model          = kiyaket.imageUrl,
                    contentDescription = kiyaket.marka,
                    modifier       = Modifier.size(64.dp),
                    contentScale   = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Checkroom, null,
                    Modifier.size(64.dp),
                    tint = if (kiyaket != null) PrimaryLight else Grey400
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Grey500)
            if (kiyaket != null) {
                Text(
                    kiyaket.marka,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    kiyaket.tur.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Grey500
                )
            } else {
                Text("—", style = MaterialTheme.typography.bodySmall, color = Grey400)
            }
        }
    }
}
