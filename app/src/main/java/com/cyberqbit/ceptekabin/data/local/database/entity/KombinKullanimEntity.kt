package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kombin_kullanim",
    foreignKeys = [
        ForeignKey(
            entity = KombinEntity::class,
            parentColumns = ["id"],
            childColumns = ["kombinId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("kombinId")]
)
data class KombinKullanimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kombinId: Long,
    val tarih: Long = System.currentTimeMillis()
)
