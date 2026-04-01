package com.cyberqbit.ceptekabin.data.repository

import com.cyberqbit.ceptekabin.data.remote.api.HavaDurumuApiService
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.repository.HavaDurumuRepository
import javax.inject.Inject

class HavaDurumuRepositoryImpl @Inject constructor(
    private val apiService: HavaDurumuApiService
) : HavaDurumuRepository {

    override suspend fun getWeatherByCity(city: String): Result<HavaDurumu> {
        return apiService.getWeatherByCity(city)
    }

    override suspend fun getWeatherByLocation(lat: Double, lon: Double): Result<HavaDurumu> {
        return apiService.getWeatherByLocation(lat, lon)
    }
}
