package com.cyberqbit.ceptekabin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.ui.theme.*

/**
 * KiyaketDetayScreen içinde gösterilecek Kirli Sepet bölümü.
 * [isDirty] = kıyafetin şu anki durumu.
 * [onToggle] = toggle çağrısı → ViewModel'e iletilir.
 */
@Composable
fun KirliSepetSection(
    isDirty: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isDirty) Icons.Default.LocalLaundryService else Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint     = if (isDirty) Error else Success,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (isDirty) "Kirli Sepetinde" else "Temiz",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isDirty) Error else Success
                    )
                    Text(
                        if (isDirty) "Yıkanana kadar kombinlerden çıkarıldı"
                        else "Bu kıyafet kombin önerilerinde gösteriliyor",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Grey400 else Grey600
                    )
                }
            }
            TextButton(onClick = onToggle) {
                Text(
                    if (isDirty) "Temizlendi" else "Kirli Sepete Gönder",
                    color = if (isDirty) Success else Error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
