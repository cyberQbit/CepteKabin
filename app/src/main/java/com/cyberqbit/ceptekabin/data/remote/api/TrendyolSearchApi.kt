package com.cyberqbit.ceptekabin.data.remote.api

import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class TrendyolSearchApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        // Trendyol'un public arama endpoint'i
        private const val BASE_URL = "https://apigw.trendyol.com/discovery-web-searchgw-service/api/search/v2"
    }

    suspend fun searchBarkod(barkod: String): BarkodSonuc? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL?q=$barkod&culture=tr-TR&tyclid=0&operationName=SearchProducts&marketingFleet=local_presentation_1_0_0")
                .get()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            parseTrendyolResponse(body, barkod)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTrendyolResponse(jsonString: String, barkod: String): BarkodSonuc? {
        return try {
            val json = JSONObject(jsonString)
            val products = json.optJSONArray("products") ?: return null

            if (products.length() == 0) return null

            // İlk ürünü al
            val product = products.getJSONObject(0)

            val name = product.optString("name", "")
            val brand = product.optString("brandName", "")
            val imageUrl = product.optJSONArray("images")?.optString(0)
            val categoryName = product.optString("categoryName", "")

            // Ürün adından tür ve renk çıkar
            val (tur, renk) = parseProductInfo(name, categoryName)

            BarkodSonuc(
                barkod = barkod,
                marka = brand,
                model = name,
                tur = tur,
                beden = null,
                renk = renk,
                imageUrl = imageUrl,
                kaynak = "trendyol"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseProductInfo(name: String, category: String): Pair<String?, String?> {
        val lowerName = name.lowercase()
        val lowerCategory = category.lowercase()

        // Tür çıkarma
        val tur = when {
            lowerName.contains("tişört") || lowerName.contains("tshirt") || lowerName.contains("t-shirt") -> "TISORT"
            lowerName.contains("gömlek") || lowerName.contains("shirt") -> "GOMLEK"
            lowerName.contains("pantolon") || lowerName.contains("pants") || lowerName.contains("jean") -> "PANTOLON"
            lowerName.contains("etek") || lowerName.contains("skirt") -> "ETEK"
            lowerName.contains("şort") || lowerName.contains("sort") -> "SORT"
            lowerName.contains("ceket") || lowerName.contains("blazer") -> "CEKET"
            lowerName.contains("kaban") || lowerName.contains("coat") -> "KABAN"
            lowerName.contains("mont") || lowerName.contains("jacket") -> "MONTO"
            lowerName.contains("elbise") || lowerName.contains("dress") -> "ELBISE"
            lowerName.contains("ayakkabı") || lowerName.contains("shoe") || lowerName.contains("spor") -> "AYAKKABI"
            lowerName.contains("bot") -> "BOT"
            lowerName.contains("terlik") || lowerName.contains("sandaly") || lowerName.contains("sandalet") -> "TERLIK"
            lowerName.contains("şapka") || lowerName.contains("sapka") || lowerName.contains("hat") -> "SAPKA"
            lowerName.contains("çanta") || lowerName.contains("cant") || lowerName.contains("bag") -> "CANTA"
            lowerName.contains("eşarp") || lowerName.contains("esarp") || lowerName.contains("scarf") -> "ESARP"
            lowerName.contains("takı") || lowerName.contains("jewelry") -> "TAKI"
            lowerName.contains("çorap") || lowerName.contains("corap") || lowerName.contains("sock") -> "CORAP"
            lowerCategory.contains("tişört") -> "TISORT"
            lowerCategory.contains("pantolon") -> "PANTOLON"
            lowerCategory.contains("ayakkabı") -> "AYAKKABI"
            else -> null
        }

        // Renk çıkarma
        val renk = when {
            lowerName.contains("siyah") || lowerName.contains("black") -> "SIYAH"
            lowerName.contains("beyaz") || lowerName.contains("white") -> "BEYAZ"
            lowerName.contains("gri") || lowerName.contains("grey") || lowerName.contains("gray") -> "GRI"
            lowerName.contains("kırmızı") || lowerName.contains("red") || lowerName.contains("bordo") -> "KIRMIZI"
            lowerName.contains("mavi") || lowerName.contains("lacivert") || lowerName.contains("blue") || lowerName.contains("navy") -> "MAVI"
            lowerName.contains("yeşil") || lowerName.contains("green") || lowerName.contains("haki") -> "YESIL"
            lowerName.contains("sarı") || lowerName.contains("yellow") || lowerName.contains("hardal") -> "SARI"
            lowerName.contains("turuncu") || lowerName.contains("orange") -> "TURUNCU"
            lowerName.contains("mor") || lowerName.contains("purple") || lowerName.contains("lila") -> "MOR"
            lowerName.contains("pembe") || lowerName.contains("pink") -> "PEMBE"
            lowerName.contains("kahverengi") || lowerName.contains("brown") || lowerName.contains("bej") -> "BEJ"
            lowerName.contains("krem") || lowerName.contains("cream") || lowerName.contains("ekru") -> "KREM"
            else -> null
        }

        return Pair(tur, renk)
    }
}
