package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.domain.model.*
import com.cyberqbit.ceptekabin.ui.components.*
import com.cyberqbit.ceptekabin.ui.theme.*
import com.cyberqbit.ceptekabin.util.Constants
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiyaketEkleScreen(
    barkod: String,
    kiyaketId: Long = 0L,
    onNavigateBack: () -> Unit,
    onKiyaketSaved: () -> Unit,
    viewModel: KiyaketEkleViewModel = hiltViewModel()
) {
    val barkodSonuc              by viewModel.barkodSonuc.collectAsState()
    val isLoading                by viewModel.isLoading.collectAsState()
    val errorMessage             by viewModel.errorMessage.collectAsState()
    val kaynak                   by viewModel.kaynak.collectAsState()
    val urunKoduAramaYukleniyor  by viewModel.urunKoduAramaYukleniyor.collectAsState()
    val urunKoduHata             by viewModel.urunKoduHata.collectAsState()

    // ── Form state ────────────────────────────────────────────────────────────
    var urunKoduInput    by remember { mutableStateOf("") }
    var marka            by remember { mutableStateOf("") }
    var customMarkaField by remember { mutableStateOf("") }
    var markaExpanded    by remember { mutableStateOf(false) }

    // #14: Kategori önce, Tür sonra
    var kategori         by remember { mutableStateOf("") }
    var kategoriExpanded by remember { mutableStateOf(false) }

    var tur              by remember { mutableStateOf<KiyaketTur?>(null) }
    var turExpanded      by remember { mutableStateOf(false) }

    // #13: beden sadece Aksesuar dışı kategorilerde gösterilir
    var beden            by remember { mutableStateOf("") }
    var bedenExpanded    by remember { mutableStateOf(false) }

    var renk             by remember { mutableStateOf("") }
    var customRenkField  by remember { mutableStateOf("") }
    var renkExpanded     by remember { mutableStateOf(false) }

    var mevsim           by remember { mutableStateOf(Mevsim.DORT_MEVSIM) }
    var mevsimExpanded   by remember { mutableStateOf(false) }

    var sezon            by remember { mutableStateOf("") }
    var not              by remember { mutableStateOf("") }
    var fiyat            by remember { mutableStateOf("") }
    var saveSuccess      by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var validationError  by remember { mutableStateOf<String?>(null) }

    val isDark = isSystemInDarkTheme()
    val context = androidx.compose.ui.platform.LocalContext.current

    // #13: Aksesuar kategorisinde beden alanı gizlenir
    val bedenGizle = Constants.BEDEN_GEREKTIRMEYEN_KATEGORILER.contains(kategori)

    // Kategoriye göre Tür listesi
    val turListesi = remember(kategori) { Constants.getGecerliTurler(kategori) }

    // Kategoriye göre Beden listesi (null = gizle)
    val bedenListesi = remember(kategori) { Constants.getBedenListesi(kategori) }

    // Kategori değişince tur/beden sıfırla
    LaunchedEffect(kategori) {
        tur   = null
        beden = ""
    }

    // ── Geçici kamera dosyası — #10 FIX ──────────────────────────────────────
    // Statik isim kullanarak her seferinde üzerine yazıyoruz; asla birikmiyor.
    val tempCameraFile = remember { File(context.cacheDir, "temp_camera.jpg") }
    val tempCameraUri = remember {
        androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", tempCameraFile
        )
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { selectedImageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempCameraUri
        }
        // #10 FIX: success==false ise dosyayı sil
        else if (tempCameraFile.exists()) {
            tempCameraFile.delete()
        }
    }

    LaunchedEffect(barkod) { if (barkod.isNotBlank()) viewModel.barkodAra(barkod) }

    LaunchedEffect(barkodSonuc) {
        barkodSonuc?.let { s ->
            marka = s.marka ?: ""
            tur   = s.tur?.let { KiyaketTur.fromString(it) }
            renk  = s.renk ?: ""
        }
    }

    LaunchedEffect(saveSuccess) { if (saveSuccess) onKiyaketSaved() }

    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("Eksik Bilgi") },
            text  = { Text(validationError ?: "") },
            confirmButton = {
                TextButton(onClick = { validationError = null }) { Text("Tamam") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (kiyaketId > 0L) "Kıyafeti Düzenle" else "Kıyafet Ekle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri",
                            tint = if (isDark) Grey100 else Grey800)
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
                .background(Brush.verticalGradient(
                    if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                ))
                .padding(paddingValues)
                .padding(16.dp)
                // #9 FIX: klavye IME padding
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Ürün Kodu ile Ara ─────────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Ürün Kodu ile Ara",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900)
                Spacer(Modifier.height(4.dp))
                Text("Etiket üzerindeki referans kodunu girin (örn: W2GL42Z8-CVL)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey400 else Grey600)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = urunKoduInput, onValueChange = { urunKoduInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Ürün Kodu") },
                        placeholder = { Text("örn: W2GL42Z8-CVL") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        colors = outlinedColors(isDark)
                    )
                    Spacer(Modifier.width(8.dp))
                    GlassButton(
                        onClick = { viewModel.urunKoduAra(urunKoduInput) },
                        enabled = urunKoduInput.isNotBlank() && !urunKoduAramaYukleniyor,
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (urunKoduAramaYukleniyor) CircularProgressIndicator(
                            Modifier.size(18.dp), White, strokeWidth = 2.dp)
                        else Text("Ara")
                    }
                }
                urunKoduHata?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Error)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Barkod bilgisi ────────────────────────────────────────────────
            if (barkod.isNotBlank()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, null, tint = PrimaryLight)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Barkod", style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Grey500 else Grey600)
                            Text(barkod, style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Grey100 else Grey900)
                        }
                        if (kaynak.isNotEmpty()) {
                            AssistChip(onClick = { },
                                label = { Text(kaynak.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = PrimaryLight.copy(alpha = 0.1f),
                                    labelColor = PrimaryLight))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryLight)
                }
                Spacer(Modifier.height(16.dp))
            }

            if (errorMessage != null && barkodSonuc == null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Info)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Ürün bulunamadı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Grey100 else Grey900)
                            Text("Bilgileri manuel olarak doldurun",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Grey400 else Grey600)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Fotoğraf ──────────────────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Kıyafet Resmi",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900)
                Spacer(Modifier.height(12.dp))
                if (selectedImageUri != null) {
                    Box(Modifier.fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Grey700),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        AsyncImage(
                            model = selectedImageUri, contentDescription = "Seçilen Resim",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit  // #11 FIX: Fit değil Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Close, "Kaldır", tint = White)
                        }
                    }
                } else {
                    Box(Modifier.fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Grey800.copy(0.5f) else Grey300.copy(0.3f)),
                        Alignment.Center) {
                        Icon(Icons.Default.Image, null,
                            tint = if (isDark) Grey500 else Grey600,
                            modifier = Modifier.size(48.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    GlassButton(
                        onClick = { cameraLauncher.launch(tempCameraUri) },
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Fotoğraf", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                    GlassButton(
                        onClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Galeriden", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Ürün Bilgileri ────────────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Ürün Bilgileri",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900)
                Spacer(Modifier.height(16.dp))

                // Marka
                DropdownField("Marka *", Icons.Default.Business, marka, markaExpanded,
                    { markaExpanded = it }, isDark) {
                    Constants.MARKALAR.forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { marka = m; customMarkaField = ""; markaExpanded = false })
                    }
                }
                if (marka == "Diğer") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = customMarkaField, onValueChange = { customMarkaField = it },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Marka Adı") },
                        placeholder = { Text("Markayı yazın") }, singleLine = true,
                        colors = outlinedColors(isDark))
                }

                Spacer(Modifier.height(12.dp))

                // #14 FIX: Kategori → Tür sıralaması
                DropdownField("Kategori *", Icons.Default.GridView, kategori, kategoriExpanded,
                    { kategoriExpanded = it }, isDark) {
                    Constants.ANA_KATEGORILER.forEach { k ->
                        DropdownMenuItem(text = { Text(k) }, onClick = { kategori = k; kategoriExpanded = false })
                    }
                }

                Spacer(Modifier.height(12.dp))

                DropdownField("Tür *", Icons.Default.Category,
                    tur?.displayName ?: "", turExpanded, { turExpanded = it }, isDark,
                    enabled = kategori.isNotBlank()) {
                    turListesi.forEach { t ->
                        DropdownMenuItem(text = { Text(t) }, onClick = {
                            tur = KiyaketTur.fromString(t); turExpanded = false
                        })
                    }
                }

                // #13 FIX: Aksesuar kategorisinde beden gizlenir
                if (!bedenGizle && bedenListesi != null) {
                    Spacer(Modifier.height(12.dp))
                    DropdownField("Beden *", Icons.Default.Straighten,
                        beden, bedenExpanded, { bedenExpanded = it }, isDark) {
                        bedenListesi.forEach { b ->
                            DropdownMenuItem(text = { Text(b) }, onClick = { beden = b; bedenExpanded = false })
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Renk
                DropdownField("Renk", Icons.Default.Palette, renk, renkExpanded,
                    { renkExpanded = it }, isDark) {
                    Constants.RENKLER.forEach { r ->
                        DropdownMenuItem(text = { Text(r) }, onClick = { renk = r; customRenkField = ""; renkExpanded = false })
                    }
                }
                if (renk == "Diğer") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = customRenkField, onValueChange = { customRenkField = it },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Renk Adı") },
                        singleLine = true, colors = outlinedColors(isDark))
                }

                Spacer(Modifier.height(12.dp))

                // Mevsim
                DropdownField("Mevsim", Icons.Default.WbSunny,
                    mevsim.displayName, mevsimExpanded, { mevsimExpanded = it }, isDark) {
                    Mevsim.entries.forEach { m ->
                        DropdownMenuItem(text = { Text(m.displayName) }, onClick = { mevsim = m; mevsimExpanded = false })
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(value = sezon, onValueChange = { sezon = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sezon (Opsiyonel)") }, placeholder = { Text("örn: 2025 Yaz") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) }, singleLine = true,
                    colors = outlinedColors(isDark))

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(value = fiyat, onValueChange = { fiyat = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Satın Alma Fiyatı (₺ — Opsiyonel)") },
                    leadingIcon = { Icon(Icons.Default.Payments, null) }, singleLine = true,
                    colors = outlinedColors(isDark))

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(value = not, onValueChange = { not = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Not (Opsiyonel)") },
                    leadingIcon = { Icon(Icons.Default.Comment, null) }, maxLines = 3,
                    colors = outlinedColors(isDark))
            }

            Spacer(Modifier.height(24.dp))

            GlassButton(
                onClick = {
                    val finalMarka = if (marka == "Diğer") customMarkaField else marka
                    val finalRenk  = if (renk  == "Diğer") customRenkField  else renk
                    val finalBeden = if (bedenGizle) "Standart" else beden
                    val fiyatDouble = fiyat.replace(",", ".").toDoubleOrNull()

                    if (finalMarka.isBlank() || tur == null || kategori.isBlank() ||
                        (!bedenGizle && finalBeden.isBlank())) {
                        validationError = "Lütfen Marka, Kategori ve Tür alanlarını eksiksiz doldurun."
                        return@GlassButton
                    }

                    viewModel.saveKiyaket(
                        kiyaket = Kiyaket(
                            barkod           = barkod.takeIf { it.isNotBlank() },
                            marka            = finalMarka,
                            model            = tur?.displayName,
                            tur              = tur!!,
                            beden            = finalBeden,
                            renk             = finalRenk.takeIf { it.isNotBlank() },
                            mevsim           = mevsim,
                            sezon            = sezon.takeIf { it.isNotBlank() },
                            not              = not.takeIf { it.isNotBlank() },
                            imageUrl         = selectedImageUri?.toString(),
                            satinAlmaFiyati  = fiyatDouble
                        ),
                        onSuccess = { saveSuccess = true },
                        onError   = { validationError = it }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = !isLoading
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Kaydet")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("İptal", color = if (isDark) Grey400 else Grey600)
            }
        }
    }
}

// ── Yardımcı bileşenler ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isDark: Boolean,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) onExpandedChange(it) }) {
        OutlinedTextField(
            value = value, onValueChange = { },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
            label = { Text(label) },
            leadingIcon = { Icon(icon, null) },
            readOnly = true, enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            colors = outlinedColors(isDark)
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { onExpandedChange(false) },
            content = content)
    }
}

@Composable
private fun outlinedColors(isDark: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = PrimaryLight,
    unfocusedBorderColor = if (isDark) Grey700 else Grey400
)
