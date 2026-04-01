package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sezonlu_urunler")
data class SezonluUrunEntity(
    @PrimaryKey val barkod: String,
    val marka: String,
    val model: String,
    val tur: String,
    val sezon: String,
    val durum: String,
    val alternatifBarkodlar: String,
    val guncellemeTarihi: Long = System.currentTimeMillis()
)
