package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KombinScreen(
    viewModel: KombinViewModel,
    onNavigateToKombinDetay: (Long) -> Unit,
    onNavigateToKombinOlustur: () -> Unit
) {
    val kombinler by viewModel.kombinler.collectAsState()
    val favorilerOnly by viewModel.favorilerOnly.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kombinlerim") },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorilerOnly() }) {
                        Icon(
                            if (favorilerOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoriler",
                            tint = if (favorilerOnly) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToKombinOlustur) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Kombin")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (kombinler.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Style,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (favorilerOnly) "Favori kombinin yok" else "Henüz kombin oluşturmadın",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kombinlerini oluşturmaya başla",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(kombinler) { kombin ->
                        KombinCard(
                            kombin = kombin,
                            onClick = { onNavigateToKombinDetay(kombin.id) },
                            onToggleFavori = { viewModel.toggleFavori(kombin) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KombinCard(
    kombin: Kombin,
    onClick: () -> Unit,
    onToggleFavori: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = kombin.ad,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onToggleFavori) {
                    Icon(
                        if (kombin.favori) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (kombin.favori) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Kombin öğeleri ikonları
                listOfNotNull(
                    kombin.ustGiyim,
                    kombin.altGiyim,
                    kombin.ayakkabi
                ).take(3).forEach { kiyaket ->
                    GlassSurface(modifier = Modifier.size(48.dp)) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Checkroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${kombin.puan} puan",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
