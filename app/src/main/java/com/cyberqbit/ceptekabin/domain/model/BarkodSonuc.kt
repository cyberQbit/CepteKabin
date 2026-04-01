package com.cyberqbit.ceptekabin.domain.model

data class BarkodSonuc(
    val barkod: String,
    val marka: String?,
    val model: String?,
    val tur: String?,
    val beden: String?,
    val renk: String?,
    val imageUrl: String?,
    val sezon: String? = null,
    val urunDurumu: UrunDurum = UrunDurum.BILINMIYOR,
    val kaynak: String = "local"
)
