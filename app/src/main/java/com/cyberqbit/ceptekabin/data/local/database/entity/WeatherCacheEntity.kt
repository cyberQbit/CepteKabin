package com.cyberqbit.ceptekabin.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Int = 1,           // Tek satır, her zaman üzerine yazar
    val sehir: String,
    val sicaklik: Double,
    val hissedilen: Double,
    val durum: String,
    val nemOrani: Int,
    val ruzgarHizi: Double,
    val forecastJson: String,              // Gson ile serileştirilmiş ForecastItem listesi
    val kayitTarihi: Long = System.currentTimeMillis()
)
