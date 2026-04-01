package com.cyberqbit.ceptekabin.domain.repository

import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc

interface BarkodRepository {
    suspend fun barkodAra(barkod: String): Result<BarkodSonuc>
    suspend fun getBarkodOnbellek(barkod: String): BarkodSonuc?
    suspend fun saveBarkodOnbellek(sonuc: BarkodSonuc)
    suspend fun deleteBarkodOnbellek(barkod: String)
}
