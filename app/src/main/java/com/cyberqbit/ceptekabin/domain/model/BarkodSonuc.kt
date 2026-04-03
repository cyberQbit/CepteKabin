package com.cyberqbit.ceptekabin.domain.model

data class BarkodSonuc(
    val barkod: String,
    val marka: String? = null,
    val model: String? = null,
    val tur: String? = null,
    val beden: String? = null,
    val renk: String? = null,
    val imageUrl: String? = null,
    val sezon: String? = null,
    val urunDurumu: UrunDurum = UrunDurum.BILINMIYOR,
    val kaynak: String = "local"
)
