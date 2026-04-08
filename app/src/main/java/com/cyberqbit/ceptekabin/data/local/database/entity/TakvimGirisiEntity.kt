package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.*

@Entity(
    tableName = "takvim_girisi",
    indices = [
        Index(value = ["tarih_gun", "slot"], unique = true),
        Index(value = ["kombin_id"])
    ]
)
data class TakvimGirisiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tarih_gun")
    val tarihGun: Long,

    @ColumnInfo(name = "slot")
    val slot: Int = 0,

    @ColumnInfo(name = "kombin_id")
    val kombinId: Long,

    @ColumnInfo(name = "kombin_ad")
    val kombinAd: String,

    @ColumnInfo(name = "ust_giyim_ad")
    val ustGiyimAd: String? = null,

    @ColumnInfo(name = "alt_giyim_ad")
    val altGiyimAd: String? = null,

    @ColumnInfo(name = "dis_giyim_ad")
    val disGiyimAd: String? = null,

    @ColumnInfo(name = "ayakkabi_ad")
    val ayakkabiAd: String? = null,

    @ColumnInfo(name = "aksesuar_ad")
    val aksesuarAd: String? = null,

    @ColumnInfo(name = "ust_giyim_resim")
    val ustGiyimResim: String? = null,

    @ColumnInfo(name = "alt_giyim_resim")
    val altGiyimResim: String? = null,

    @ColumnInfo(name = "dis_giyim_resim")
    val disGiyimResim: String? = null,

    @ColumnInfo(name = "ayakkabi_resim")
    val ayakkabiResim: String? = null,

    @ColumnInfo(name = "aksesuar_resim")
    val aksesuarResim: String? = null,

    @ColumnInfo(name = "ekleme_zamani")
    val eklemeZamani: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "kombin_silinmis")
    val kombinSilinmis: Boolean = false
)
