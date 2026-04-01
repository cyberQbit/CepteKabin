package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.KategoriEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KategoriDao {
    @Query("SELECT * FROM kategoriler ORDER BY sirasi ASC")
    fun getAll(): Flow<List<KategoriEntity>>

    @Query("SELECT * FROM kategoriler WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): KategoriEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KategoriEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<KategoriEntity>)

    @Update
    suspend fun update(entity: KategoriEntity)

    @Delete
    suspend fun delete(entity: KategoriEntity)

    @Query("SELECT COUNT(*) FROM kategoriler")
    suspend fun getCount(): Int
}
