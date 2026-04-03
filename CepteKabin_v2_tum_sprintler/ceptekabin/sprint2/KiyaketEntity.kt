package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kiyafetler")
data class KiyaketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barkod: String?,
    val marka: String,
    val model: String?,
    val tur: String,
    val beden: String,
    val renk: String?,
    val mevsim: String,
    val sezon: String?,
    val urunDurumu: String?,
    val imageUrl: String?,
    val firebaseStoragePath: String?,
    val eklenmeTarihi: Long = System.currentTimeMillis(),
    val kategoriId: Long?,
    val favori: Boolean = false,
    val kullanimSayisi: Int = 0,
    val not: String?,
    val satinAlmaTarihi: Long?,
    val satinAlmaFiyati: Double?,
    val alternatifBarkodlar: String?,
    // Sprint 2 — #2 Kirli Sepet
    val isDirty: Boolean = false
)
