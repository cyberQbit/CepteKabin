package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.KiyaketTur
import com.cyberqbit.ceptekabin.domain.model.Mevsim
import com.cyberqbit.ceptekabin.ui.theme.*
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
    val barkodSonuc by viewModel.barkodSonuc.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val urunKoduAramaYukleniyor by viewModel.urunKoduAramaYukleniyor.collectAsState()

    var urunKoduInput by remember { mutableStateOf("") }
    var marka by remember { mutableStateOf("") }
    var customMarkaField by remember { mutableStateOf("") }
    var markaExpanded by remember { mutableStateOf(false) }
    
    var model by remember { mutableStateOf("") }
    var modelExpanded by remember { mutableStateOf(false) }
    
    var tur by remember { mutableStateOf<KiyaketTur?>(null) }
    var turExpanded by remember { mutableStateOf(false) }

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
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val isDark = true
    val context = LocalContext.current
    var validationError by remember { mutableStateOf<String?>(null) }

    val markaList = com.cyberqbit.ceptekabin.util.Constants.MARKALAR
    val renkList = com.cyberqbit.ceptekabin.util.Constants.RENKLER
    val dinamikBedenListesi = com.cyberqbit.ceptekabin.util.Constants.getBedenListesi(model) ?: emptyList()

    LaunchedEffect(model) {
        if (!dinamikBedenListesi.contains(beden)) beden = ""
    }

    fun createSafeImageUri(): Uri {
        val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { selectedImageUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempCameraUri?.let { selectedImageUri = it }
    }

    LaunchedEffect(kiyaketId) {
        if (kiyaketId > 0L) {
            val existing = viewModel.getKiyaket(kiyaketId)
            existing?.let {
                marka = it.marka
                model = it.model ?: ""
                tur = it.tur
                beden = it.beden
                renk = it.renk ?: ""
                mevsim = it.mevsim
                sezon = it.sezon ?: ""
                not = it.not ?: ""
                it.imageUrl?.let { url -> selectedImageUri = Uri.parse(url) }
            }
        } else if (barkod.isNotBlank()) {
            viewModel.barkodAra(barkod)
        }
    }

    LaunchedEffect(barkodSonuc) {
        if (kiyaketId == 0L) {
            barkodSonuc?.let { sonuc ->
                marka = sonuc.marka ?: ""
                model = sonuc.model ?: ""
                tur = sonuc.tur?.let { KiyaketTur.fromString(it) }
                renk = sonuc.renk ?: ""
            }
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onKiyaketSaved()
    }

    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("Eksik Bilgi") },
            text = { Text(validationError ?: "") },
            confirmButton = { TextButton(onClick = { validationError = null }) { Text("Tamam") } }
        )
    }

    val containerColor = if (isDark) SurfaceDark else SurfaceLight
    val cardColor = if (isDark) SurfaceVariantDark else White
    val textColor = if (isDark) Grey100 else Grey900

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (kiyaketId > 0L) "Kıyafeti Düzenle" else "Kıyafet Ekle", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
            )
        },
        containerColor = containerColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // SİLİNEN BARKOD VE ÜRÜN KODU KISMI GERİ GELDİ
            if (kiyaketId == 0L) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ürün Kodu ile Ara", fontWeight = FontWeight.SemiBold, color = textColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = urunKoduInput, onValueChange = { urunKoduInput = it }, modifier = Modifier.weight(1f),
                                label = { Text("Ürün Kodu") }, placeholder = { Text("Örn: W2GL42Z8-CVL") }, singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.urunKoduAra(urunKoduInput) },
                                enabled = urunKoduInput.isNotBlank() && !urunKoduAramaYukleniyor,
                                modifier = Modifier.height(56.dp), shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                            ) { Text("Ara") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, null, tint = PrimaryLight, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Okutulan Barkod", style = MaterialTheme.typography.labelMedium, color = Grey500)
                            Text(barkod.takeIf { it.isNotBlank() } ?: "Barkod Okutulmadı", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Resim Seçici
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Kıyafet Görseli", fontWeight = FontWeight.SemiBold, color = textColor)
                    Spacer(Modifier.height(12.dp))
                    
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.TopEnd) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(selectedImageUri).setParameter("time", System.currentTimeMillis(), memoryCacheKey = null).build(),
                                contentDescription = "Seçilen Resim",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(onClick = { selectedImageUri = null }, modifier = Modifier.padding(8.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)) {
                                Icon(Icons.Default.Close, "Kaldır", tint = Color.White)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(if (isDark) Grey800 else Grey200), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Grey500, modifier = Modifier.size(48.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                val uri = createSafeImageUri()
                                tempCameraUri = uri
                                cameraLauncher.launch(uri) 
                            }, 
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                        ) { Text("Kamera") }
                        
                        OutlinedButton(
                            onClick = { pickImageLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, 
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("Galeri") }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Ürün Detay Formu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ürün Detayları", fontWeight = FontWeight.SemiBold, color = textColor)
                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = markaExpanded, onExpandedChange = { markaExpanded = it }) {
                        OutlinedTextField(
                            value = marka, onValueChange = { }, readOnly = true,
                            label = { Text("Marka *") }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = markaExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                        )
                        ExposedDropdownMenu(expanded = markaExpanded, onDismissRequest = { markaExpanded = false }) {
                            markaList.forEach { m -> DropdownMenuItem(text = { Text(m) }, onClick = { marka = m; customMarkaField = ""; markaExpanded = false }) }
                        }
                    }
                    if (marka == "Diğer") {
                        OutlinedTextField(value = customMarkaField, onValueChange = { customMarkaField = it }, label = { Text("Marka Adı") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
                    }

                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = modelExpanded, onExpandedChange = { modelExpanded = it }) {
                        OutlinedTextField(
                            value = model, onValueChange = { }, readOnly = true,
                            label = { Text("Kategori *") }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                        )
                        ExposedDropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                            com.cyberqbit.ceptekabin.util.Constants.ANA_KATEGORILER.forEach { k -> DropdownMenuItem(text = { Text(k) }, onClick = { model = k; modelExpanded = false; tur = null; beden = "" }) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = turExpanded, onExpandedChange = { turExpanded = it }) {
                        OutlinedTextField(
                            value = tur?.displayName ?: "", onValueChange = { }, readOnly = true,
                            label = { Text("Tür *") }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = turExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                        )
                        ExposedDropdownMenu(expanded = turExpanded, onDismissRequest = { turExpanded = false }) {
                            com.cyberqbit.ceptekabin.util.Constants.getGecerliTurler(model).forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { tur = KiyaketTur.fromString(t); turExpanded = false }) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (dinamikBedenListesi.isNotEmpty()) {
                        ExposedDropdownMenuBox(expanded = bedenExpanded, onExpandedChange = { bedenExpanded = it }) {
                            OutlinedTextField(
                                value = beden, onValueChange = { }, readOnly = true,
                                label = { Text("Beden *") }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bedenExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                            )
                            ExposedDropdownMenu(expanded = bedenExpanded, onDismissRequest = { bedenExpanded = false }) {
                                dinamikBedenListesi.forEach { b -> DropdownMenuItem(text = { Text(b) }, onClick = { beden = b; bedenExpanded = false }) }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    ExposedDropdownMenuBox(expanded = renkExpanded, onExpandedChange = { renkExpanded = it }) {
                        OutlinedTextField(
                            value = renk, onValueChange = { }, readOnly = true,
                            label = { Text("Renk") }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = renkExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                        )
                        ExposedDropdownMenu(expanded = renkExpanded, onDismissRequest = { renkExpanded = false }) {
                            renkList.forEach { r -> DropdownMenuItem(text = { Text(r) }, onClick = { renk = r; customRenkField = ""; renkExpanded = false }) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = not, onValueChange = { not = it },
                        label = { Text("Not (İsteğe Bağlı)") }, modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val finalMarka = if (marka == "Diğer") customMarkaField else marka
                    val finalRenk = if (renk == "Diğer") customRenkField else renk
                    
                    if (finalMarka.isNotBlank() && tur != null && model.isNotBlank()) {
                        viewModel.saveKiyaket(
                            kiyaket = Kiyaket(
                                id = kiyaketId, barkod = barkod.takeIf { it.isNotBlank() },
                                marka = finalMarka, model = model, tur = tur!!, beden = beden, renk = finalRenk,
                                mevsim = mevsim, sezon = sezon, not = not, imageUrl = selectedImageUri?.toString()
                            ),
                            onSuccess = { saveSuccess = true },
                            onError = { validationError = it }
                        )
                    } else {
                        validationError = "Lütfen yıldızlı (*) alanları eksiksiz doldurun."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
            ) {
                Text(if (kiyaketId > 0L) "Güncelle" else "Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(Modifier.height(80.dp)) // Alt bar için kaydırma payı
        }
    }
}
