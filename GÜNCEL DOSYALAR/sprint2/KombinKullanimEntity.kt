package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Her "Giydim" kaydı — OOTD takvimine beslenir */
@Entity(
    tableName = "kombin_kullanimlari",
    foreignKeys = [ForeignKey(
        entity      = KombinEntity::class,
        parentColumns = ["id"],
        childColumns  = ["kombinId"],
        onDelete    = ForeignKey.CASCADE
    )]
)
data class KombinKullanimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kombinId: Long,
    val tarih: Long = System.currentTimeMillis(),   // epoch ms
    val not: String? = null
)
