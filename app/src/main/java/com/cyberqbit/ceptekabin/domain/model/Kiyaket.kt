package com.cyberqbit.ceptekabin.domain.model

import androidx.annotation.Keep

@Keep // Proguard'ın bu sınıfın ismini değiştirmesini engeller (Gson serileştirmesi için)
data class Kiyaket(
    val id: Long = 0,
    val barkod: String? = null,
    val marka: String,
    val model: String? = null,
    val tur: KiyaketTur,
    val beden: String,
    val renk: String? = null,
    val mevsim: Mevsim,
    val sezon: String? = null,
    val urunDurumu: UrunDurum? = null,
    val imageUrl: String? = null,
    val firebaseStoragePath: String? = null,
    val eklenmeTarihi: Long = System.currentTimeMillis(),
    val kategoriId: Long? = null,
    val favori: Boolean = false,
    val kullanimSayisi: Int = 0,
    val not: String? = null,
    val satinAlmaTarihi: Long? = null,
    val satinAlmaFiyati: Double? = null,
    val alternatifBarkodlar: List<String>? = null
)

@Keep // Proguard'ın bu enum'un ismini değiştirmesini engeller (Gson serileştirmesi için)
enum class KiyaketTur(val displayName: String) {
    TISORT("Tişört"), GOMLEK("Gömlek"), PANTOLON("Pantolon"), ETEK("Etek"), SORT("Şort"),
    CEKET("Ceket"), KABAN_MONT("Kaban / Mont"), KABAN("Kaban"), MONTO("Mont"), YAGMURLUK("Yağmurluk"), SWEAT("Sweat"), SWEATSHIRT("Sweatshirt"), KAZAK("Kazak"),
    HIRKA("Hırka"), ELBISE("Elbise"), AYAKKABI("Ayakkabı"), KADIN_AYAKKABISI("Kadın Ayakkabısı"), ERKEK_AYAKKABISI("Erkek Ayakkabısı"), COCUK_AYAKKABISI("Çocuk Ayakkabısı"), TERLIK("Terlik"), SANTRAFOR("Sandalet"),
    BOT("Bot"), CANTA("Çanta"), SAPKA("Şapka"), ESARP("Eşarp"), TAKI("Takı"), CORAP("Çorap"),
    ICCAMASIRI("İç Çamaşırı"), PLAJ("Plaj"), ESOFMAN("Eşofman"), KRAVAT("Kravat"), DIGER("Diğer");

    companion object {
        fun fromString(value: String?): KiyaketTur = entries.find {
            it.displayName == value || it.name == value || it.name.lowercase() == value?.lowercase()
        } ?: DIGER
    }
}

@Keep // Proguard'ın bu enum'un ismini değiştirmesini engeller (Gson serileştirmesi için)
enum class Mevsim(val displayName: String) {
    ILKBAHAR("İlkbahar"), YAZ("Yaz"), SONBAHAR("Sonbahar"), KIS("Kış"), DORT_MEVSIM("Dört Mevsim");

    companion object {
        fun fromString(value: String?): Mevsim = entries.find {
            it.displayName == value || it.name == value || it.name.lowercase() == value?.lowercase()
        } ?: DORT_MEVSIM
    }
}

@Keep // Proguard'ın bu enum'un ismini değiştirmesini engeller (Gson serileştirmesi için)
enum class Renk(val displayName: String) {
    SIYAH("Siyah"), BEYAZ("Beyaz"), GRI("Gri"), KREM("Krem"), BEJ("Bej"),
    KAHVERENGI("Kahverengi"), KIRMIZI("Kırmızı"), BORDO("Bordo"), PEMBE("Pembe"),
    MOR("Mor"), LACIVERT("Lacivert"), MAVI("Mavi"), HAKI("Haki"), YESIL("Yeşil"),
    SARI("Sarı"), TURUNCU("Turuncu"), TAS_KOPRU("Taş Köprü"), VIZON("Vizon"),
    HARDAL("Hardal"), PETROL("Petrol"), COKLU("Çoklu"), DESENLI("Desenli"), DIGER("Diğer");

    companion object {
        fun fromString(value: String?): Renk = entries.find {
            it.displayName == value || it.name == value || it.name.lowercase() == value?.lowercase()
        } ?: DIGER
    }
}

