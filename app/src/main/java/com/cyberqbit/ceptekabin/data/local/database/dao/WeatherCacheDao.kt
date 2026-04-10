package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1 LIMIT 1")
    suspend fun getCache(): WeatherCacheEntity?

    /** Reaktif kaynak: cache güncellenince tüm observer'lar (HomeScreen, HavaDurumu) otomatik bildirim alır */
    @Query("SELECT * FROM weather_cache WHERE id = 1 LIMIT 1")
    fun getCacheFlow(): Flow<WeatherCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}
