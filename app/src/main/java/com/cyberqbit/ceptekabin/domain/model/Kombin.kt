package com.cyberqbit.ceptekabin.domain.model

import androidx.annotation.Keep

@Keep // Proguard'ın bu sınıfın ismini değiştirmesini engeller (Gson serileştirmesi için)
data class Kombin(
    val id: Long = 0,
    val ad: String,
    val ustGiyim: Kiyaket? = null,
    val altGiyim: Kiyaket? = null,
    val disGiyim: Kiyaket? = null,
    val ayakkabi: Kiyaket? = null,
    val aksesuar: Kiyaket? = null,
    val olusturmaTarihi: Long = System.currentTimeMillis(),
    val havaDurumu: HavaDurumu? = null,
    val puan: Int = 0,
    val favori: Boolean = false
)

@Keep // Proguard'ın bu sınıfın ismini değiştirmesini engeller (Gson serileştirmesi için)
data class HavaDurumu(
    val sehir: String, val sehirId: String, val sicaklik: Double, val hissedilenSicaklik: Double,
    val durum: HavaDurumuDurum, val aciklama: String, val nemOrani: Int, val ruzgarHizi: Double,
    val gunBatimi: Long, val gunDogumu: Long, val tarih: Long = System.currentTimeMillis(),
    val guncelTarih: String, val forecastList: List<ForecastItem> = emptyList()
)

@Keep // Proguard'ın bu sınıfın ismini değiştirmesini engeller (Gson serileştirmesi için)
data class ForecastItem(
    val tarih: String, val gun: String, val sicaklikMin: Double, val sicaklikMax: Double,
    val durum: HavaDurumuDurum, val nemOrani: Int, val ruzgarHizi: Double, val yağışOlasılığı: Int
)

@Keep // Proguard'ın bu enum'un ismini değiştirmesini engeller (Gson serileştirmesi için)
enum class HavaDurumuDurum(val icon: String, val displayName: String) {
    GUNESLI("sunny", "Güneşli"), BULUTLU("cloudy", "Bulutlu"), YAGMURLU("rainy", "Yağmurlu"),
    KARLI("snowy", "Karlı"), FIRTINALI("stormy", "Fırtınalı"), RUZGARLI("windy", "Rüzgarlı"),
    Sisli("foggy", "Sisli"), PARCALI_BULUTLU("partly_cloudy", "Parçalı Bulutlu"),
    YAGIS_HAKLI("rainy", "Yağışlı"), AZ_BULUTLU("few_clouds", "Az Bulutlu"),
    COK_BULUTLU("overcast", "Çok Bulutlu"), SOGUK("cold", "Soğuk"), Sicak("hot", "Sıcak"),
    NEMLI("humid", "Nemli"), BILINMIYOR("unknown", "Bilinmiyor");

    fun toEmoji(): String {
        return when(this) {
            GUNESLI, AZ_BULUTLU -> "☀️"
            PARCALI_BULUTLU -> "⛅"
            BULUTLU, COK_BULUTLU -> "☁️"
            YAGMURLU, YAGIS_HAKLI -> "🌧️"
            KARLI -> "❄️"
            FIRTINALI -> "⛈️"
            RUZGARLI -> "🌬️"
            Sisli -> "🌫️"
            SOGUK -> "🧊"
            Sicak -> "🔥"
            else -> "🌡️"
        }
    }

    companion object {
        fun fromIconAndCode(icon: String, code: Int): HavaDurumuDurum = when (code) {
            0 -> GUNESLI; 1, 2 -> AZ_BULUTLU; 3 -> COK_BULUTLU; 45, 48 -> Sisli
            51, 53, 55, 56, 57 -> YAGMURLU; 61, 63, 65, 66, 67, 80, 81, 82 -> YAGIS_HAKLI
            71, 73, 75, 77, 85, 86 -> KARLI; 95, 96, 99 -> FIRTINALI; else -> BILINMIYOR
        }
    }
}

data class Kategori(val id: Long = 0, val ad: String, val ikon: String? = null, val sirasi: Int = 0)

object Kategoriler {
    val defaultKategoriler = listOf(
        Kategori(1, "Üst Giyim", "shirt", 1), Kategori(2, "Alt Giyim", "pants", 2),
        Kategori(3, "Dış Giyim", "jacket", 3), Kategori(4, "Ayakkabı", "shoe", 4),
        Kategori(5, "Aksesuar", "accessory", 5)
    )
}
