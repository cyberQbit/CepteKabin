package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberqbit.ceptekabin.data.local.database.entity.TakvimGirisiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TakvimGirisiDao {
    @Query("SELECT * FROM takvim_girisleri WHERE tarihGunu >= :baslangic AND tarihGunu <= :bitis ORDER BY tarihGunu ASC")
    fun getTakvimGirisleri(baslangic: Long, bitis: Long): Flow<List<TakvimGirisiEntity>>

    @Query("SELECT * FROM takvim_girisleri WHERE tarihGunu = :gun")
    suspend fun getGirislerForGun(gun: Long): List<TakvimGirisiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTakvimGirisi(giris: TakvimGirisiEntity)

    @Delete
    suspend fun deleteTakvimGirisi(giris: TakvimGirisiEntity)

    @Query("DELETE FROM takvim_girisleri WHERE id = :id")
    suspend fun deleteById(id: Long)
}
