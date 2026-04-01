package com.cyberqbit.ceptekabin.data.remote.api

import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

/**
 * Ürün referans kodlarını (örn: W2GL42Z8-CVL, S4AQ73Z8-GNH) kullanarak
 * Trendyol üzerinden ürün bilgisi arar.
 */
class UrunKoduSearchService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TRENDYOL_SEARCH_URL =
            "https://apigw.trendyol.com/discovery-web-searchgw-service/api/search/v2"
    }

    /**
     * Verilen ürün kodunu arar ve bulunursa BarkodSonuc döner, bulunamazsa null döner.
     */
    suspend fun urunKoduAra(urunKodu: String): BarkodSonuc? = withContext(Dispatchers.IO) {
        // Önce kodu doğrudan ara
        searchTrendyol(urunKodu, urunKodu)
            ?: searchTrendyol("$urunKodu kıyafet", urunKodu)
    }

    private fun searchTrendyol(query: String, originalKod: String): BarkodSonuc? {
        return try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "$TRENDYOL_SEARCH_URL?q=$encodedQuery&culture=tr-TR" +
                "&tyclid=0&operationName=SearchProducts&marketingFleet=local_presentation_1_0_0"

            val request = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .header("Referer", "https://www.trendyol.com/")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            parseTrendyolSonuc(body, originalKod)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTrendyolSonuc(jsonString: String, urunKodu: String): BarkodSonuc? {
        return try {
            val json = JSONObject(jsonString)
            val products = json.optJSONArray("products") ?: return null
            if (products.length() == 0) return null

            val product = products.getJSONObject(0)

            val urunAdi = product.optString("name", "").ifBlank { null }
            val marka = product.optString("brandName", "").ifBlank { null }
            val kategori = product.optString("categoryName", "")
            val imageUrl = product.optJSONArray("images")?.optString(0)

            val (tur, renk) = parseUrunBilgisi(urunAdi ?: "", kategori)

            BarkodSonuc(
                barkod = urunKodu,
                marka = marka,
                model = urunAdi,
                tur = tur,
                beden = null,
                renk = renk,
                imageUrl = imageUrl,
                kaynak = "urun_kodu"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseUrunBilgisi(urunAdi: String, kategori: String): Pair<String?, String?> {
        val ad = urunAdi.lowercase()
        val kat = kategori.lowercase()

        val tur = when {
            ad.contains("tişört") || ad.contains("tshirt") || ad.contains("t-shirt") -> "TISORT"
            ad.contains("gömlek") || ad.contains("shirt")                             -> "GOMLEK"
            ad.contains("pantolon") || ad.contains("jean") || ad.contains("denim")   -> "PANTOLON"
            ad.contains("etek") || ad.contains("skirt")                               -> "ETEK"
            ad.contains("şort") || ad.contains("short")                               -> "SORT"
            ad.contains("ceket") || ad.contains("blazer")                             -> "CEKET"
            ad.contains("kaban") || ad.contains("coat")                               -> "KABAN"
            ad.contains("mont") || ad.contains("jacket")                              -> "MONTO"
            ad.contains("elbise") || ad.contains("dress")                             -> "ELBISE"
            ad.contains("ayakkabı") || ad.contains("shoe") || ad.contains("sneaker") -> "AYAKKABI"
            ad.contains("bot") || ad.contains("boot")                                 -> "BOT"
            ad.contains("sandalet") || ad.contains("terlik")                          -> "TERLIK"
            ad.contains("şapka") || ad.contains("hat") || ad.contains("cap")         -> "SAPKA"
            ad.contains("çanta") || ad.contains("bag")                               -> "CANTA"
            ad.contains("eşarp") || ad.contains("scarf")                             -> "ESARP"
            ad.contains("çorap") || ad.contains("sock")                              -> "CORAP"
            kat.contains("tişört")   -> "TISORT"
            kat.contains("pantolon") -> "PANTOLON"
            kat.contains("ayakkabı") -> "AYAKKABI"
            kat.contains("elbise")   -> "ELBISE"
            else -> null
        }

        val renk = when {
            ad.contains("siyah") || ad.contains("black")                             -> "Siyah"
            ad.contains("beyaz") || ad.contains("white")                             -> "Beyaz"
            ad.contains("gri") || ad.contains("grey") || ad.contains("gray")        -> "Gri"
            ad.contains("kırmızı") || ad.contains("red") || ad.contains("bordo")    -> "Kırmızı"
            ad.contains("mavi") || ad.contains("lacivert") || ad.contains("navy")   -> "Mavi"
            ad.contains("yeşil") || ad.contains("green") || ad.contains("haki")     -> "Yeşil"
            ad.contains("sarı") || ad.contains("yellow") || ad.contains("hardal")   -> "Sarı"
            ad.contains("turuncu") || ad.contains("orange")                          -> "Turuncu"
            ad.contains("mor") || ad.contains("purple") || ad.contains("lila")      -> "Mor"
            ad.contains("pembe") || ad.contains("pink")                              -> "Pembe"
            ad.contains("kahverengi") || ad.contains("brown") || ad.contains("bej") -> "Bej"
            ad.contains("krem") || ad.contains("cream") || ad.contains("ekru")      -> "Krem"
            else -> null
        }

        return Pair(tur, renk)
    }
}
