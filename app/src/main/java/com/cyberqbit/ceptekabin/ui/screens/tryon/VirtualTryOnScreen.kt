package com.cyberqbit.ceptekabin.ui.screens.tryon

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualTryOnScreen(
    kombinId: Long,
    onNavigateBack: () -> Unit,
    viewModel: VirtualTryOnViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsState()
    val isDark    = true

    LaunchedEffect(kombinId) { viewModel.loadKombin(kombinId) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setUserPhoto(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ãœzerimde GÃ¶r") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                ))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AÃ§Ä±klama kartÄ±
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(28.dp), tint = AccentGold)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Sanal Kabin (Beta)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Grey100 else Grey900)
                        Text(
                            "Kendi fotoÄŸrafÄ±na kombini yapay zeka ile bindiriyoruz. " +
                            "FotoÄŸrafÄ±n yalnÄ±zca iÅŸlem iÃ§in kullanÄ±lÄ±r ve hemen silinir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Grey400 else Grey600
                        )
                    }
                }
            }

            // Kombin bilgisi
            uiState.kombinAd?.let { ad ->
                Text("Kombin: $ad",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Grey100 else Grey900)
            }

            // KullanÄ±cÄ± fotoÄŸrafÄ± seÃ§imi
            if (uiState.userPhotoUri == null) {
                GlassCard(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonAdd, null, Modifier.size(48.dp),
                            tint = if (isDark) Grey500 else Grey400)
                        Spacer(Modifier.height(12.dp))
                        Text("FotoÄŸrafÄ±nÄ± yÃ¼kle",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Grey100 else Grey900)
                        Spacer(Modifier.height(4.dp))
                        Text("Tam boy, ayakta duran bir fotoÄŸraf seÃ§",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Grey400 else Grey600,
                            textAlign = TextAlign.Center)
                    }
                }
                GlassButton(
                    onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galeriden FotoÄŸraf SeÃ§")
                }
            } else {
                // Yan yana: kullanÄ±cÄ± fotoÄŸrafÄ± + sonuÃ§
                Row(Modifier.fillMaxWidth().height(360.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Sol: kullanÄ±cÄ± fotoÄŸrafÄ±
                    Column(Modifier.weight(1f)) {
                        Text("Sen", style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Grey500 else Grey600)
                        Spacer(Modifier.height(4.dp))
                        AsyncImage(
                            model = uiState.userPhotoUri, contentDescription = "SeÃ§ilen fotoÄŸraf",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    // SaÄŸ: sonuÃ§
                    Column(Modifier.weight(1f)) {
                        Text("Kombinle", style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Grey500 else Grey600)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Grey800 else Grey200),
                            Alignment.Center
                        ) {
                            when {
                                uiState.isProcessing -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = PrimaryLight)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Yapay zeka Ã§alÄ±ÅŸÄ±yor...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Grey400 else Grey600)
                                    }
                                }
                                uiState.resultUri != null -> {
                                    AsyncImage(
                                        model = uiState.resultUri,
                                        contentDescription = "Try-On sonucu",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                else -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AutoAwesome, null,
                                            Modifier.size(40.dp),
                                            tint = if (isDark) Grey600 else Grey400)
                                        Spacer(Modifier.height(8.dp))
                                        Text("SonuÃ§ burada gÃ¶rÃ¼necek",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = if (isDark) Grey500 else Grey600)
                                    }
                                }
                            }
                        }
                    }
                }

                // Hata
                uiState.error?.let { err ->
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = Error.copy(alpha = 0.12f)) {
                        Text(err, Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall, color = Error)
                    }
                }

                // Aksiyon butonlarÄ±
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.clearPhoto() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("FotoÄŸrafÄ± DeÄŸiÅŸtir")
                    }
                    GlassButton(
                        onClick = { viewModel.process() },
                        modifier = Modifier.weight(1f),
                        enabled  = !uiState.isProcessing && uiState.resultUri == null
                    ) {
                        Icon(Icons.Default.AutoFixHigh, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Uygula")
                    }
                }

                if (uiState.resultUri != null) {
                    GlassButton(
                        onClick = { viewModel.process() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Yeniden Dene")
                    }
                }
            }
        }
    }
}
