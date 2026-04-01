package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OcrEtiketScreen(
    onTextRecognized: (Map<String, String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            cameraPermissionState.status.isGranted -> {
                OcrCameraView(
                    onTextRecognized = onTextRecognized,
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                PermissionDenied(onNavigateBack = onNavigateBack)
            }
        }
    }
}

@Composable
fun OcrCameraView(
    onTextRecognized: (Map<String, String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showResult) {
            // Kamera görünümü
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView.setOnTouchListener { _, _ ->
                        true
                    }

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.DocumentScanner,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = PrimaryLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Etiket Fotoğrafı Çekin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Grey100 else Grey900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kıyafet etiketindeki marka, boyut ve materyal bilgilerini otomatik olarak tanıyacağız",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Grey400 else Grey600,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Geri butonu
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }

            // Çekim butonu
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                FloatingActionButton(
                    onClick = { /* Fotoğraf çek */ },
                    containerColor = PrimaryLight,
                    contentColor = White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Fotoğraf çek",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        } else {
            // Sonuç görünümü
            OcrResultView(
                recognizedText = recognizedText,
                isProcessing = isProcessing,
                onRetry = { showResult = false },
                onConfirm = { parsedData ->
                    onTextRecognized(parsedData)
                },
                isDark = isDark
            )
        }
    }
}

@Composable
fun OcrResultView(
    recognizedText: String?,
    isProcessing: Boolean,
    onRetry: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit,
    isDark: Boolean
) {
    val parsedData = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(recognizedText) {
        recognizedText?.let { text ->
            parsedData.value = parseEtiketText(text)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Grey900 else Grey100)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRetry) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = if (isDark) Grey100 else Grey800
                )
            }
            Text(
                text = "Tanıma Sonucu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Grey100 else Grey900
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryLight)
            }
        } else {
            // Tanınan metin
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tanınan Metin",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recognizedText ?: "Metin tanınamadı",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey400 else Grey600
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Çıkarılan bilgiler
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Çıkarılan Bilgiler",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900
                )

                Spacer(modifier = Modifier.height(12.dp))

                parsedData.value.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Grey500 else Grey600
                        )
                        Text(
                            text = value.ifBlank { "-" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Grey100 else Grey900
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Onay butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeniden Çek")
                }

                GlassButton(
                    onClick = { onConfirm(parsedData.value) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Onayla")
                }
            }
        }
    }
}

private fun parseEtiketText(text: String): Map<String, String> {
    val lowerText = text.lowercase()
    val result = mutableMapOf<String, String>()

    // Marka tespiti
    val markaList = listOf("koton", "lcw", "mavi", "defacto", "benetton", "puma", "adidas", "nike", "zara", "h&m", "mango", "bershka", "stradivarius", "pull&bear", "cors小黑瓶", "deichmann", "nine west")
    markaList.forEach { marka ->
        if (lowerText.contains(marka)) {
            result["Marka"] = marka.replaceFirstChar { it.uppercase() }
            return@forEach
        }
    }

    // Beden tespiti
    val bedenRegexes = listOf(
        Regex("""size[:\s]*([a-z0-9]+)""", RegexOption.IGNORE_CASE),
        Regex("""beden[:\s]*([a-z0-9]+)""", RegexOption.IGNORE_CASE),
        Regex("""\b(XS|S|M|L|XL|XXL|XXXL|34|36|38|40|42|44|46|48|50)\b""", RegexOption.IGNORE_CASE)
    )
    bedenRegexes.forEach { regex ->
        regex.find(text)?.groupValues?.getOrNull(1)?.let { beden ->
            result["Beden"] = beden.uppercase()
            return@forEach
        }
    }

    // Materyal tespiti
    val materyalList = listOf("%100 cotton", "%100 pamuk", "polyester", "viscose", "linen", "keten", "wool", "yün", "cashmere", "kaşmere", "modal", "akrilik")
    materyalList.forEach { materyal ->
        if (lowerText.contains(materyal)) {
            result["Materyal"] = materyal.replaceFirstChar { it.uppercase() }
            return@forEach
        }
    }

    // Renk tespiti
    val renkList = listOf("siyah", "beyaz", "gri", "kırmızı", "mavi", "yeşil", "sarı", "turuncu", "mor", "pembe", "bordo", "bej", "kahverengi", "lacivert")
    renkList.forEach { renk ->
        if (lowerText.contains(renk)) {
            result["Renk"] = renk.replaceFirstChar { it.uppercase() }
            return@forEach
        }
    }

    // Üretim yeri tespiti
    if (lowerText.contains("türkiye") || lowerText.contains("made in turkey")) {
        result["Üretim"] = "Türkiye"
    }

    return result
}
