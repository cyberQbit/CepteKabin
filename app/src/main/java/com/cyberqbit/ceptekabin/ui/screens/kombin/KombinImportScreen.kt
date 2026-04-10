package com.cyberqbit.ceptekabin.ui.screens.kombin

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.ui.components.GlassButton
import com.cyberqbit.ceptekabin.ui.components.GlassCard
import com.cyberqbit.ceptekabin.ui.components.GlassSurface
import com.cyberqbit.ceptekabin.ui.theme.*

@Composable
fun KombinImportScreen(
    uri: Uri,
    onNavigateBack: () -> Unit,
    onImportSuccess: (Long) -> Unit,
    viewModel: KombinImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = true

    LaunchedEffect(uri) { viewModel.parseUri(uri) }

    LaunchedEffect(uiState.savedKombinId) {
        uiState.savedKombinId?.let { onImportSuccess(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(Color(0xFF0A0F1E), SurfaceDark)
                    else listOf(Color(0xFFEEF4FB), White)
                )
            )
    ) {
        AnimatedContent(
            targetState = when {
                uiState.isLoading -> "loading"
                uiState.error != null && uiState.exportData == null -> "error"
                uiState.exportData != null -> "content"
                else -> "loading"
            },
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "import_state"
        ) { state ->
            when (state) {
                "loading" -> LoadingState(isDark)
                "error"   -> ErrorState(
                    message = uiState.error ?: "Bilinmeyen hata",
                    onBack = onNavigateBack,
                    isDark = isDark
                )
                else      -> ContentState(
                    exportData = uiState.exportData!!,
                    isSaving = uiState.isSaving,
                    saveError = uiState.error,
                    onConfirm = { viewModel.confirmImport(onImportSuccess) },
                    onCancel = onNavigateBack,
                    isDark = isDark
                )
            }
        }
    }
}

// ── Loading ────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingState(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.85f, targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                tween(700, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ), label = "scale"
        )
        Text("g�?�", fontSize = 64.sp, modifier = Modifier.scale(scale))
        Spacer(Modifier.height(24.dp))
        Text(
            "Kombin açılıyor...",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Grey300 else Grey700
        )
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(160.dp).clip(RoundedCornerShape(8.dp)),
            color = PrimaryLight
        )
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorState(message: String, onBack: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline, null,
            modifier = Modifier.size(80.dp), tint = Error
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Kombinyi Açamadık g���",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Grey100 else Grey900,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) Grey400 else Grey600,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        GlassButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.ArrowBack, null)
            Spacer(Modifier.width(8.dp))
            Text("Geri Dön")
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────

@Composable
private fun ContentState(
    exportData: com.cyberqbit.ceptekabin.util.KombinExportData,
    isSaving: Boolean,
    saveError: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isDark: Boolean
) {
    val kombin = exportData.kombin
    val parcalar = listOfNotNull(
        kombin.ustGiyim, kombin.altGiyim,
        kombin.disGiyim, kombin.ayakkabi, kombin.aksesuar
    )

    // Fade-in animasyonu
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(
            tween(500, easing = FastOutSlowInEasing)
        ) { it / 3 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Hediye ikonu ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryLight.copy(alpha = 0.25f),
                                PrimaryDark.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) { Text("g�?�", fontSize = 52.sp) }

            Spacer(Modifier.height(20.dp))

            Text(
                "Sana Bir Kombin Geldi!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Grey100 else Grey900,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Aşağıdaki kombini dolabına eklemek ister misin?",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Grey400 else Grey600,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── Kombin kartı ──────────────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Style, null,
                        tint = AccentGold,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        kombin.ad,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Grey100 else Grey900
                    )
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(
                    color = if (isDark) GlassDarkBorder else Grey200,
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(14.dp))

                Text(
                    "${parcalar.size} Parça İçeriyor",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) Grey500 else Grey600
                )
                Spacer(Modifier.height(10.dp))

                parcalar.forEach { kiyaket ->
                    KiyaketImportRow(kiyaket, isDark)
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Hata mesajı (kayıt hatası)
            if (saveError != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = Error, modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            saveError,
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Dolabıma Ekle butonu ──────────────────────────────────────────
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryLight,
                    contentColor = White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Ekleniyor...", fontWeight = FontWeight.SemiBold)
                } else {
                    Text(
                        "g�?�  Dolabıma Ekle!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Teşekkürler, İstemiyorum",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Grey500 else Grey500
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Kıyafet satırı ────────────────────────────────────────────────────────────

@Composable
private fun KiyaketImportRow(kiyaket: Kiyaket, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassSurface(
            modifier = Modifier.size(52.dp),
            cornerRadius = 10.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!kiyaket.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = kiyaket.imageUrl,
                        contentDescription = kiyaket.marka,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Checkroom, null,
                        tint = PrimaryLight, modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                kiyaket.marka,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Grey100 else Grey900
            )
            val detail = buildString {
                append(kiyaket.tur.displayName)
                append(" · ${kiyaket.beden}")
                if (!kiyaket.renk.isNullOrBlank()) append(" · ${kiyaket.renk}")
            }
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Grey400 else Grey600
            )
        }

        Icon(
            Icons.Default.CheckCircleOutline, null,
            tint = PrimaryLight.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}
