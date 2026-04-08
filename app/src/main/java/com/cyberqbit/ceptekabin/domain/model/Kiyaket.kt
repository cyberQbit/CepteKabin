package com.cyberqbit.ceptekabin.domain.model

import androidx.annotation.Keep

@Keep
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

@Keep
enum class KiyaketTur(val displayName: String) {
    TISORT("Tişört"), GOMLEK("Gömlek"), POLO("Polo"), BLUZ("Bluz"), KAZAK("Kazak"), HIRKA("Hırka"),
    SWEATSHIRT("Sweatshirt"), CROP_TOP("Crop Top"), TANK_TOP("Tank Top"), ELBISE("Elbise"), ATLET("Atlet"),
    PANTOLON("Pantolon"), KOT_PANTOLON("Kot Pantolon"), SORT("Şort"), ETEK("Etek"), ESOFMAN_ALTI("Eşofman Altı"),
    TAYT("Tayt"), JOGGER("Jogger"), CHINO("Chino"), BERMUDA("Bermuda"),
    CEKET("Ceket"), BLAZER("Blazer"), KABAN_MONT("Kaban / Mont"), KABAN("Kaban"), MONT("Mont"), PARKA("Parka"), TRENCKOT("Trençkot"),
    YAGMURLUK("Yağmurluk"), DERI_CEKET("Deri Ceket"), BOMBER("Bomber"),
    AYAKKABI("Ayakkabı"), KADIN_AYAKKABISI("Kadın Ayakkabısı"), ERKEK_AYAKKABISI("Erkek Ayakkabısı"), COCUK_AYAKKABISI("Çocuk Ayakkabısı"),
    SPOR_AYAKKABI("Spor Ayakkabı"), KLASIK_AYAKKABI("Klasik Ayakkabı"), SNEAKER("Sneaker"), LOAFER("Loafer"),
    BOT("Bot"), CIZME("Çizme"), SANDALET("Sandalet"), TERLIK("Terlik"), OXFORD("Oxford"), MOKASEN("Mokasen"),
    CANTA("Çanta"), SIRT_CANTASI("Sırt Çantası"), EL_CANTASI("El Çantası"), SAPKA("Şapka"), BERE("Bere"),
    ESARP("Eşarp"), KRAVAT("Kravat"), PAPYON("Papyon"), KEMER("Kemer"), KOLYE("Kolye"), BILEKLIK("Bileklik"),
    GOZLUK("Gözlük"), CORAP("Çorap"), ELDIVEN("Eldiven"), TAKI("Takı"),
    ICCAMASIRI("İç Çamaşırı"), PLAJ("Plaj"), ESOFMAN("Eşofman"), DIGER("Diğer");

    companion object {
        fun fromString(value: String?): KiyaketTur = entries.find {
            it.displayName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: DIGER
    }
}

@Keep
enum class Mevsim(val displayName: String) {
    ILKBAHAR("İlkbahar"), YAZ("Yaz"), SONBAHAR("Sonbahar"), KIS("Kış"), DORT_MEVSIM("Dört Mevsim");

    companion object {
        fun fromString(value: String?): Mevsim = entries.find {
            it.displayName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: DORT_MEVSIM
    }
}

@Keep
enum class Renk(val displayName: String) {
    SIYAH("Siyah"), BEYAZ("Beyaz"), GRI("Gri"), KREM("Krem"), BEJ("Bej"),
    KAHVERENGI("Kahverengi"), KIRMIZI("Kırmızı"), BORDO("Bordo"), PEMBE("Pembe"),
    MOR("Mor"), LACIVERT("Lacivert"), MAVI("Mavi"), HAKI("Haki"), YESIL("Yeşil"),
    SARI("Sarı"), TURUNCU("Turuncu"), TAS_KOPRU("Taş Köprü"), VIZON("Vizon"),
    HARDAL("Hardal"), PETROL("Petrol"), COKLU("Çoklu"), DESENLI("Desenli"), DIGER("Diğer");

    companion object {
        fun fromString(value: String?): Renk = entries.find {
            it.displayName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: DIGER
    }
}
