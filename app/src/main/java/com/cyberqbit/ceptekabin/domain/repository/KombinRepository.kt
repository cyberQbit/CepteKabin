package com.cyberqbit.ceptekabin.domain.repository

import com.cyberqbit.ceptekabin.domain.model.Kombin
import kotlinx.coroutines.flow.Flow

interface KombinRepository {
    fun getAllKombinler(): Flow<List<Kombin>>
    suspend fun getKombinById(id: Long): Kombin?
    fun getFavoriKombinler(): Flow<List<Kombin>>
    fun getKombinlerByMinPuan(minPuan: Int): Flow<List<Kombin>>
    suspend fun insertKombin(kombin: Kombin): Long
    suspend fun updateKombin(kombin: Kombin)
    suspend fun deleteKombin(kombin: Kombin)
    suspend fun deleteKombinById(id: Long)
    suspend fun toggleFavori(id: Long, favori: Boolean)
    suspend fun incrementPuan(id: Long)
}
