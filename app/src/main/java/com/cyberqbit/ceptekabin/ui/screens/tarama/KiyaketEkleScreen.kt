package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
    kiyaketId: Long = 0L,
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
    var customMarkaField by remember { mutableStateOf("") }
    var markaExpanded by remember { mutableStateOf(false) }
    
    var model by remember { mutableStateOf("") }
    var modelExpanded by remember { mutableStateOf(false) }
    
    var tur by remember { mutableStateOf<KiyaketTur?>(null) }
    var beden by remember { mutableStateOf("") }
    var bedenExpanded by remember { mutableStateOf(false) }
    
    var renk by remember { mutableStateOf("") }
    var customRenkField by remember { mutableStateOf("") }
    var renkExpanded by remember { mutableStateOf(false) }
    
    var mevsim by remember { mutableStateOf(Mevsim.DORT_MEVSIM) }
    var mevsimExpanded by remember { mutableStateOf(false) }
    
    var sezon by remember { mutableStateOf("") }
    var not by remember { mutableStateOf("") }
    var saveSuccess by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // Validation error state
    var validationError by remember { mutableStateOf<String?>(null) }

    // Marka listesi - International + Top Turkish Brands
    val markaList = com.cyberqbit.ceptekabin.util.Constants.MARKALAR

    // Renk listesi - 20+ renkler
    val renkList = listOf(
        "Siyah-BLK", "Beyaz-WHI", "Kırmızı-RED", "Mavi-BLU", "Yeşil-GRN",
        "Sarı-YEL", "Turuncu-ORN", "Mor-PUR", "Pembe-PNK", "Kahverengi-BRN",
        "Gri-GRY", "Lacivert-NVY", "Turkuaz-TRQ", "Zeytin-OLV", "Koyu Yeşil-DRG",
        "Ekru-CRM", "Beige-BGE", "Bej-BGE", "Maroon-MAR", "Tüm Renkler-MLT",
        "Diğer"
    )

    // Beden listesi - eksiksiz
    val dinamikBedenListesi = when (tur?.displayName) {
    "Pantolon", "Şort", "Etek", "Eşofman" -> com.cyberqbit.ceptekabin.util.Constants.PANTOLON_BEDENLERI
    "Kadın Ayakkabısı" -> com.cyberqbit.ceptekabin.util.Constants.KADIN_AYAKKABI_NUMARALARI
    "Erkek Ayakkabısı" -> com.cyberqbit.ceptekabin.util.Constants.ERKEK_AYAKKABI_NUMARALARI
    "Çocuk Ayakkabısı" -> com.cyberqbit.ceptekabin.util.Constants.COCUK_AYAKKABI_NUMARALARI
    else -> com.cyberqbit.ceptekabin.util.Constants.GENEL_BEDENLER
}
androidx.compose.runtime.LaunchedEffect(tur) {
    if (tur != null && !dinamikBedenListesi.contains(beden)) {
        beden = ""
    }
}

    // Model/Kategori listesi
    val kategoriList = com.cyberqbit.ceptekabin.util.Constants.KIYAFET_TURLERI

    // Image Picker Launchers
        val context = androidx.compose.ui.platform.LocalContext.current
    var tempCameraUri by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }

    fun createImageUri(): android.net.Uri {
        val file = java.io.File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedImageUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { selectedImageUri = it }
        }
    }

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

    // Validation Error Dialog
    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("Eksik Bilgi") },
            text = { Text(validationError ?: "") },
            confirmButton = {
                TextButton(onClick = { validationError = null }) {
                    Text("Tamam")
                }
            }
        )
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

            // Resim Yükleme
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kıyafet Resmi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Grey700),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Seçilen Resim",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Resmi Kaldır",
                                tint = White
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDark) Grey800.copy(alpha = 0.5f) else Grey300.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = if (isDark) Grey500 else Grey600,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = {
                            val uri = createImageUri()
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fotoğraf", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }

                    GlassButton(
                        onClick = {
                            pickImageLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galeriden", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Otomatik doldurulan bilgiler
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ürün Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Marka Dropdown
                ExposedDropdownMenuBox(
                    expanded = markaExpanded,
                    onExpandedChange = { markaExpanded = it }
                ) {
                    OutlinedTextField(
                        value = marka,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        label = { Text("Marka *") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = markaExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = markaExpanded,
                        onDismissRequest = { markaExpanded = false }
                    ) {
                        markaList.forEach { markaOption ->
                            DropdownMenuItem(
                                text = { Text(markaOption) },
                                onClick = {
                                    marka = markaOption
                                    customMarkaField = ""
                                    markaExpanded = false
                                }
                            )
                        }
                    }
                }

                // Custom Marka Field
                if (marka == "Diğer") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customMarkaField,
                        onValueChange = { customMarkaField = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Marka Adı") },
                        placeholder = { Text("Markayı yazın") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Model/Kategori Dropdown
                ExposedDropdownMenuBox(
                    expanded = modelExpanded,
                    onExpandedChange = { modelExpanded = it }
                ) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        label = { Text("Model / Kategori *") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        kategoriList.forEach { kategoriOption ->
                            DropdownMenuItem(
                                text = { Text(kategoriOption) },
                                onClick = {
                                    model = kategoriOption
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }

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
                        val gecerliTurler = com.cyberqbit.ceptekabin.util.Constants.getGecerliTurler(model)
                        gecerliTurler.forEach { turAdi ->
                            DropdownMenuItem(
                                text = { Text(turAdi) },
                                onClick = {
                                    tur = KiyaketTur.fromString(turAdi)
                                    turExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Beden Dropdown - Readonly
                ExposedDropdownMenuBox(
                    expanded = bedenExpanded,
                    onExpandedChange = { bedenExpanded = it }
                ) {
                    OutlinedTextField(
                        value = beden,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        label = { Text("Beden *") },
                        leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bedenExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = bedenExpanded,
                        onDismissRequest = { bedenExpanded = false }
                    ) {
                        dinamikBedenListesi.forEach { bedenOption ->
                            DropdownMenuItem(
                                text = { Text(bedenOption) },
                                onClick = {
                                    beden = bedenOption
                                    bedenExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Renk Dropdown
                ExposedDropdownMenuBox(
                    expanded = renkExpanded,
                    onExpandedChange = { renkExpanded = it }
                ) {
                    OutlinedTextField(
                        value = renk,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        label = { Text("Renk") },
                        leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = renkExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = renkExpanded,
                        onDismissRequest = { renkExpanded = false }
                    ) {
                        renkList.forEach { renkOption ->
                            DropdownMenuItem(
                                text = { Text(renkOption) },
                                onClick = {
                                    renk = renkOption
                                    customRenkField = ""
                                    renkExpanded = false
                                }
                            )
                        }
                    }
                }

                // Custom Renk Field
                if (renk == "Diğer") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customRenkField,
                        onValueChange = { customRenkField = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Renk Adı") },
                        placeholder = { Text("Rengi yazın") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey400
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mevsim seçimi
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
                    val finalMarka = if (marka == "Diğer") customMarkaField else marka
                    val finalRenk = if (renk == "Diğer") customRenkField else renk
                    
                    // Şartlar sağlanıyorsa kaydet
                    if (finalMarka.isNotBlank() && tur != null && beden.isNotBlank() && model.isNotBlank()) {
                        viewModel.saveKiyaket(
                            kiyaket = Kiyaket(
                                barkod = barkod.takeIf { it.isNotBlank() },
                                marka = finalMarka,
                                model = model.takeIf { it.isNotBlank() },
                                tur = tur!!,
                                beden = beden,
                                renk = finalRenk.takeIf { it.isNotBlank() },
                                mevsim = mevsim,
                                sezon = sezon.takeIf { it.isNotBlank() },
                                not = not.takeIf { it.isNotBlank() },
                                imageUrl = selectedImageUri?.toString()
                            ),
                            onSuccess = { saveSuccess = true },
                            onError = { hataMesaji -> 
                                // HATA GÖSTERİMİ DÜZELTİLDİ:
                                // Sayfanın üst kısmında tanımladığın validationError değişkenine hatayı atıyoruz,
                                // böylece ekranda AlertDialog (Uyarı penceresi) çıkacak.
                                validationError = hataMesaji 
                            }
                        )
                    } else {
                        // KULLANICI EKSİK GİRİŞ YAPTIYSA UYAR:
                        validationError = "Lütfen Marka, Tür, Model ve Beden alanlarını eksiksiz doldurun."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // Sadece yükleniyorsa butonu pasif yap
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





@Composable
fun EminMisinizDialog(
    onayla: () -> Unit,
    iptalEt: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = iptalEt,
        title = { androidx.compose.material3.Text("Onay Gerekli") },
        text = { androidx.compose.material3.Text("Bu ürünü kaydetmek/düzenlemek istediğinize emin misiniz?") },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onayla) { androidx.compose.material3.Text("Evet, Eminim") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = iptalEt) { androidx.compose.material3.Text("İptal") }
        }
    )
}


