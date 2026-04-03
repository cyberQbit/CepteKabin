package com.cyberqbit.ceptekabin.data.local.database.dao

import androidx.room.*
import com.cyberqbit.ceptekabin.data.local.database.entity.KiyaketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KiyaketDao {
    @Query("SELECT * FROM kiyafetler ORDER BY eklenmeTarihi DESC")
    fun getAll(): Flow<List<KiyaketEntity>>

    /** Sprint 2 — Kirli sepet dışındaki temiz kıyafetler */
    @Query("SELECT * FROM kiyafetler WHERE isDirty = 0 ORDER BY eklenmeTarihi DESC")
    fun getTemiz(): Flow<List<KiyaketEntity>>

    /** Sprint 2 — Kirli sepetteki kıyafetler */
    @Query("SELECT * FROM kiyafetler WHERE isDirty = 1 ORDER BY eklenmeTarihi DESC")
    fun getKirli(): Flow<List<KiyaketEntity>>

    @Query("SELECT * FROM kiyafetler WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): KiyaketEntity?

    @Query("SELECT * FROM kiyafetler WHERE barkod = :barkod LIMIT 1")
    suspend fun getByBarkod(barkod: String): KiyaketEntity?

    @Query("SELECT COUNT(*) FROM kiyafetler WHERE barkod = :barkod")
    suspend fun checkBarkodExists(barkod: String): Int

    @Query("SELECT * FROM kiyafetler WHERE kategoriId = :kategoriId ORDER BY eklenmeTarihi DESC")
    fun getByKategori(kategoriId: Long): Flow<List<KiyaketEntity>>

    @Query("SELECT * FROM kiyafetler WHERE favori = 1 ORDER BY eklenmeTarihi DESC")
    fun getFavoriler(): Flow<List<KiyaketEntity>>

    @Query("SELECT * FROM kiyafetler WHERE tur = :tur ORDER BY eklenmeTarihi DESC")
    fun getByTur(tur: String): Flow<List<KiyaketEntity>>

    @Query("SELECT * FROM kiyafetler WHERE marka LIKE '%' || :marka || '%' ORDER BY eklenmeTarihi DESC")
    fun getByMarka(marka: String): Flow<List<KiyaketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KiyaketEntity): Long

    @Update
    suspend fun update(entity: KiyaketEntity)

    @Delete
    suspend fun delete(entity: KiyaketEntity)

    @Query("DELETE FROM kiyafetler WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE kiyafetler SET favori = :favori WHERE id = :id")
    suspend fun updateFavori(id: Long, favori: Boolean)

    /** Sprint 2 — kirli/temiz toggle */
    @Query("UPDATE kiyafetler SET isDirty = :isDirty WHERE id = :id")
    suspend fun updateDirty(id: Long, isDirty: Boolean)

    @Query("UPDATE kiyafetler SET kullanimSayisi = kullanimSayisi + 1 WHERE id = :id")
    suspend fun incrementKullanim(id: Long)
}
