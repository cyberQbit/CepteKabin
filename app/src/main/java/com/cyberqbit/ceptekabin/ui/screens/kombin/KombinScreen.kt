package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.ui.theme.*

enum class KombinSiralama(val label: String) {
    EN_YENI("En yeni"), FAVORILER("Favoriler"), EN_COK_GIYILEN("En Ã§ok giyilen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KombinScreen(
    viewModel: KombinViewModel,
    onNavigateToKombinDetay: (Long) -> Unit,
    onNavigateToKombinOlustur: () -> Unit,
    onNavigateToDolap: () -> Unit = {}
) {
    val kombinler by viewModel.kombinler.collectAsState()
    val favorilerOnly by viewModel.favorilerOnly.collectAsState()
    val siralama by viewModel.siralama.collectAsState()
    val dolapBos by viewModel.dolapBos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isDark = true
    var showSiralamaMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kombinlerim") },
                actions = {
                    Box {
                        IconButton(onClick = { showSiralamaMenu = true }) { Icon(Icons.Default.Sort, "SÄ±rala") }
                        DropdownMenu(expanded = showSiralamaMenu, onDismissRequest = { showSiralamaMenu = false }) {
                            KombinSiralama.entries.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (siralama == s) { Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = PrimaryLight); Spacer(Modifier.width(8.dp)) }
                                            Text(s.label)
                                        }
                                    },
                                    onClick = { viewModel.setSiralama(s); showSiralamaMenu = false }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.toggleFavorilerOnly() }) {
                        Icon(if (favorilerOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favoriler",
                            tint = if (favorilerOnly) MaterialTheme.colorScheme.error else LocalContentColor.current)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isDark) SurfaceDark else SurfaceLight)
            )
        },
        floatingActionButton = {
            if (!dolapBos) {
                FloatingActionButton(
                    onClick = onNavigateToKombinOlustur,
                    containerColor = PrimaryLight,
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Default.Add, "Yeni Kombin", tint = White)
                }
            }
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize()
            .background(Brush.verticalGradient(if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)))
            .padding(paddingValues).padding(horizontal = 16.dp)) {

            if (isLoading) {
                Column(Modifier.fillMaxSize().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) {
                        com.cyberqbit.ceptekabin.ui.components.ShimmerCard(
                            modifier = Modifier.fillMaxWidth().height(130.dp),
                            isDark = isDark
                        ) {}
                    }
                }
            } else if (kombinler.isEmpty()) {
                SlideUpFadeIn(visible = true) {
                    EmptyKombinState(dolapBos, isDark, onNavigateToDolap, onNavigateToKombinOlustur)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(kombinler, key = { it.id }) { kombin ->
                        KombinCard(kombin, isDark,
                            onClick = { onNavigateToKombinDetay(kombin.id) },
                            onShareClick = { viewModel.shareKombin(kombin) },
                            onFavoriToggle = { viewModel.toggleFavori(kombin) })
                    }
                }
            }
        }
    }
}

@Composable
private fun KombinCard(kombin: Kombin, isDark: Boolean, onClick: () -> Unit,
    onShareClick: () -> Unit, onFavoriToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "kombin_card_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Grey800.copy(alpha = 0.5f) else White.copy(alpha = 0.9f),
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(kombin.ad, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, "PaylaÅŸ", Modifier.size(18.dp), tint = if (isDark) Grey400 else Grey600)
                    }
                    IconButton(onClick = onFavoriToggle, modifier = Modifier.size(32.dp)) {
                        Icon(if (kombin.favori) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favori",
                            Modifier.size(18.dp), tint = if (kombin.favori) MaterialTheme.colorScheme.error
                            else if (isDark) Grey400 else Grey600)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                kombin.ustGiyim?.let { KiyafetThumbnail(it, "Ãœst", isDark) }
                kombin.altGiyim?.let { KiyafetThumbnail(it, "Alt", isDark) }
                kombin.disGiyim?.let { KiyafetThumbnail(it, "DÄ±ÅŸ", isDark) }
                kombin.ayakkabi?.let { KiyafetThumbnail(it, "Ayak", isDark) }
                kombin.aksesuar?.let { KiyafetThumbnail(it, "Aks", isDark) }
            }
            Spacer(Modifier.height(6.dp))
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("tr"))
            Text(sdf.format(java.util.Date(kombin.olusturmaTarihi)),
                style = MaterialTheme.typography.labelSmall, color = Grey500)
        }
    }
}

@Composable
private fun KiyafetThumbnail(kiyaket: Kiyaket, label: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
        Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))
            .background(if (isDark) Grey700 else Grey200), contentAlignment = Alignment.Center) {
            if (kiyaket.imageUrl != null) {
                AsyncImage(model = kiyaket.imageUrl, contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
            } else {
                Icon(Icons.Default.Checkroom, null, Modifier.size(22.dp), tint = if (isDark) Grey500 else Grey400)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDark) Grey500 else Grey600, fontSize = 9.sp)
    }
}

@Composable
private fun EmptyKombinState(dolapBos: Boolean, isDark: Boolean,
    onNavigateToDolap: () -> Unit, onNavigateToKombinOlustur: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Surface(shape = CircleShape, color = PrimaryLight.copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Style, null, Modifier.size(40.dp), tint = PrimaryLight) }
            }
            Spacer(Modifier.height(18.dp))
            if (dolapBos) {
                Text("Ã–nce dolabÄ±na kÄ±yafet ekle,\nsonra kombinle!", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = onNavigateToDolap, colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                    Text("Dolaba Git", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text("HenÃ¼z kombin oluÅŸturmadÄ±n", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text("KÄ±yafetlerini birleÅŸtirerek harika kombinler yarat!", style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Grey400 else Grey600, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = onNavigateToKombinOlustur, colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                    Text("Ä°lk Kombini OluÅŸtur", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
