package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.KombinKullanimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KombinKullanimDao {
    @Insert
    suspend fun insert(entity: KombinKullanimEntity): Long

    @Delete
    suspend fun delete(entity: KombinKullanimEntity)

    @Query("SELECT * FROM kombin_kullanim WHERE kombinId = :kombinId ORDER BY tarih DESC")
    fun getByKombinId(kombinId: Long): Flow<List<KombinKullanimEntity>>

    @Query("SELECT * FROM kombin_kullanim WHERE tarih BETWEEN :startMs AND :endMs ORDER BY tarih DESC")
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<KombinKullanimEntity>>

    @Query("SELECT COUNT(*) FROM kombin_kullanim WHERE kombinId = :kombinId")
    suspend fun getUsageCount(kombinId: Long): Int
}
