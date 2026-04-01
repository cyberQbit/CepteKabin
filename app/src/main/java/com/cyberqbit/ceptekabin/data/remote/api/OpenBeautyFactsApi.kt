package com.cyberqbit.ceptekabin.data.remote.api

import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class OpenBeautyFactsApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://world.openbeautyfacts.org/api/v2/product/"
    }

    suspend fun searchBarkod(barkod: String): BarkodSonuc? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL$barkod.json")
                .get()
                .header("User-Agent", "CepteKabin/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val status = json.optInt("status", 0)

            if (status != 1) return@withContext null

            val product = json.optJSONObject("product") ?: return@withContext null
            val brands = product.optString("brands", "")
            val productName = product.optString("product_name", "")
            val categories = product.optString("categories", "")
            val imageUrl = if (product.has("image_url")) product.optString("image_url", null) else null

            val tur = parseCategory(categories)
            val renk = extractColor(product)

            BarkodSonuc(
                barkod = barkod,
                marka = brands.split(",").firstOrNull()?.trim() ?: "",
                model = productName,
                tur = tur,
                beden = null,
                renk = renk,
                imageUrl = imageUrl,
                kaynak = "openbeautyfacts"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCategory(categories: String): String? {
        val lowerCategories = categories.lowercase()
        return when {
            lowerCategories.contains("tshirt") || lowerCategories.contains("t-shirt") || lowerCategories.contains("tişört") || lowerCategories.contains("tisort") -> "TISORT"
            lowerCategories.contains("shirt") || lowerCategories.contains("gömlek") -> "GOMLEK"
            lowerCategories.contains("pants") || lowerCategories.contains("pantolon") -> "PANTOLON"
            lowerCategories.contains("jacket") || lowerCategories.contains("ceket") -> "CEKET"
            lowerCategories.contains("dress") || lowerCategories.contains("elbise") -> "ELBISE"
            lowerCategories.contains("shoe") || lowerCategories.contains("ayakkabı") -> "AYAKKABI"
            lowerCategories.contains("skirt") || lowerCategories.contains("etek") -> "ETEK"
            lowerCategories.contains("hat") || lowerCategories.contains("şapka") || lowerCategories.contains("sapka") -> "SAPKA"
            else -> null
        }
    }

    private fun extractColor(product: JSONObject): String? {
        val tags = product.optJSONArray("tags") ?: return null
        for (i in 0 until tags.length()) {
            val tag = tags.getString(i)?.lowercase() ?: continue
            if (tag.contains("color:")) {
                return tag.removePrefix("color:").uppercase()
            }
        }
        return null
    }
}
