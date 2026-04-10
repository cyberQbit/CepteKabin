package com.cyberqbit.ceptekabin.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.data.remote.api.BackgroundRemoverService
import com.cyberqbit.ceptekabin.ui.theme.*
import com.cyberqbit.ceptekabin.util.Constants
import com.cyberqbit.ceptekabin.util.LocalImageStorageHelper
import kotlinx.coroutines.launch

/**
 * FotoÄŸraf seÃ§ildikten sonra KiyaketEkleScreen'de gÃ¶sterilen
 * "Arka PlanÄ± KaldÄ±r" aksiyon bÃ¶lÃ¼mÃ¼.
 *
 * [imageUri]       â€” seÃ§ili gÃ¶rsel URI
 * [bgRemover]      â€” inject edilmiÅŸ servis
 * [onResultUri]    â€” iÅŸlem sonucu yeni URI
 */
@Composable
fun BgRemoverSection(
    imageUri: Uri,
    bgRemover: BackgroundRemoverService,
    onResultUri: (Uri) -> Unit
) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()
    val isDark   = true

    var isProcessing  by remember { mutableStateOf(false) }
    var showApiDialog by remember { mutableStateOf(false) }
    var errorMsg      by remember { mutableStateOf<String?>(null) }

    val prefs   = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    var apiKey  by remember { mutableStateOf(prefs.getString("removebg_api_key", null) ?: "") }

    if (showApiDialog) {
        ApiKeyDialog(
            current   = apiKey,
            onConfirm = { key ->
                apiKey = key
                prefs.edit().putString("removebg_api_key", key).apply()
                showApiDialog = false
                // Anahtar alÄ±ndÄ±ktan hemen sonra iÅŸlemi baÅŸlat
                scope.launch {
                    runRemoval(context, imageUri, key, bgRemover, onResultUri,
                        onStart    = { isProcessing = true; errorMsg = null },
                        onFinish   = { isProcessing = false },
                        onError    = { errorMsg = it; isProcessing = false }
                    )
                }
            },
            onDismiss = { showApiDialog = false }
        )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Arka Plan Silici",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDark) Grey100 else Grey900)
                Text("remove.bg Ã¼cretsiz tier (50/ay)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Grey400 else Grey600)
            }

            if (isProcessing) {
                CircularProgressIndicator(Modifier.size(24.dp), PrimaryLight, strokeWidth = 2.dp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // API anahtarÄ± ayarla
                    IconButton(onClick = { showApiDialog = true }) {
                        Icon(Icons.Default.Key, "API AnahtarÄ±",
                            tint = if (isDark) Grey400 else Grey600,
                            modifier = Modifier.size(20.dp))
                    }
                    // Arka planÄ± kaldÄ±r
                    GlassButton(
                        onClick = {
                            if (apiKey.isBlank()) {
                                showApiDialog = true
                            } else {
                                scope.launch {
                                    runRemoval(context, imageUri, apiKey, bgRemover, onResultUri,
                                        onStart  = { isProcessing = true; errorMsg = null },
                                        onFinish = { isProcessing = false },
                                        onError  = { errorMsg = it; isProcessing = false }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Uygula", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        AnimatedVisibility(errorMsg != null) {
            Spacer(Modifier.height(6.dp))
            Text(errorMsg ?: "", style = MaterialTheme.typography.bodySmall, color = Error)
        }
    }
}

private suspend fun runRemoval(
    context: Context,
    sourceUri: Uri,
    apiKey: String,
    bgRemover: BackgroundRemoverService,
    onResultUri: (Uri) -> Unit,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onError: (String) -> Unit
) {
    onStart()
    try {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val sourceBitmap = BitmapFactory.decodeStream(inputStream)
        if (sourceBitmap == null) { onError("GÃ¶rsel okunamadÄ±."); return }

        val result = bgRemover.removeBackground(sourceBitmap, apiKey)
        if (result == null) { onError("API yanÄ±t vermedi. AnahtarÄ±nÄ±zÄ± kontrol edin."); return }

        val savedUriStr = LocalImageStorageHelper.saveBitmapToGallery(
            context, result, "bgremoved_${System.currentTimeMillis()}"
        )
        if (savedUriStr == null) { onError("SonuÃ§ kaydedilemedi."); return }

        onResultUri(android.net.Uri.parse(savedUriStr))
    } catch (e: Exception) {
        onError(e.message ?: "Beklenmeyen hata")
    } finally {
        onFinish()
    }
}

@Composable
private fun ApiKeyDialog(current: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var value by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("remove.bg API AnahtarÄ±") },
        text  = {
            Column {
                Text("remove.bg sitesinden Ã¼cretsiz anahtar alabilirsiniz (50 iÅŸlem/ay).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = value, onValueChange = { value = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) },
                enabled = value.isNotBlank()) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ä°ptal") } }
    )
}
