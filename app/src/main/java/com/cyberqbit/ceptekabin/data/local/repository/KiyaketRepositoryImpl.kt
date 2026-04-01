package com.cyberqbit.ceptekabin.data.local.repository

import com.cyberqbit.ceptekabin.data.local.database.dao.KiyaketDao
import com.cyberqbit.ceptekabin.data.local.database.entity.KiyaketEntity
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.KiyaketTur
import com.cyberqbit.ceptekabin.domain.model.Mevsim
import com.cyberqbit.ceptekabin.domain.model.UrunDurum
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class KiyaketRepositoryImpl @Inject constructor(
    private val kiyaketDao: KiyaketDao
) : KiyaketRepository {

    override fun getAllKiyaketler(): Flow<List<Kiyaket>> {
        return kiyaketDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getKiyaketById(id: Long): Kiyaket? {
        return kiyaketDao.getById(id)?.toDomain()
    }

    override suspend fun getKiyaketByBarkod(barkod: String): Kiyaket? {
        return kiyaketDao.getByBarkod(barkod)?.toDomain()
    }
    override suspend fun checkBarkodExists(barkod: String): Boolean {
        return kiyaketDao.checkBarkodExists(barkod) > 0
    }
    override fun getKiyaketlerByKategori(kategoriId: Long): Flow<List<Kiyaket>> {
        return kiyaketDao.getByKategori(kategoriId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriKiyaketler(): Flow<List<Kiyaket>> {
        return kiyaketDao.getFavoriler().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getKiyaketlerByTur(tur: String): Flow<List<Kiyaket>> {
        return kiyaketDao.getByTur(tur).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertKiyaket(kiyaket: Kiyaket): Long {
        return kiyaketDao.insert(kiyaket.toEntity())
    }

    override suspend fun updateKiyaket(kiyaket: Kiyaket) {
        kiyaketDao.update(kiyaket.toEntity())
    }

    override suspend fun deleteKiyaket(kiyaket: Kiyaket) {
        kiyaketDao.delete(kiyaket.toEntity())
    }

    override suspend fun deleteKiyaketById(id: Long) {
        kiyaketDao.deleteById(id)
    }

    override suspend fun toggleFavori(id: Long, favori: Boolean) {
        kiyaketDao.updateFavori(id, favori)
    }

    override suspend fun incrementKullanim(id: Long) {
        kiyaketDao.incrementKullanim(id)
    }

    private fun KiyaketEntity.toDomain(): Kiyaket = Kiyaket(
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

    private fun Kiyaket.toEntity(): KiyaketEntity = KiyaketEntity(
        id = id,
        barkod = barkod,
        marka = marka,
        model = model,
        tur = tur.name,
        beden = beden,
        renk = renk,
        mevsim = mevsim.name,
        sezon = sezon,
        urunDurumu = urunDurumu?.name,
        imageUrl = imageUrl,
        firebaseStoragePath = firebaseStoragePath,
        eklenmeTarihi = eklenmeTarihi,
        kategoriId = kategoriId,
        favori = favori,
        kullanimSayisi = kullanimSayisi,
        not = not,
        satinAlmaTarihi = satinAlmaTarihi,
        satinAlmaFiyati = satinAlmaFiyati,
        alternatifBarkodlar = alternatifBarkodlar?.joinToString(",")
    )
}
