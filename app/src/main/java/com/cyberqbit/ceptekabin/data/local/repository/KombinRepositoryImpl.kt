package com.cyberqbit.ceptekabin.data.local.repository

import com.cyberqbit.ceptekabin.data.local.database.dao.KombinDao
import com.cyberqbit.ceptekabin.data.local.database.entity.KombinEntity
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class KombinRepositoryImpl @Inject constructor(
    private val kombinDao: KombinDao
) : KombinRepository {

    override fun getAllKombinler(): Flow<List<Kombin>> {
        return kombinDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getKombinById(id: Long): Kombin? {
        return kombinDao.getById(id)?.toDomain()
    }

    override fun getFavoriKombinler(): Flow<List<Kombin>> {
        return kombinDao.getFavoriler().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getKombinlerByMinPuan(minPuan: Int): Flow<List<Kombin>> {
        return kombinDao.getByMinPuan(minPuan).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertKombin(kombin: Kombin): Long {
        return kombinDao.insert(kombin.toEntity())
    }

    override suspend fun updateKombin(kombin: Kombin) {
        kombinDao.update(kombin.toEntity())
    }

    override suspend fun deleteKombin(kombin: Kombin) {
        kombinDao.delete(kombin.toEntity())
    }

    override suspend fun deleteKombinById(id: Long) {
        kombinDao.deleteById(id)
    }

    override suspend fun toggleFavori(id: Long, favori: Boolean) {
        kombinDao.updateFavori(id, favori)
    }

    override suspend fun incrementPuan(id: Long) {
        kombinDao.incrementPuan(id)
    }

    private fun KombinEntity.toDomain(): Kombin = Kombin(
        id = id,
        ad = ad,
        olusturmaTarihi = olusturmaTarihi,
        puan = puan,
        favori = favori
    )

    private fun Kombin.toEntity(): KombinEntity = KombinEntity(
        id = id,
        ad = ad,
        ustGiyimId = ustGiyim?.id,
        altGiyimId = altGiyim?.id,
        disGiyimId = disGiyim?.id,
        ayakkabiId = ayakkabi?.id,
        aksesuarId = aksesuar?.id,
        olusturmaTarihi = olusturmaTarihi,
        puan = puan,
        favori = favori
    )
}
