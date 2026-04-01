package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KombinDetayScreen(
    kombinId: Long,
    onNavigateBack: () -> Unit,
    viewModel: KombinViewModel = hiltViewModel()
) {
    var kombin by remember { mutableStateOf<Kombin?>(null) }
    var yukleniyor by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // FIX: actually load the kombin data
    LaunchedEffect(kombinId) {
        yukleniyor = true
        kombin = viewModel.getKombinById(kombinId)
        yukleniyor = false
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Kombini Sil") },
            text = { Text("Bu kombini silmek istediğinden emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    kombin?.let {
                        viewModel.deleteKombin(it)
                        onNavigateBack()
                    }
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
                title = { Text(kombin?.ad ?: "Kombin Detay") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    kombin?.let { k ->
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
                }
            )
        }
    ) { padding ->
        when {
            yukleniyor -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryLight)
                }
            }
            kombin == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Kombin bulunamadı.")
                }
            }
            else -> {
                val k = kombin!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(k.ad, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.toggleFavori(k) }) {
                                    Icon(
                                        if (k.favori) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        "Favori",
                                        tint = if (k.favori) Error else LocalContentColor.current
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Kıyafet slotları
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KombinSlotCard("Üst", k.ustGiyim, Modifier.weight(1f))
                                KombinSlotCard("Alt", k.altGiyim, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KombinSlotCard("Dış", k.disGiyim, Modifier.weight(1f))
                                KombinSlotCard("Ayakkabı", k.ayakkabi, Modifier.weight(1f))
                            }
                            k.aksesuar?.let { aks ->
                                Spacer(Modifier.height(10.dp))
                                KombinSlotCard("Aksesuar", aks, Modifier.fillMaxWidth())
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, Modifier.size(18.dp), tint = AccentGold)
                                Spacer(Modifier.width(4.dp))
                                Text("${k.puan} puan", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.incrementPuan(k.id)
                            kombin = k.copy(puan = k.puan + 1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Icon(Icons.Default.ThumbUp, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bu Kombini Giydim (+1 puan)")
                    }

                    Spacer(Modifier.height(12.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val coroutineScope = rememberCoroutineScope()

                    com.cyberqbit.ceptekabin.ui.components.GlassButton(onClick = {
                        coroutineScope.launch {
                            // Kombin kıyafetlerini listeye çevir
                            val kombinKiyafetleri = listOfNotNull(k.ustGiyim, k.altGiyim, k.disGiyim, k.ayakkabi, k.aksesuar)
                            
                            val shareUri = com.cyberqbit.ceptekabin.util.KombinShareHelper.createKmbFile(context, k, kombinKiyafetleri)
                            
                            if (shareUri != null) {
                                val promosyonMesaji = """
                                    Hey! CepteKabin uygulamasında sana özel harika bir kombin hazırladım. 🤩👗👔
                                    
                                    Eğer uygulaman varsa ekteki .kmb dosyasına tıklayarak bu kombini anında kendi dolabına ekleyebilirsin!
                                    
                                    Henüz CepteKabin'in yok mu? Hemen ücretsiz indir:
                                    👉 https://bit.ly/CepteKabinApp
                                """.trimIndent()

                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/octet-stream"
                                    putExtra(android.content.Intent.EXTRA_STREAM, shareUri)
                                    putExtra(android.content.Intent.EXTRA_TEXT, promosyonMesaji)
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Sana Harika Bir Kombin Gönderdim!")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Kombini Paylaş"))
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kombini Gönder")
                    }
                }
            }
        }
    }
}

@Composable
private fun KombinSlotCard(label: String, kiyaket: Kiyaket?, modifier: Modifier = Modifier) {
    GlassSurface(modifier = modifier) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Checkroom, null, Modifier.size(28.dp), tint = if (kiyaket != null) PrimaryLight else Grey400)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Grey500)
            if (kiyaket != null) {
                Text(kiyaket.marka, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(kiyaket.tur.displayName, style = MaterialTheme.typography.labelSmall, color = Grey500)
            } else {
                Text("—", style = MaterialTheme.typography.bodySmall, color = Grey400)
            }
        }
    }
}
