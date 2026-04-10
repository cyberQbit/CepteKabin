package com.cyberqbit.ceptekabin.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.BuildConfig
import com.cyberqbit.ceptekabin.data.worker.UpdateCheckWorker
import com.cyberqbit.ceptekabin.ui.theme.PrimaryLight
import com.cyberqbit.ceptekabin.util.Constants
import com.cyberqbit.ceptekabin.util.KombinShareHelper

/**
 * BuildConfig.VERSION_NAME ile GitHub'daki son release tag'ini karşılaştırır.
 * Yeni sürüm varsa ve daha önce dismiss edilmemişse banner gösterir.
 */
@Composable
fun UpdateBanner() {
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var showBanner by remember {
        val latest    = prefs.getString(UpdateCheckWorker.PREF_LATEST_VERSION_NAME, null)
        val dismissed = prefs.getString(Constants.PREF_DISMISSED_UPDATE_VERSION, null)
        val current   = BuildConfig.VERSION_NAME
        // Tag format: "v1.2.0" veya "1.2.0" — her ikisini de destekle
        val latestClean = latest?.removePrefix("v") ?: ""
        val currentClean = current.removePrefix("v")
        mutableStateOf(
            latestClean.isNotBlank() &&
            latestClean != currentClean &&
            dismissed != latestClean
        )
    }

    val latestVersion = prefs.getString(UpdateCheckWorker.PREF_LATEST_VERSION_NAME, null) ?: ""

    AnimatedVisibility(
        visible = showBanner,
        enter   = slideInVertically { -it } + fadeIn(),
        exit    = slideOutVertically { -it } + fadeOut()
    ) {
        Surface(
            color  = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint     = PrimaryLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Yeni sürüm: $latestVersion",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "En iyi deneyim için güncelleyin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                TextButton(onClick = {
                    // Play Store'a yönlendir (GitHub release URL yerine)
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(KombinShareHelper.PLAY_STORE_LINK))
                    )
                }) {
                    Text("Güncelle", color = PrimaryLight, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = {
                        val clean = latestVersion.removePrefix("v")
                        prefs.edit()
                            .putString(Constants.PREF_DISMISSED_UPDATE_VERSION, clean)
                            .apply()
                        showBanner = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, "Kapat",
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                }
            }
        }
    }
}
