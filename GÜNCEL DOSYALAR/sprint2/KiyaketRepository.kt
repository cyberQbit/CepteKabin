// ──────────────────────────────────────────────────────────────────────────────
// KiyaketRepository.kt — isDirty desteğiyle güncellendi
// ──────────────────────────────────────────────────────────────────────────────
package com.cyberqbit.ceptekabin.domain.repository

import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import kotlinx.coroutines.flow.Flow

interface KiyaketRepository {
    fun getAllKiyaketler(): Flow<List<Kiyaket>>
    fun getTemizKiyaketler(): Flow<List<Kiyaket>>       // isDirty == false
    fun getKirliKiyaketler(): Flow<List<Kiyaket>>       // isDirty == true
    suspend fun getKiyaketById(id: Long): Kiyaket?
    suspend fun getKiyaketByBarkod(barkod: String): Kiyaket?
    suspend fun checkBarkodExists(barkod: String): Boolean
    fun getKiyaketlerByKategori(kategoriId: Long): Flow<List<Kiyaket>>
    fun getFavoriKiyaketler(): Flow<List<Kiyaket>>
    fun getKiyaketlerByTur(tur: String): Flow<List<Kiyaket>>
    suspend fun insertKiyaket(kiyaket: Kiyaket): Long
    suspend fun updateKiyaket(kiyaket: Kiyaket)
    suspend fun deleteKiyaket(kiyaket: Kiyaket)
    suspend fun deleteKiyaketById(id: Long)
    suspend fun toggleFavori(id: Long, favori: Boolean)
    suspend fun toggleDirty(id: Long, isDirty: Boolean)  // Sprint 2
    suspend fun incrementKullanim(id: Long)
}
