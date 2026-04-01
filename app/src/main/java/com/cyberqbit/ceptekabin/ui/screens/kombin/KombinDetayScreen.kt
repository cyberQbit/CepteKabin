package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.foundation.clickable
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
    onNavigateToKiyaket: (Long) -> Unit = {},
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
                            }

                            Spacer(Modifier.height(16.dp))

                            // Kıyafet slotları
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KombinSlotCard("Üst", k.ustGiyim, Modifier.weight(1f)) { k.ustGiyim?.let { onNavigateToKiyaket(it.id) } }
                                KombinSlotCard("Alt", k.altGiyim, Modifier.weight(1f)) { k.altGiyim?.let { onNavigateToKiyaket(it.id) } }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KombinSlotCard("Dış", k.disGiyim, Modifier.weight(1f)) { k.disGiyim?.let { onNavigateToKiyaket(it.id) } }
                                KombinSlotCard("Ayakkabı", k.ayakkabi, Modifier.weight(1f)) { k.ayakkabi?.let { onNavigateToKiyaket(it.id) } }
                            }
                            k.aksesuar?.let { aks ->
                                Spacer(Modifier.height(10.dp))
                                KombinSlotCard("Aksesuar", aks, Modifier.fillMaxWidth()) { onNavigateToKiyaket(aks.id) }
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
                            val logoUri = com.cyberqbit.ceptekabin.util.KombinShareHelper.getPromoImageUri(context)

                            if (shareUri != null && logoUri != null) {
                                val promosyonMesaji = """
                                    Hey! CepteKabin uygulamasında sana özel harika bir kombin hazırladım. 🤩👗👔
                                    
                                    ✨ Eğer uygulaman zaten yüklüyse, hemen aşağıdaki .kmb dosyasına tıklayarak kombini anında dolabına ekleyebilirsin!
                                    
                                    ⚠️ Henüz CepteKabin'in yok mu? Çok basit:
                                    1️⃣ Önce şu linkten uygulamayı ücretsiz indir ve kur:
                                    🔗 https://bit.ly/CepteKabinApp
                                    
                                    2️⃣ Kurulum bittikten sonra bu sohbete geri dön ve bu sohbetteki belgeye (.kmb dosyasına) tıkla!
                                    
                                    Sihir o an gerçekleşecek ve kombin dolabında belirecek! 🪄
                                """.trimIndent()

                                val uris = java.util.ArrayList<android.net.Uri>().apply {
                                    add(logoUri)
                                    add(shareUri)
                                }

                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                    type = "*/*"
                                    putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                                    putExtra(android.content.Intent.EXTRA_TEXT, promosyonMesaji)
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Sana Harika Bir Kombin Gönderdim!")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val chooser = android.content.Intent.createChooser(shareIntent, "Kombini Paylaş")
                                context.startActivity(chooser)
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
private fun KombinSlotCard(label: String, kiyaket: Kiyaket?, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val mod = if (onClick != null) modifier.clickable { onClick() } else modifier
    GlassSurface(modifier = mod) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (kiyaket != null && !kiyaket.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = kiyaket.imageUrl,
                    contentDescription = kiyaket.marka,
                    modifier = Modifier.size(64.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Checkroom, null, Modifier.size(64.dp), tint = if (kiyaket != null) PrimaryLight else Grey400)
            }
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
