package com.cyberqbit.ceptekabin.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cyberqbit.ceptekabin.ui.theme.*
import com.cyberqbit.ceptekabin.util.Constants

private data class TutorialStep(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)

private val tutorialSteps = listOf(
    TutorialStep(
        icon = Icons.Default.Home,
        title = "Ana Sayfa",
        description = "Hava durumuna göre kombin önerisi burada görünür. Gardırop istatistiklerini ve son eklenen kıyafetleri takip et.",
        accentColor = Color(0xFF2196F3)
    ),
    TutorialStep(
        icon = Icons.Default.Checkroom,
        title = "Dolap",
        description = "Tüm kıyafetlerini bu sekmede yönet. Kategori ve renge göre filtrele, barkod okutarak kolayca ekle.",
        accentColor = Color(0xFF9C27B0)
    ),
    TutorialStep(
        icon = Icons.Default.Style,
        title = "Kombin",
        description = "Dolabındaki kıyafetlerden kombin oluştur. Takvime plan ekle ve kombinlerini arkadaşlarınla paylaş.",
        accentColor = Color(0xFFFF9800)
    ),
    TutorialStep(
        icon = Icons.Default.WbSunny,
        title = "Hava Durumu",
        description = "Anlık hava ve 5 günlük tahmine göre yapay zeka her gün için kıyafet önerisi üretir.",
        accentColor = Color(0xFF4CAF50)
    ),
    TutorialStep(
        icon = Icons.Default.Check,
        title = "Hazırsın!",
        description = "CepteKabin hazır. Önce barkod tarayarak veya fotoğraf çekerek ilk kıyafetini ekle. İyi kombinler! 🎽",
        accentColor = PrimaryLight
    )
)

/**
 * Uygulamayı ilk kez açan kullanıcıya temel özellikleri anlatan tutorial.
 * SharedPreferences'dan [PREF_TUTORIAL_SHOWN] kontrolü yapar, bir kez gösterir.
 */
@Composable
fun TutorialOverlay(isDark: Boolean = false) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var showTutorial by remember {
        mutableStateOf(!prefs.getBoolean(Constants.PREF_TUTORIAL_SHOWN, false))
    }
    var currentStep by remember { mutableIntStateOf(0) }

    if (!showTutorial) return

    val step = tutorialSteps[currentStep]
    val isLastStep = currentStep == tutorialSteps.lastIndex

    Dialog(
        onDismissRequest = { /* kapatılamaz, sadece butonla */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* tüm ekrana tıklanmayı engelle */ },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "tutorial_step"
            ) { stepIdx ->
                val s = tutorialSteps[stepIdx]
                TutorialCard(
                    step = s,
                    stepIndex = stepIdx,
                    totalSteps = tutorialSteps.size,
                    isLastStep = stepIdx == tutorialSteps.lastIndex,
                    isDark = isDark,
                    onNext = {
                        if (stepIdx < tutorialSteps.lastIndex) {
                            currentStep++
                        } else {
                            prefs.edit().putBoolean(Constants.PREF_TUTORIAL_SHOWN, true).apply()
                            showTutorial = false
                        }
                    },
                    onSkip = {
                        prefs.edit().putBoolean(Constants.PREF_TUTORIAL_SHOWN, true).apply()
                        showTutorial = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TutorialCard(
    step: TutorialStep,
    stepIndex: Int,
    totalSteps: Int,
    isLastStep: Boolean,
    isDark: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF1C1C2E) else Color.White,
        shadowElevation = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // İkon
            Surface(
                shape = CircleShape,
                color = step.accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        step.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = step.accentColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Başlık
            Text(
                step.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Grey100 else Grey900,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Açıklama
            Text(
                step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Grey400 else Grey600,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(28.dp))

            // Adım noktaları
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(totalSteps) { i ->
                    val isActive = i == stepIndex
                    val width by animateDpAsState(
                        targetValue = if (isActive) 20.dp else 6.dp,
                        label = "dot"
                    )
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isActive) step.accentColor
                                else if (isDark) Grey700 else Grey300
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Butonlar
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = step.accentColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (isLastStep) "Başlayalım! 🎽" else "Sonraki",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            if (!isLastStep) {
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Atla",
                        color = if (isDark) Grey500 else Grey400,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
