package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.WeatherCacheEntity

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1 LIMIT 1")
    suspend fun getCache(): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}
