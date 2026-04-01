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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiyaketDetayScreen(
    kiyaketId: Long,
    onNavigateBack: () -> Unit,
    viewModel: KiyaketDetayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(kiyaketId) {
        viewModel.loadKiyaket(kiyaketId)
    }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onNavigateBack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Kıyafeti Sil") },
            text = { Text("Bu kıyafeti dolabından silmek istediğinden emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete()
                    showDeleteDialog = false
                }) { Text("Sil", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.kiyaket?.marka ?: "Kıyafet Detay") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    uiState.kiyaket?.let { k ->
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
            uiState.yukleniyor -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryLight)
                }
            }
            uiState.kiyaket == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = Error)
                        Spacer(Modifier.height(12.dp))
                        Text("Kıyafet bulunamadı", style = MaterialTheme.typography.bodyLarge)
                    }
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
                    // Görsel / İkon kartı
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!k.imageUrl.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = k.imageUrl,
                                    contentDescription = k.marka,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Checkroom,
                                    null,
                                    modifier = Modifier.size(80.dp),
                                    tint = PrimaryLight
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            k.marka,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Grey100 else Grey900
                        )
                        k.model?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Detaylar
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Ürün Bilgileri",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Grey100 else Grey900
                        )
                        Spacer(Modifier.height(12.dp))
                        DetayRow(Icons.Default.Category, "Tür", k.tur.displayName, isDark)
                        DetayRow(Icons.Default.Straighten, "Beden", k.beden, isDark)
                        DetayRow(Icons.Default.Palette, "Renk", k.renk ?: "—", isDark)
                        DetayRow(Icons.Default.WbSunny, "Mevsim", k.mevsim.displayName, isDark)
                        k.sezon?.let { DetayRow(Icons.Default.DateRange, "Sezon", it, isDark) }
                        k.barkod?.let { DetayRow(Icons.Default.QrCode, "Barkod", it, isDark) }
                    }

                    k.not?.let { not ->
                        Spacer(Modifier.height(12.dp))
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Notlar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900)
                            Spacer(Modifier.height(8.dp))
                            Text(not, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey300 else Grey700)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    GlassButton(
                        onClick = { viewModel.incrementKullanim(k.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bu Kıyafeti Giydim (+1 kullanım)")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Toplam kullanım: ${k.kullanimSayisi} kez",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDark) Grey500 else Grey600,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetayRow(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PrimaryLight, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Grey500 else Grey600,
            modifier = Modifier.width(72.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Grey100 else Grey900
        )
    }
}
