package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.KombinKullanimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KombinKullanimDao {
    /** Aylık: verilen yıl/ay aralığındaki tüm kullanımlar */
    @Query("""
        SELECT * FROM kombin_kullanimlari
        WHERE tarih BETWEEN :startMs AND :endMs
        ORDER BY tarih DESC
    """)
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<KombinKullanimEntity>>

    /** Belirli kombinin tüm kullanımları */
    @Query("SELECT * FROM kombin_kullanimlari WHERE kombinId = :kombinId ORDER BY tarih DESC")
    fun getByKombin(kombinId: Long): Flow<List<KombinKullanimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KombinKullanimEntity): Long

    @Delete
    suspend fun delete(entity: KombinKullanimEntity)

    @Query("DELETE FROM kombin_kullanimlari WHERE kombinId = :kombinId")
    suspend fun deleteByKombin(kombinId: Long)
}
