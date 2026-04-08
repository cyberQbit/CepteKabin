package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "takvim_girisleri")
data class TakvimGirisiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tarihGunu: Long, // Gece yarısı (00:00) timestamp'i
    val ogun: String, // Örn: "SABAH", "ÖĞLE", "AKŞAM"
    val kombinId: Long,
    val kombinAd: String, // Kombin silinse bile adı kalsın diye (Snapshot)
    val kombinGorselleri: String // Virgülle ayrılmış resim URL'leri (Snapshot)
)
