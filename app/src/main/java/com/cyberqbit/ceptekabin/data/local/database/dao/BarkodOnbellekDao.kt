package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.BarkodOnbellekEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarkodOnbellekDao {
    @Query("SELECT * FROM barkod_onbellek WHERE barkod = :barkod LIMIT 1")
    suspend fun getByBarkod(barkod: String): BarkodOnbellekEntity?

    @Query("SELECT * FROM barkod_onbellek ORDER BY sonKullanimTarihi DESC")
    fun getAll(): Flow<List<BarkodOnbellekEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BarkodOnbellekEntity)

    @Update
    suspend fun update(entity: BarkodOnbellekEntity)

    @Delete
    suspend fun delete(entity: BarkodOnbellekEntity)

    @Query("DELETE FROM barkod_onbellek WHERE barkod = :barkod")
    suspend fun deleteByBarkod(barkod: String)

    @Query("UPDATE barkod_onbellek SET sonKullanimTarihi = :timestamp, kullanımSayisi = kullanımSayisi + 1 WHERE barkod = :barkod")
    suspend fun updateUsage(barkod: String, timestamp: Long = System.currentTimeMillis())
}
