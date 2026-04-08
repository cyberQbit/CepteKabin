package com.cyberqbit.ceptekabin.domain.engine

import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Renk

object ColorHarmonyUtil {
    private val neutralColors = setOf(Renk.SIYAH, Renk.BEYAZ, Renk.GRI, Renk.BEJ, Renk.KREM)

    fun calculateHarmonyScore(ust: Kiyaket?, alt: Kiyaket?, dis: Kiyaket? = null, ayakkabi: Kiyaket? = null): Int {
        var score = 0
        val activeColors = listOfNotNull(
            ust?.renk?.let { Renk.fromString(it) },
            alt?.renk?.let { Renk.fromString(it) },
            dis?.renk?.let { Renk.fromString(it) },
            ayakkabi?.renk?.let { Renk.fromString(it) }
        ).filter { it != Renk.DIGER }

        if (activeColors.isEmpty()) return 50 // Renk bilinmiyorsa ortalama puan

        // 1. Nötr renkler her zaman kurtarıcıdır (+10 Puan)
        val neutralCount = activeColors.count { it in neutralColors }
        score += (neutralCount * 10)

        // 2. Monokromatik Uyum (Aynı renklerin farklı tonları / Hepsi Siyah vb.)
        val uniqueColors = activeColors.toSet()
        if (uniqueColors.size == 1) {
            score += 30 // Tam uyum
        } else if (uniqueColors.size == 2 && neutralCount > 0) {
            score += 20 // 1 Canlı Renk + 1 Nötr Renk çok şıktır
        } else if (uniqueColors.size > 3) {
            score -= 15 // Çok fazla karmaşık renk göz yorar
        }

        // Güvenlik sınırları
        return score.coerceIn(0, 100)
    }
}
