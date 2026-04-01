package com.cyberqbit.ceptekabin.domain.repository

import com.cyberqbit.ceptekabin.domain.model.HavaDurumu

interface HavaDurumuRepository {
    suspend fun getWeatherByCity(city: String): Result<HavaDurumu>
    suspend fun getWeatherByLocation(lat: Double, lon: Double): Result<HavaDurumu>
}
