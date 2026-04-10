package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.Manifest
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cyberqbit.ceptekabin.data.service.DppUrlService
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Taranan değer:
 *  - Normal barkod/QR  → onBarkodFound(value)
 *  - DPP URL           → onDppUrlFound(url) ile KiyaketEkleScreen'e yönlendirilir,
 *                        orada DppUrlService çağrılarak form otomatik doldurulur.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TaramaScreen(
    onBarkodFound: (String) -> Unit,
    onDppUrlFound: (String) -> Unit = { url -> onBarkodFound("DPP:$url") },
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) cameraPermissionState.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreviewWithBarcode(
                onBarkodFound  = onBarkodFound,
                onDppUrlFound  = onDppUrlFound,
                onNavigateBack = onNavigateBack
            )
        } else {
            PermissionDenied(onNavigateBack = onNavigateBack)
        }
    }
}

@Composable
fun CameraPreviewWithBarcode(
    onBarkodFound: (String) -> Unit,
    onDppUrlFound: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scannedValue   by remember { mutableStateOf<String?>(null) }
    var isProcessing   by remember { mutableStateOf(false) }
    val isDark         = true

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis  = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        if (!isProcessing && scannedValue == null) {
                            isProcessing = true
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage, imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val raw = barcode.rawValue ?: continue
                                            if (scannedValue == null) {
                                                scannedValue = raw
                                                // #15: DPP URL mi barkod mu?
                                                if (DppUrlService.isDppUrl(raw)) {
                                                    onDppUrlFound(raw)
                                                } else {
                                                    onBarkodFound(raw)
                                                }
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                        isProcessing = false
                                    }
                            } else {
                                imageProxy.close()
                                isProcessing = false
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA,
                            preview, imageAnalysis
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            GlassSurface(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCodeScanner, null,
                        Modifier.size(48.dp), tint = PrimaryLight)
                    Spacer(Modifier.height(16.dp))
                    Text("Barkod veya QR kod tarayın",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = if (isDark) Grey100 else Grey900)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Kıyafet etiketi barkodunu veya ürün QR kodunu\nkameraya tutun — bilgiler otomatik doldurulur",
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
            Icon(Icons.Default.ArrowBack, "Geri", tint = Color.White)
        }
    }
}

@Composable
fun PermissionDenied(onNavigateBack: () -> Unit) {
    val isDark = true
    Column(
        Modifier.fillMaxSize()
            .background(if (isDark) Grey900 else Grey100)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CameraAlt, null, Modifier.size(64.dp), tint = Error)
        Spacer(Modifier.height(24.dp))
        Text("Kamera İzni Reddedildi",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = if (isDark) Grey100 else Grey900)
        Spacer(Modifier.height(12.dp))
        Text("Ayarlardan kamera iznini etkinleştirebilirsiniz.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (isDark) Grey400 else Grey600)
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onNavigateBack) { Text("Geri Dön", color = PrimaryLight) }
    }
}
