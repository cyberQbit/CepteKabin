package com.cyberqbit.ceptekabin.domain.engine

import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.KiyaketTur
import com.cyberqbit.ceptekabin.domain.model.Kombin

object SmartKombinSuggester {

    fun generateSmartKombins(
        tumKiyafetler: List<Kiyaket>, 
        weatherCategory: WeatherOutfitEngine.OutfitCategory
    ): List<Kombin> {
        if (tumKiyafetler.isEmpty()) return emptyList()

        val generatedKombins = mutableListOf<Kombin>()

        // Kıyafetleri mantıksal listelere ayır (Constants vs Enum karmaşasını önlemek için basite indirgendi)
        val ustGiyimler = tumKiyafetler.filter { it.tur in listOf(KiyaketTur.TISORT, KiyaketTur.GOMLEK, KiyaketTur.POLO, KiyaketTur.SWEATSHIRT, KiyaketTur.KAZAK) }
        val altGiyimler = tumKiyafetler.filter { it.tur in listOf(KiyaketTur.PANTOLON, KiyaketTur.KOT_PANTOLON, KiyaketTur.SORT, KiyaketTur.ESOFMAN_ALTI) }
        val disGiyimler = tumKiyafetler.filter { it.tur in listOf(KiyaketTur.CEKET, KiyaketTur.MONT, KiyaketTur.KABAN, KiyaketTur.YAGMURLUK) }

        // Havaya uygun filtreleme yapalım
        val uygunUstler = ustGiyimler.filter { isSuitableForWeather(it.tur, weatherCategory) }.shuffled()
        val uygunAltlar = altGiyimler.filter { isSuitableForWeather(it.tur, weatherCategory) }.shuffled()
        val uygunDislar = disGiyimler.filter { isSuitableForWeather(it.tur, weatherCategory) }.shuffled()

        // 3 Farklı kombin oluşturmaya çalış
        for (i in 0 until 3) {
            val secilenUst = uygunUstler.getOrNull(i)
            val secilenAlt = uygunAltlar.getOrNull(i)
            
            // Eğer üst veya alt yoksa kombin yapamayız
            if (secilenUst == null || secilenAlt == null) continue

            // Dış giyim gerekiyorsa ekle
            val secilenDis = if (weatherCategory in listOf(WeatherOutfitEngine.OutfitCategory.SERIN, WeatherOutfitEngine.OutfitCategory.SOGUK, WeatherOutfitEngine.OutfitCategory.COK_SOGUK)) {
                uygunDislar.getOrNull(i)
            } else null

            // Renk uyum puanı hesapla (İleride bu puana göre filtreleme/sıralama yapılabilir)
            val harmonyScore = ColorHarmonyUtil.calculateHarmonyScore(secilenUst, secilenAlt, secilenDis)

            val kombinAdi = "AI Önerisi: ${secilenUst.renk ?: "Şık"} & ${secilenAlt.renk ?: "Rahat"}"
            
            generatedKombins.add(
                Kombin(
                    id = -(System.currentTimeMillis() + i), // Kaydedilmemiş sanal id
                    ad = kombinAdi,
                    ustGiyim = secilenUst,
                    altGiyim = secilenAlt,
                    disGiyim = secilenDis,
                    olusturmaTarihi = System.currentTimeMillis(),
                    puan = harmonyScore
                )
            )
        }

        return generatedKombins
    }

    private fun isSuitableForWeather(tur: KiyaketTur, weather: WeatherOutfitEngine.OutfitCategory): Boolean {
        return when (weather) {
            WeatherOutfitEngine.OutfitCategory.COK_SICAK -> tur in listOf(KiyaketTur.TISORT, KiyaketTur.SORT, KiyaketTur.CROP_TOP)
            WeatherOutfitEngine.OutfitCategory.SICAK -> tur in listOf(KiyaketTur.TISORT, KiyaketTur.GOMLEK, KiyaketTur.PANTOLON, KiyaketTur.KOT_PANTOLON)
            WeatherOutfitEngine.OutfitCategory.ILIK -> tur in listOf(KiyaketTur.GOMLEK, KiyaketTur.PANTOLON, KiyaketTur.KOT_PANTOLON)
            WeatherOutfitEngine.OutfitCategory.SERIN -> tur in listOf(KiyaketTur.SWEATSHIRT, KiyaketTur.CEKET, KiyaketTur.PANTOLON)
            WeatherOutfitEngine.OutfitCategory.SOGUK, WeatherOutfitEngine.OutfitCategory.COK_SOGUK, WeatherOutfitEngine.OutfitCategory.ASIRI_SOGUK -> tur in listOf(KiyaketTur.KAZAK, KiyaketTur.MONT, KiyaketTur.KABAN, KiyaketTur.PANTOLON)
            else -> false
        }
    }
}
