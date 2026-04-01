package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kombinler")
data class KombinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ad: String,
    val ustGiyimId: Long?,
    val altGiyimId: Long?,
    val disGiyimId: Long?,
    val ayakkabiId: Long?,
    val aksesuarId: Long?,
    val olusturmaTarihi: Long = System.currentTimeMillis(),
    val puan: Int = 0,
    val favori: Boolean = false
)
