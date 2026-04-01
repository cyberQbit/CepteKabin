package com.cyberqbit.ceptekabin.domain.model

import androidx.annotation.Keep

@Keep // Proguard'ın bu enum'un ismini değiştirmesini engeller (Gson serileştirmesi için)
enum class UrunDurum(val displayName: String, val description: String) {
    SATISTA("Satışta", "Ürün hala mağazalarda satışta"),
    STOKTA_YOK("Stokta Yok", "Ürün şu an stokta yok ama tekrar gelebilir"),
    DURDURULDU("Üretimi Durduruldu", "Bu ürün artık üretilmiyor"),
    SATIS_DISI("Satış Dışı", "Ürün kalıcı olarak satıştan kaldırıldı"),
    BILINMIYOR("Bilinmiyor", "Ürün durumu belirlenmedi");

    companion object { fun fromString(value: String?): UrunDurum = entries.find { it.name == value } ?: BILINMIYOR }
}

object UrunOncelik {
    val ON_TAVSIYE_EDILEN = listOf(UrunDurum.SATISTA, UrunDurum.STOKTA_YOK)
    val ONAY_ILE_KULLANILABILIR = listOf(UrunDurum.DURDURULDU, UrunDurum.SATIS_DISI)
    fun kullanilabilirMi(durum: UrunDurum, kullaniciOnayli: Boolean) = durum in ON_TAVSIYE_EDILEN || (kullaniciOnayli && durum in ONAY_ILE_KULLANILABILIR)
}

@Keep // Proguard'ın bu enum'un ismini değiştirmesini engeller (Gson serileştirmesi için)
enum class Sezon(val displayName: String, val year: Int?) {
    BILINMIYOR("Bilinmiyor", null), ILKBAHAR_2024("2024 İlkbahar", 2024), YAZ_2024("2024 Yaz", 2024),
    SONBAHAR_2024("2024 Sonbahar", 2024), KIS_2024_2025("2024-2025 Kış", 2024),
    ILKBAHAR_2025("2025 İlkbahar", 2025), YAZ_2025("2025 Yaz", 2025),
    SONBAHAR_2025("2025 Sonbahar", 2025), KIS_2025_2026("2025-2026 Kış", 2025);

    companion object { fun fromString(value: String?): Sezon = entries.find { it.displayName == value } ?: BILINMIYOR }
}
