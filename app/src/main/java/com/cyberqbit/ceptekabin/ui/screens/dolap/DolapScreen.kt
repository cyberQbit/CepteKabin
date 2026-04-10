package com.cyberqbit.ceptekabin.ui.screens.dolap

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.screens.home.RenkDairesi
import com.cyberqbit.ceptekabin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DolapScreen(
    onNavigateToTarama: () -> Unit,
    onNavigateToKiyaketDetay: (Long) -> Unit,
    onNavigateToKiyaketEkle: () -> Unit,
    viewModel: DolapViewModel = hiltViewModel()
) {
    val kiyafetler by viewModel.kiyafetler.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()

    val isDark = true
    val categories = listOf("TÃ¼mÃ¼", "Ãœst Giyim", "Alt Giyim", "DÄ±ÅŸ Giyim", "AyakkabÄ±", "Aksesuar")
    val selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    val filteredList = kiyafetler.filter { kiyaket ->
        val categoryMatch = selectedCategory == "TÃ¼mÃ¼" || kiyaket.kategori == selectedCategory
        val searchMatch = searchQuery.isBlank() ||
            kiyaket.marka.contains(searchQuery, ignoreCase = true) ||
            kiyaket.tur.displayName.contains(searchQuery, ignoreCase = true) ||
            (kiyaket.renk ?: "").contains(searchQuery, ignoreCase = true)
        categoryMatch && searchMatch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isMultiSelectMode) "${selectedIds.size} seÃ§ili" else "DolabÄ±m") },
                navigationIcon = {
                    if (isMultiSelectMode) {
                        IconButton(onClick = { viewModel.exitMultiSelect() }) { Icon(Icons.Default.Close, "Ä°ptal") }
                    }
                },
                actions = {
                    if (isMultiSelectMode) {
                        IconButton(onClick = { viewModel.deleteSelected() }, enabled = selectedIds.isNotEmpty()) {
                            Icon(Icons.Default.Delete, "Sil",
                                tint = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                        }
                    } else {
                        IconButton(onClick = onNavigateToTarama) { Icon(Icons.Default.QrCodeScanner, "Barkod Tara") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isDark) SurfaceDark else SurfaceLight)
            )
        },
        floatingActionButton = {
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = onNavigateToKiyaketEkle,
                    containerColor = PrimaryLight,
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Default.Add, "KÄ±yafet Ekle", tint = White)
                }
            }
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize()
            .background(Brush.verticalGradient(if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)))
            .padding(paddingValues)) {

            OutlinedTextField(
                value = searchQuery, onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Marka, tÃ¼r veya renk ara...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, "Temizle") }
                    }
                },
                singleLine = true, shape = RoundedCornerShape(14.dp)
            )

            ScrollableTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp, containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = PrimaryLight)
                    }
                }) {
                categories.forEachIndexed { index, category ->
                    Tab(selected = selectedTabIndex == index,
                        onClick = { viewModel.setCategory(category) },
                        text = {
                            val count = if (category == "TÃ¼mÃ¼") kiyafetler.size
                            else kiyafetler.count { it.kategori == category }
                            Text("$category ($count)",
                                fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal)
                        })
                }
            }

            if (kiyafetler.isEmpty()) {
                SlideUpFadeIn(visible = true) {
                    EmptyDolapState(onBarkodTara = onNavigateToTarama, onManuelEkle = onNavigateToKiyaketEkle, isDark = isDark)
                }
            } else if (filteredList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, Modifier.size(48.dp), tint = if (isDark) Grey500 else Grey400)
                        Spacer(Modifier.height(12.dp))
                        Text("Bu filtreyle kÄ±yafet bulunamadÄ±", color = if (isDark) Grey400 else Grey600)
                    }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList, key = { it.id }) { kiyaket ->
                        KiyafetCard(kiyaket, isDark, kiyaket.id in selectedIds, isMultiSelectMode,
                            onClick = {
                                if (isMultiSelectMode) viewModel.toggleSelection(kiyaket.id)
                                else onNavigateToKiyaketDetay(kiyaket.id)
                            },
                            onLongClick = { viewModel.enterMultiSelect(kiyaket.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KiyafetCard(kiyaket: Kiyaket, isDark: Boolean, isSelected: Boolean,
    isMultiSelectMode: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "card_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PrimaryLight.copy(alpha = 0.15f)
        else if (isDark) Grey800.copy(alpha = 0.5f) else White.copy(alpha = 0.9f),
        border = if (isSelected) BorderStroke(2.dp, PrimaryLight) else null,
        shadowElevation = if (isSelected) 0.dp else 2.dp) {
        Column(Modifier.padding(10.dp)) {
            Box(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(10.dp))
                .background(if (isDark) Grey700 else Grey200), contentAlignment = Alignment.Center) {
                if (kiyaket.imageUrl != null) {
                    AsyncImage(model = kiyaket.imageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
                } else {
                    Icon(Icons.Default.Checkroom, null, Modifier.size(40.dp), tint = if (isDark) Grey500 else Grey400)
                }
                if (isMultiSelectMode) {
                    Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).clip(CircleShape)
                        .background(if (isSelected) PrimaryLight else Grey600.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center) {
                        if (isSelected) Icon(Icons.Default.Check, null, Modifier.size(16.dp), tint = White)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(kiyaket.marka.ifBlank { "Marka belirtilmemiÅŸ" }, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold, color = if (isDark) Grey100 else Grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!kiyaket.renk.isNullOrBlank()) {
                    RenkDairesi(renk = kiyaket.renk!!, size = 10); Spacer(Modifier.width(5.dp))
                }
                Text(kiyaket.tur.displayName, style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey400 else Grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (kiyaket.beden.isNotBlank()) {
                    Text(" â€¢ ${kiyaket.beden}", style = MaterialTheme.typography.bodySmall, color = Grey500)
                }
            }
        }
    }
}

@Composable
private fun EmptyDolapState(onBarkodTara: () -> Unit, onManuelEkle: () -> Unit, isDark: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Surface(shape = CircleShape, color = PrimaryLight.copy(alpha = 0.1f), modifier = Modifier.size(100.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Checkroom, null, Modifier.size(48.dp), tint = PrimaryLight)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Ä°lk kÄ±yafetini ekle!", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(8.dp))
        Text("Barkod okutarak veya manuel olarak\ndolabÄ±nÄ± oluÅŸturmaya baÅŸla",
            style = MaterialTheme.typography.bodyMedium, color = if (isDark) Grey400 else Grey600, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Button(onClick = onBarkodTara, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.QrCodeScanner, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("Barkod ile Ekle", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onManuelEkle, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, PrimaryLight)) {
            Icon(Icons.Default.Edit, null, Modifier.size(20.dp), tint = PrimaryLight); Spacer(Modifier.width(8.dp))
            Text("Manuel Ekle", color = PrimaryLight, fontWeight = FontWeight.SemiBold)
        }
    }
}
