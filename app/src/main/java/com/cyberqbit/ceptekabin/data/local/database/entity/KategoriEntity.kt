package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kategoriler")
data class KategoriEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ad: String,
    val ikon: String?,
    val sirasi: Int = 0
)
