package com.cyberqbit.ceptekabin.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDolapScreen(
    friendUserId: String,
    onNavigateBack: () -> Unit,
    viewModel: FriendDolapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark  = true

    LaunchedEffect(friendUserId) { viewModel.loadFriendWardrobe(friendUserId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.friendName.ifBlank { "ArkadaÅŸÄ±n DolabÄ±" },
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Salt okunur gÃ¶rÃ¼nÃ¼m",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Grey500 else Grey600)
                    }
                },
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
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = PrimaryLight)
            }
            uiState.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = Error)
                    Spacer(Modifier.height(12.dp))
                    Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Grey400 else Grey600)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.loadFriendWardrobe(friendUserId) }) {
                        Text("Tekrar Dene", color = PrimaryLight)
                    }
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize()
                        .background(Brush.verticalGradient(
                            if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                        ))
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        // ArkadaÅŸ profil Ã¶zeti
                        FriendProfileBanner(
                            name         = uiState.friendName,
                            kiyafetSayisi = uiState.kiyaketler.size,
                            isDark       = isDark
                        )
                    }

                    if (uiState.kiyaketler.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Checkroom, null,
                                        Modifier.size(48.dp),
                                        tint = if (isDark) Grey600 else Grey400)
                                    Spacer(Modifier.height(8.dp))
                                    Text("ArkadaÅŸÄ±n dolabÄ± boÅŸ.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) Grey500 else Grey600)
                                }
                            }
                        }
                    } else {
                        items(uiState.kiyaketler) { kiyaket ->
                            FriendKiyaketCard(kiyaket = kiyaket, isDark = isDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendProfileBanner(name: String, kiyafetSayisi: Int, isDark: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(PrimaryLight, SecondaryDark))),
                Alignment.Center
            ) {
                Text(
                    name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = White
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Grey100 else Grey900)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Checkroom, null, Modifier.size(13.dp),
                        tint = if (isDark) Grey500 else Grey600)
                    Spacer(Modifier.width(4.dp))
                    Text("$kiyafetSayisi kÄ±yafet",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Grey500 else Grey600)
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = PrimaryLight.copy(alpha = 0.12f)
            ) {
                Text("Salt Okunur", style = MaterialTheme.typography.labelSmall,
                    color = PrimaryLight,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun FriendKiyaketCard(kiyaket: Kiyaket, isDark: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassSurface(modifier = Modifier.size(64.dp)) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    if (!kiyaket.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = kiyaket.imageUrl, contentDescription = kiyaket.marka,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Checkroom, null, Modifier.size(32.dp), tint = PrimaryLight)
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(kiyaket.marka, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900)
                Text(kiyaket.tur.displayName, style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey400 else Grey600)
                if (!kiyaket.renk.isNullOrBlank()) {
                    Text(kiyaket.renk, style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Grey500 else Grey600)
                }
            }
        }
    }
}
