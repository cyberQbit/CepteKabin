package com.cyberqbit.ceptekabin.domain.engine

import com.cyberqbit.ceptekabin.domain.model.HavaDurumu

object WeatherOutfitEngine {
    enum class OutfitCategory { 
        COK_SICAK, SICAK, ILIK, SERIN, SOGUK, COK_SOGUK, ASIRI_SOGUK 
    }

    fun analyzeWeather(hava: HavaDurumu?): OutfitCategory {
        if (hava == null) return OutfitCategory.ILIK // Varsayılan

        val temp = hava.sicaklik
        val feltTemp = hava.hissedilenSicaklik ?: temp

        // Basit bir kural seti (İleride neme ve rüzgara göre genişletilebilir)
        return when {
            feltTemp >= 30 -> OutfitCategory.COK_SICAK  // Şort, Askılı, Sandalet
            feltTemp in 24.0..29.9 -> OutfitCategory.SICAK       // Tişört, İnce Pantolon
            feltTemp in 18.0..23.9 -> OutfitCategory.ILIK        // Gömlek, Pantolon
            feltTemp in 12.0..17.9 -> OutfitCategory.SERIN       // Sweatshirt, İnce Ceket
            feltTemp in 5.0..11.9 -> OutfitCategory.SOGUK        // Kazak, Kaban
            feltTemp in -5.0..4.9 -> OutfitCategory.COK_SOGUK    // Kalın Mont, Atkı
            else -> OutfitCategory.ASIRI_SOGUK                   // Lahana gibi giyin
        }
    }

    fun getWeatherAdvice(category: OutfitCategory): String {
        return when(category) {
            OutfitCategory.COK_SICAK -> "Kavurucu sıcak! Şort ve ince, nefes alan kumaşlar tercih et. Şapkanı unutma."
            OutfitCategory.SICAK -> "Hava sıcak. Tişört ve keten pantolonlar/etekler kurtarıcın olacak."
            OutfitCategory.ILIK -> "Harika bir hava. Gömlek veya ince uzun kollularla rahat edersin."
            OutfitCategory.SERIN -> "Biraz esiyor olabilir. Üzerine ince bir ceket veya sweatshirt almalısın."
            OutfitCategory.SOGUK -> "Hava soğuk. Kazak ve mevsimlik kaban giyme vakti gelmiş."
            OutfitCategory.COK_SOGUK -> "Dışarısı buz gibi! Kalın bir mont, bere ve eldivensiz çıkma."
            OutfitCategory.ASIRI_SOGUK -> "Dondurucu soğuk! Termal içlikler ve en kalın giysilerini kat kat giyin."
        }
    }
}
