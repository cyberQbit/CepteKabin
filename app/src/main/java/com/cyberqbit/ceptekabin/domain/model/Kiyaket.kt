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
) {
    /** v2: Fotoğraf yolu (imageUrl alias) */
    val fotografYolu: String? get() = imageUrl

    /** v2: Oluşturma tarihi (eklenmeTarihi alias) */
    val olusturmaTarihi: Long get() = eklenmeTarihi

    /** v2: KiyaketTur'dan otomatik kategori belirleme */
    val kategori: String get() = when (tur) {
        KiyaketTur.TISORT, KiyaketTur.GOMLEK, KiyaketTur.POLO, KiyaketTur.BLUZ,
        KiyaketTur.KAZAK, KiyaketTur.HIRKA, KiyaketTur.SWEATSHIRT, KiyaketTur.CROP_TOP,
        KiyaketTur.TANK_TOP, KiyaketTur.ATLET, KiyaketTur.ELBISE -> "Üst Giyim"
        KiyaketTur.PANTOLON, KiyaketTur.KOT_PANTOLON, KiyaketTur.SORT, KiyaketTur.ETEK,
        KiyaketTur.ESOFMAN_ALTI, KiyaketTur.TAYT, KiyaketTur.JOGGER, KiyaketTur.CHINO,
        KiyaketTur.BERMUDA -> "Alt Giyim"
        KiyaketTur.CEKET, KiyaketTur.BLAZER, KiyaketTur.KABAN_MONT, KiyaketTur.KABAN,
        KiyaketTur.MONT, KiyaketTur.PARKA, KiyaketTur.TRENCKOT, KiyaketTur.YAGMURLUK,
        KiyaketTur.DERI_CEKET, KiyaketTur.BOMBER -> "Dış Giyim"
        KiyaketTur.AYAKKABI, KiyaketTur.KADIN_AYAKKABISI, KiyaketTur.ERKEK_AYAKKABISI,
        KiyaketTur.COCUK_AYAKKABISI, KiyaketTur.SPOR_AYAKKABI, KiyaketTur.KLASIK_AYAKKABI,
        KiyaketTur.SNEAKER, KiyaketTur.LOAFER, KiyaketTur.BOT, KiyaketTur.CIZME,
        KiyaketTur.SANDALET, KiyaketTur.TERLIK, KiyaketTur.OXFORD, KiyaketTur.MOKASEN -> "Ayakkabı"
        KiyaketTur.CANTA, KiyaketTur.SIRT_CANTASI, KiyaketTur.EL_CANTASI, KiyaketTur.SAPKA,
        KiyaketTur.BERE, KiyaketTur.ESARP, KiyaketTur.KRAVAT, KiyaketTur.PAPYON,
        KiyaketTur.KEMER, KiyaketTur.KOLYE, KiyaketTur.BILEKLIK, KiyaketTur.GOZLUK,
        KiyaketTur.CORAP, KiyaketTur.ELDIVEN, KiyaketTur.TAKI -> "Aksesuar"
        else -> "Diğer"
    }
}

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
