package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barkod_onbellek")
data class BarkodOnbellekEntity(
    @PrimaryKey val barkod: String,
    val marka: String?,
    val model: String?,
    val tur: String?,
    val beden: String?,
    val renk: String?,
    val imageUrl: String?,
    val eklenmeTarihi: Long = System.currentTimeMillis(),
    val sonKullanimTarihi: Long = System.currentTimeMillis(),
    val kullanımSayisi: Int = 0
)
