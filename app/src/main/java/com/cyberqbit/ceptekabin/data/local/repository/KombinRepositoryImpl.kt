package com.cyberqbit.ceptekabin.data.local.repository

import com.cyberqbit.ceptekabin.data.local.database.dao.KiyaketDao
import com.cyberqbit.ceptekabin.data.local.database.dao.KombinDao
import com.cyberqbit.ceptekabin.data.local.database.entity.KombinEntity
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.KiyaketTur
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.model.Mevsim
import com.cyberqbit.ceptekabin.domain.model.UrunDurum
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// FIX: inject KiyaketDao so we can resolve kiyaket relations when loading Kombins
class KombinRepositoryImpl @Inject constructor(
    private val kombinDao: KombinDao,
    private val kiyaketDao: KiyaketDao
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

    // FIX: resolve Kiyaket objects from IDs using KiyaketDao
    private suspend fun KombinEntity.toDomain(): Kombin = Kombin(
        id = id,
        ad = ad,
        ustGiyim   = ustGiyimId?.let { kiyaketDao.getById(it)?.toDomain() },
        altGiyim   = altGiyimId?.let { kiyaketDao.getById(it)?.toDomain() },
        disGiyim   = disGiyimId?.let { kiyaketDao.getById(it)?.toDomain() },
        ayakkabi   = ayakkabiId?.let { kiyaketDao.getById(it)?.toDomain() },
        aksesuar   = aksesuarId?.let { kiyaketDao.getById(it)?.toDomain() },
        olusturmaTarihi = olusturmaTarihi,
        puan = puan,
        favori = favori
    )

    private fun com.cyberqbit.ceptekabin.data.local.database.entity.KiyaketEntity.toDomain(): Kiyaket = Kiyaket(
        id = id,
        barkod = barkod,
        marka = marka,
        model = model,
        tur = KiyaketTur.fromString(tur),
        beden = beden,
        renk = renk,
        mevsim = Mevsim.fromString(mevsim),
        sezon = sezon,
        urunDurumu = UrunDurum.fromString(urunDurumu),
        imageUrl = imageUrl,
        firebaseStoragePath = firebaseStoragePath,
        eklenmeTarihi = eklenmeTarihi,
        kategoriId = kategoriId,
        favori = favori,
        kullanimSayisi = kullanimSayisi,
        not = not,
        satinAlmaTarihi = satinAlmaTarihi,
        satinAlmaFiyati = satinAlmaFiyati,
        alternatifBarkodlar = alternatifBarkodlar?.split(",")?.filter { it.isNotBlank() }
    )

    private fun Kombin.toEntity(): KombinEntity = KombinEntity(
        id = id,
        ad = ad,
        ustGiyimId  = ustGiyim?.id,
        altGiyimId  = altGiyim?.id,
        disGiyimId  = disGiyim?.id,
        ayakkabiId  = ayakkabi?.id,
        aksesuarId  = aksesuar?.id,
        olusturmaTarihi = olusturmaTarihi,
        puan = puan,
        favori = favori
    )
}
