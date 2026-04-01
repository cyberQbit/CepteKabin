package com.cyberqbit.ceptekabin.ui.screens.tarama

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.KiyaketTur
import com.cyberqbit.ceptekabin.domain.model.Mevsim
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiyaketEkleScreen(
    barkod: String,
    onNavigateBack: () -> Unit,
    onKiyaketSaved: () -> Unit,
    viewModel: KiyaketEkleViewModel = hiltViewModel()
) {
    val barkodSonuc by viewModel.barkodSonuc.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val kaynak by viewModel.kaynak.collectAsState()
    val urunKoduAramaYukleniyor by viewModel.urunKoduAramaYukleniyor.collectAsState()
    val urunKoduHata by viewModel.urunKoduHata.collectAsState()

    var urunKoduInput by remember { mutableStateOf("") }
    var marka by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var tur by remember { mutableStateOf<KiyaketTur?>(null) }
    var beden by remember { mutableStateOf("") }
    var renk by remember { mutableStateOf("") }
    var mevsim by remember { mutableStateOf(Mevsim.DORT_MEVSIM) }
    var sezon by remember { mutableStateOf("") }
    var not by remember { mutableStateOf("") }
    var showBedenSecimi by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }

    val bedenSecenekleri = listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL")

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    LaunchedEffect(barkod) {
        if (barkod.isNotBlank()) {
            viewModel.barkodAra(barkod)
        }
    }

    LaunchedEffect(barkodSonuc) {
        barkodSonuc?.let { sonuc ->
            marka = sonuc.marka ?: ""
            model = sonuc.model ?: ""
            tur = sonuc.tur?.let { KiyaketTur.fromString(it) }
            renk = sonuc.renk ?: ""
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onKiyaketSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kıyafet Ekle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = if (isDark) Grey100 else Grey800
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                    )
                )
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Ürün Kodu ile Ara
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ürün Kodu ile Ara",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Etiket üzerindeki referans kodunu girin (örn: W2GL42Z8-CVL)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey400 else Grey600
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = urunKoduInput,
                        onValueChange = { urunKoduInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Ürün Kodu") },
                        placeholder = { Text("örn: W2GL42Z8-CVL") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassButton(
                        onClick = { viewModel.urunKoduAra(urunKoduInput) },
                        enabled = urunKoduInput.isNotBlank() && !urunKoduAramaYukleniyor,
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (urunKoduAramaYukleniyor) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Ara")
                        }
                    }
                }
                urunKoduHata?.let { hata ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hata,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barkod bilgisi
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = PrimaryLight
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Barkod",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Grey500 else Grey600
                        )
                        Text(
                            text = barkod,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Grey100 else Grey900
                        )
                    }
                    if (kaynak.isNotEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    kaynak.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = PrimaryLight.copy(alpha = 0.1f),
                                labelColor = PrimaryLight
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Yükleniyor
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryLight)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hata - Ürün bulunamadıysa
            if (errorMessage != null && barkodSonuc == null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Info
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ürün bulunamadı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Grey100 else Grey900
                            )
                            Text(
                                text = "Bilgileri manuel olarak doldurun",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Grey400 else Grey600
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Otomatik doldurulan bilgiler
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ürün Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = marka,
                    onValueChange = { marka = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Marka *") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Model") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tür seçimi
                var turExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = turExpanded,
                    onExpandedChange = { turExpanded = it }
                ) {
                    OutlinedTextField(
                        value = tur?.displayName ?: "",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        label = { Text("Tür *") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = turExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = turExpanded,
                        onDismissRequest = { turExpanded = false }
                    ) {
                        KiyaketTur.entries.filter { it != KiyaketTur.DIGER }.forEach { turOption ->
                            DropdownMenuItem(
                                text = { Text(turOption.displayName) },
                                onClick = {
                                    tur = turOption
                                    turExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Beden seçimi
                OutlinedTextField(
                    value = beden,
                    onValueChange = { beden = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Beden *") },
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showBedenSecimi = !showBedenSecimi }) {
                            Icon(
                                if (showBedenSecimi) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )

                if (showBedenSecimi) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bedenSecenekleri.forEach { bedenOption ->
                            FilterChip(
                                selected = beden == bedenOption,
                                onClick = {
                                    beden = bedenOption
                                    showBedenSecimi = false
                                },
                                label = { Text(bedenOption) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = renk,
                    onValueChange = { renk = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Renk") },
                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mevsim seçimi
                var mevsimExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = mevsimExpanded,
                    onExpandedChange = { mevsimExpanded = it }
                ) {
                    OutlinedTextField(
                        value = mevsim.displayName,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        label = { Text("Mevsim") },
                        leadingIcon = { Icon(Icons.Default.WbSunny, contentDescription = null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mevsimExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = mevsimExpanded,
                        onDismissRequest = { mevsimExpanded = false }
                    ) {
                        Mevsim.entries.forEach { mevsimOption ->
                            DropdownMenuItem(
                                text = { Text(mevsimOption.displayName) },
                                onClick = {
                                    mevsim = mevsimOption
                                    mevsimExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sezon,
                    onValueChange = { sezon = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sezon (Opsiyonel)") },
                    placeholder = { Text("örn: 2024 Yaz") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = not,
                    onValueChange = { not = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Not (Opsiyonel)") },
                    leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = if (isDark) Grey700 else Grey400
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Kaydet butonu
            GlassButton(
                onClick = {
                    if (marka.isNotBlank() && tur != null && beden.isNotBlank()) {
                        viewModel.saveKiyaket(
                            kiyaket = Kiyaket(
                                barkod = barkod.takeIf { it.isNotBlank() },
                                marka = marka,
                                model = model.takeIf { it.isNotBlank() },
                                tur = tur!!,
                                beden = beden,
                                renk = renk.takeIf { it.isNotBlank() },
                                mevsim = mevsim,
                                sezon = sezon.takeIf { it.isNotBlank() },
                                not = not.takeIf { it.isNotBlank() }
                            ),
                            onSuccess = { saveSuccess = true },
                            onError = { /* Hata göster */ }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = marka.isNotBlank() && tur != null && beden.isNotBlank() && !isLoading
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kaydet")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // İptal butonu
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("İptal", color = if (isDark) Grey400 else Grey600)
            }
        }
    }
}
