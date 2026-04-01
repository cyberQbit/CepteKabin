package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.SezonluUrunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SezonluUrunDao {
    @Query("SELECT * FROM sezonlu_urunler WHERE barkod = :barkod LIMIT 1")
    suspend fun getByBarkod(barkod: String): SezonluUrunEntity?

    @Query("SELECT * FROM sezonlu_urunler WHERE durum = :durum ORDER BY guncellemeTarihi DESC")
    fun getByDurum(durum: String): Flow<List<SezonluUrunEntity>>

    @Query("SELECT * FROM sezonlu_urunler ORDER BY guncellemeTarihi DESC")
    fun getAll(): Flow<List<SezonluUrunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SezonluUrunEntity)

    @Update
    suspend fun update(entity: SezonluUrunEntity)

    @Delete
    suspend fun delete(entity: SezonluUrunEntity)

    @Query("UPDATE sezonlu_urunler SET durum = :durum, guncellemeTarihi = :timestamp WHERE barkod = :barkod")
    suspend fun updateDurum(barkod: String, durum: String, timestamp: Long = System.currentTimeMillis())
}
