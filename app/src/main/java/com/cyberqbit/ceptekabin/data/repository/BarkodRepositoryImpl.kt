package com.cyberqbit.ceptekabin.data.repository

import com.cyberqbit.ceptekabin.data.local.database.dao.BarkodOnbellekDao
import com.cyberqbit.ceptekabin.data.local.database.dao.SezonluUrunDao
import com.cyberqbit.ceptekabin.data.local.database.entity.BarkodOnbellekEntity
import com.cyberqbit.ceptekabin.data.remote.api.OpenBeautyFactsApi
import com.cyberqbit.ceptekabin.data.remote.api.TrendyolSearchApi
import com.cyberqbit.ceptekabin.data.remote.api.UPCItemDbApi
import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import com.cyberqbit.ceptekabin.domain.model.UrunDurum
import com.cyberqbit.ceptekabin.domain.repository.BarkodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class BarkodRepositoryImpl @Inject constructor(
    private val barkodOnbellekDao: BarkodOnbellekDao,
    private val sezonluUrunDao: SezonluUrunDao,
    private val okHttpClient: OkHttpClient
) : BarkodRepository {

    private val upcItemDbApi = UPCItemDbApi(okHttpClient)
    private val openBeautyFactsApi = OpenBeautyFactsApi(okHttpClient)
    private val trendyolSearchApi = TrendyolSearchApi(okHttpClient)

    override suspend fun barkodAra(barkod: String): Result<BarkodSonuc> = withContext(Dispatchers.IO) {
        // ══════════════════════════════════════════════════════
        // AŞAMA 1: Local Cache (Anında)
        // ══════════════════════════════════════════════════════
        getBarkodOnbellek(barkod)?.let {
            return@withContext Result.success(it.copy(kaynak = "yerel_bellegim"))
        }

        // ══════════════════════════════════════════════════════
        // AŞAMA 2A: UPCitemdb API (Uluslararası ürünler)
        // ══════════════════════════════════════════════════════
        try {
            val upcResult = upcItemDbApi.searchBarkod(barkod)
            if (upcResult != null) {
                val sonuc = upcResult.copy(kaynak = "upcitemdb")
                saveBarkodOnbellek(sonuc)
                return@withContext Result.success(sonuc)
            }
        } catch (e: Exception) {
            // Hata oldu, devam et
        }

        // ══════════════════════════════════════════════════════
        // AŞAMA 2B: Open Beauty Facts API (Kozmetik/Parfüm)
        // ══════════════════════════════════════════════════════
        try {
            val beautyResult = openBeautyFactsApi.searchBarkod(barkod)
            if (beautyResult != null) {
                val sonuc = beautyResult.copy(kaynak = "openbeautyfacts")
                saveBarkodOnbellek(sonuc)
                return@withContext Result.success(sonuc)
            }
        } catch (e: Exception) {
            // Hata oldu, devam et
        }

        // ══════════════════════════════════════════════════════
        // AŞAMA 2C: Sezonlu Ürün Veritabanı
        // ══════════════════════════════════════════════════════
        try {
            val sezonluUrun = sezonluUrunDao.getByBarkod(barkod)
            if (sezonluUrun != null) {
                val sonuc = BarkodSonuc(
                    barkod = barkod,
                    marka = sezonluUrun.marka,
                    model = sezonluUrun.model,
                    tur = sezonluUrun.tur,
                    beden = null,
                    renk = null,
                    imageUrl = null,
                    sezon = sezonluUrun.sezon,
                    urunDurumu = UrunDurum.fromString(sezonluUrun.durum),
                    kaynak = "sezonlu_urun"
                )
                return@withContext Result.success(sonuc)
            }
        } catch (e: Exception) {
            // Hata oldu, devam et
        }

        // ══════════════════════════════════════════════════════
        // AŞAMA 3: Trendyol Arama (En önemli - Türk ürünleri)
        // ══════════════════════════════════════════════════════
        try {
            val trendyolResult = trendyolSearchApi.searchBarkod(barkod)
            if (trendyolResult != null) {
                val sonuc = trendyolResult.copy(kaynak = "trendyol")
                saveBarkodOnbellek(sonuc)
                return@withContext Result.success(sonuc)
            }
        } catch (e: Exception) {
            // Hata oldu, devam et
        }

        // ══════════════════════════════════════════════════════
        // AŞAMA 4: Google Shopping Search
        // ══════════════════════════════════════════════════════
        try {
            val googleResult = searchWithGoogle(barkod)
            if (googleResult != null) {
                val sonuc = googleResult.copy(kaynak = "google_shopping")
                saveBarkodOnbellek(sonuc)
                return@withContext Result.success(sonuc)
            }
        } catch (e: Exception) {
            // Hata oldu, devam et
        }

        // ══════════════════════════════════════════════════════
        // AŞAMA 5: Manuel giriş gerekiyor
        // ══════════════════════════════════════════════════════
        Result.failure(BarkodBulunamadiException(barkod))
    }

    private suspend fun searchWithGoogle(barkod: String): BarkodSonuc? {
        // Google Custom Search API entegrasyonu
        // NOT: Ücretsiz 100 istek/gün limiti var
        // API key olmadan sadece web scraping yapılabilir
        // Şimdilik basit bir fallback olarak null döndür
        return null
    }

    override suspend fun getBarkodOnbellek(barkod: String): BarkodSonuc? {
        return barkodOnbellekDao.getByBarkod(barkod)?.let { entity ->
            BarkodSonuc(
                barkod = entity.barkod,
                marka = entity.marka,
                model = entity.model,
                tur = entity.tur,
                beden = entity.beden,
                renk = entity.renk,
                imageUrl = entity.imageUrl,
                kaynak = "yerel_bellegim"
            )
        }
    }

    override suspend fun saveBarkodOnbellek(sonuc: BarkodSonuc) {
        barkodOnbellekDao.insert(
            BarkodOnbellekEntity(
                barkod = sonuc.barkod,
                marka = sonuc.marka,
                model = sonuc.model,
                tur = sonuc.tur,
                beden = sonuc.beden,
                renk = sonuc.renk,
                imageUrl = sonuc.imageUrl
            )
        )
    }

    override suspend fun deleteBarkodOnbellek(barkod: String) {
        barkodOnbellekDao.deleteByBarkod(barkod)
    }

    class BarkodBulunamadiException(val barkod: String) : Exception(
        "Barkod bulunamadı: $barkod. Lütfen ürün bilgilerini manuel olarak girin."
    )
}
