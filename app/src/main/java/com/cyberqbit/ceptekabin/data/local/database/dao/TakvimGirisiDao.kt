package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.TakvimGirisiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TakvimGirisiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(girisi: TakvimGirisiEntity): Long

    @Update
    suspend fun update(girisi: TakvimGirisiEntity)

    @Delete
    suspend fun delete(girisi: TakvimGirisiEntity)

    @Query("DELETE FROM takvim_girisi WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM takvim_girisi WHERE tarih_gun = :tarihGun AND slot = :slot")
    suspend fun deleteByGunVeSlot(tarihGun: Long, slot: Int)

    @Query("SELECT * FROM takvim_girisi WHERE tarih_gun = :tarihGun ORDER BY slot ASC")
    fun getGununGirisleri(tarihGun: Long): Flow<List<TakvimGirisiEntity>>

    @Query("SELECT COUNT(*) FROM takvim_girisi WHERE tarih_gun = :tarihGun")
    suspend fun getGununKombinSayisi(tarihGun: Long): Int

    @Query("SELECT * FROM takvim_girisi WHERE tarih_gun >= :baslangic AND tarih_gun <= :bitis ORDER BY tarih_gun ASC, slot ASC")
    fun getAylikGirisler(baslangic: Long, bitis: Long): Flow<List<TakvimGirisiEntity>>

    @Query("SELECT DISTINCT tarih_gun FROM takvim_girisi WHERE tarih_gun >= :baslangic AND tarih_gun <= :bitis")
    fun getAktifGunler(baslangic: Long, bitis: Long): Flow<List<Long>>

    @Query("SELECT tarih_gun, COUNT(*) as sayi FROM takvim_girisi WHERE tarih_gun >= :baslangic AND tarih_gun <= :bitis GROUP BY tarih_gun")
    fun getGunlukKombinSayilari(baslangic: Long, bitis: Long): Flow<List<GunKombinSayisi>>

    @Query("SELECT COUNT(*) FROM takvim_girisi WHERE kombin_id = :kombinId")
    suspend fun getKombinKullanimSayisi(kombinId: Long): Int

    @Query("SELECT MAX(tarih_gun) FROM takvim_girisi WHERE kombin_id = :kombinId")
    suspend fun getKombinSonGiyimTarihi(kombinId: Long): Long?

    @Query("UPDATE takvim_girisi SET kombin_silinmis = 1 WHERE kombin_id = :kombinId")
    suspend fun markKombinSilinmis(kombinId: Long)

    @Query("DELETE FROM takvim_girisi WHERE tarih_gun < :bugun AND kombin_silinmis = 1")
    suspend fun temizleSilinmisGecmisGirisler(bugun: Long)

    // Geriye uyumluluk - eski DAO metod imzaları
    @Query("SELECT * FROM takvim_girisi WHERE tarih_gun = :gun ORDER BY slot ASC")
    suspend fun getGirislerForGun(gun: Long): List<TakvimGirisiEntity>
}

data class GunKombinSayisi(
    @ColumnInfo(name = "tarih_gun") val tarihGun: Long,
    @ColumnInfo(name = "sayi") val sayi: Int
)
