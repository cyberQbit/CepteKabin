package com.cyberqbit.ceptekabin.ui.screens.dolap

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
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DolapScreen(
    onNavigateToTarama: () -> Unit,
    onNavigateToKiyaketDetay: (Long) -> Unit,
    viewModel: DolapViewModel = hiltViewModel()
) {
    val kiyaketler by viewModel.kiyaketler.collectAsState()
    val seciliKategori by viewModel.seciliKategori.collectAsState()
    val kategoriler = listOf("Tümü", "Üst Giyim", "Alt Giyim", "Dış Giyim", "Ayakkabı", "Aksesuar")

    var aramaQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dolabım") },
                actions = {
                    IconButton(onClick = onNavigateToTarama) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Barkod Tara")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToTarama) {
                Icon(Icons.Default.Add, contentDescription = "Kıyafet Ekle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Arama
            OutlinedTextField(
                value = aramaQuery,
                onValueChange = { aramaQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Kıyafet ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kategori Filtreleri
            ScrollableTabRow(
                selectedTabIndex = kategoriler.indexOf(seciliKategori),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                kategoriler.forEachIndexed { index, kategori ->
                    Tab(
                        selected = seciliKategori == kategori,
                        onClick = { viewModel.kategoriSec(kategori) },
                        text = { Text(kategori) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kıyafet Listesi
            if (kiyaketler.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Checkroom,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Dolabın boş",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Barkod tarayarak kıyafet ekle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(kiyaketler) { kiyaket ->
                        KiyaketCard(
                            kiyaket = kiyaket,
                            onClick = { onNavigateToKiyaketDetay(kiyaket.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KiyaketCard(
    kiyaket: Kiyaket,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassSurface(
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Checkroom,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kiyaket.marka,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = kiyaket.tur.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = kiyaket.beden,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = " • ",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = kiyaket.renk ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (kiyaket.favori) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favori",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
