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
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class BarkodRepositoryImpl @Inject constructor(
    private val barkodOnbellekDao: BarkodOnbellekDao,
    private val sezonluUrunDao: SezonluUrunDao,
    private val okHttpClient: OkHttpClient
) : BarkodRepository {

    private val upcItemDbApi      = UPCItemDbApi(okHttpClient)
    private val openBeautyFactsApi = OpenBeautyFactsApi(okHttpClient)
    private val trendyolSearchApi  = TrendyolSearchApi(okHttpClient)

    override suspend fun barkodAra(barkod: String): Result<BarkodSonuc> = withContext(Dispatchers.IO) {

        // ── 1. Local cache (instant) ──────────────────────────────────────────
        getBarkodOnbellek(barkod)?.let {
            return@withContext Result.success(it.copy(kaynak = "yerel_bellegim"))
        }

        // ── 2. Open Food Facts (unlimited, good EAN-13 coverage) ─────────────
        tryApi("openfoodfacts") { openFoodFactsAra(barkod) }?.let {
            cache(it); return@withContext Result.success(it)
        }

        // ── 3. Open Beauty Facts (cosmetics/accessories) ─────────────────────
        tryApi("openbeautyfacts") { openBeautyFactsApi.searchBarkod(barkod) }?.let {
            cache(it); return@withContext Result.success(it)
        }

        // ── 4. UPC Item DB (international brands, 100 req/day free) ──────────
        tryApi("upcitemdb") { upcItemDbApi.searchBarkod(barkod) }?.let {
            cache(it); return@withContext Result.success(it)
        }

        // ── 5. Sezonlu ürün local DB ──────────────────────────────────────────
        sezonluUrunDao.getByBarkod(barkod)?.let { u ->
            val sonuc = BarkodSonuc(barkod, u.marka, u.model, u.tur, null, null, null,
                sezon = u.sezon, urunDurumu = UrunDurum.fromString(u.durum), kaynak = "sezonlu_urun")
            return@withContext Result.success(sonuc)
        }

        // ── 6. Trendyol search (Turkish market — most important) ──────────────
        tryApi("trendyol") { trendyolSearchApi.searchBarkod(barkod) }?.let {
            cache(it); return@withContext Result.success(it)
        }

        // ── 7. Barcode lookup via go-upc.com (another free fallback) ─────────
        tryApi("go_upc") { goUpcAra(barkod) }?.let {
            cache(it); return@withContext Result.success(it)
        }

        // ── 8. Manual entry required ──────────────────────────────────────────
        Result.failure(BarkodBulunamadiException(barkod))
    }

    // ── Open Food Facts ───────────────────────────────────────────────────────
    private fun openFoodFactsAra(barkod: String): BarkodSonuc? {
        return try {
            val req = Request.Builder()
                .url("https://world.openfoodfacts.org/api/v2/product/$barkod.json")
                .header("User-Agent", "CepteKabin/1.0 (android)")
                .build()
            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val json = JSONObject(body)
            if (json.optInt("status", 0) != 1) return null
            val product = json.optJSONObject("product") ?: return null
            val brand = product.optString("brands", "").split(",").firstOrNull()?.trim()
            val name = product.optString("product_name", "").ifBlank { null }
            val image = product.optString("image_front_url", "").ifBlank { null }
            if (brand.isNullOrBlank() && name.isNullOrBlank()) return null
            BarkodSonuc(barkod, brand, name, null, null, null, image, kaynak = "openfoodfacts")
        } catch (e: Exception) { null }
    }

    // ── Go-UPC.com free API ───────────────────────────────────────────────────
    private fun goUpcAra(barkod: String): BarkodSonuc? {
        return try {
            val req = Request.Builder()
                .url("https://go-upc.com/api/v1/code/$barkod")
                .header("User-Agent", "CepteKabin/1.0")
                .build()
            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val json = JSONObject(body)
            val product = json.optJSONObject("product") ?: return null
            val name = product.optString("name", "").ifBlank { null }
            val brand = product.optString("brand", "").ifBlank { null }
            val image = product.optString("imageUrl", "").ifBlank { null }
            if (name.isNullOrBlank() && brand.isNullOrBlank()) return null
            BarkodSonuc(barkod, brand, name, null, null, null, image, kaynak = "go_upc")
        } catch (e: Exception) { null }
    }

    private suspend fun <T> tryApi(tag: String, block: suspend () -> T?): T? {
        return try { block() } catch (e: Exception) { null }
    }

    private suspend fun cache(sonuc: BarkodSonuc) {
        try { saveBarkodOnbellek(sonuc) } catch (_: Exception) {}
    }

    override suspend fun getBarkodOnbellek(barkod: String): BarkodSonuc? {
        return barkodOnbellekDao.getByBarkod(barkod)?.let { e ->
            BarkodSonuc(e.barkod, e.marka, e.model, e.tur, e.beden, e.renk, e.imageUrl, kaynak = "yerel_bellegim")
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
