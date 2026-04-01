package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.KombinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KombinDao {
    @Query("SELECT * FROM kombinler ORDER BY olusturmaTarihi DESC")
    fun getAll(): Flow<List<KombinEntity>>

    @Query("SELECT * FROM kombinler WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): KombinEntity?

    @Query("SELECT * FROM kombinler WHERE favori = 1 ORDER BY olusturmaTarihi DESC")
    fun getFavoriler(): Flow<List<KombinEntity>>

    @Query("SELECT * FROM kombinler WHERE puan >= :minPuan ORDER BY puan DESC")
    fun getByMinPuan(minPuan: Int): Flow<List<KombinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KombinEntity): Long

    @Update
    suspend fun update(entity: KombinEntity)

    @Delete
    suspend fun delete(entity: KombinEntity)

    @Query("DELETE FROM kombinler WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE kombinler SET favori = :favori WHERE id = :id")
    suspend fun updateFavori(id: Long, favori: Boolean)

    @Query("UPDATE kombinler SET puan = puan + 1 WHERE id = :id")
    suspend fun incrementPuan(id: Long)
}
