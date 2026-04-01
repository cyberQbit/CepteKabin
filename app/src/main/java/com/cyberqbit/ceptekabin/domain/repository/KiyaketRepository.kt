package com.cyberqbit.ceptekabin.domain.repository

import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import kotlinx.coroutines.flow.Flow

interface KiyaketRepository {
    fun getAllKiyaketler(): Flow<List<Kiyaket>>
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
    suspend fun incrementKullanim(id: Long)
}
