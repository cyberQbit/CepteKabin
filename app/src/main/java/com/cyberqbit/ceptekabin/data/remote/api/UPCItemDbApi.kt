package com.cyberqbit.ceptekabin.data.remote.api

import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class UPCItemDbApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://api.upcitemdb.com/pup/trial/lookup?upc="
    }

    suspend fun searchBarkod(barkod: String): BarkodSonuc? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL$barkod")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val items = json.optJSONArray("items") ?: return@withContext null

            if (items.length() == 0) return@withContext null

            val item = items.getJSONObject(0)
            val title = item.optString("title", "")
            val brand = item.optString("brand", "")
            val images = item.optJSONArray("images")
            val imageUrl = if (images != null && images.length() > 0) images.getString(0) else null

            // Parse title to extract type and color
            val (tur, renk) = parseTitle(title)

            BarkodSonuc(
                barkod = barkod,
                marka = brand.ifBlank { extractBrand(title) },
                model = title,
                tur = tur,
                beden = null,
                renk = renk,
                imageUrl = imageUrl,
                kaynak = "upcitemdb"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTitle(title: String): Pair<String?, String?> {
        val lowerTitle = title.lowercase()
        val tur = when {
            lowerTitle.contains("tshirt") || lowerTitle.contains("t-shirt") || lowerTitle.contains("tişört") -> "TISORT"
            lowerTitle.contains("shirt") || lowerTitle.contains("gömlek") -> "GOMLEK"
            lowerTitle.contains("pants") || lowerTitle.contains("pantolon") -> "PANTOLON"
            lowerTitle.contains("jacket") || lowerTitle.contains("cektt") || lowerTitle.contains("ceket") -> "CEKET"
            lowerTitle.contains("dress") || lowerTitle.contains("elbise") -> "ELBISE"
            lowerTitle.contains("shoe") || lowerTitle.contains("ayakkabı") || lowerTitle.contains("spor") -> "AYAKKABI"
            else -> null
        }

        val renk = when {
            lowerTitle.contains("black") || lowerTitle.contains("siyah") -> "SIYAH"
            lowerTitle.contains("white") || lowerTitle.contains("beyaz") -> "BEYAZ"
            lowerTitle.contains("red") || lowerTitle.contains("kırmızı") -> "KIRMIZI"
            lowerTitle.contains("blue") || lowerTitle.contains("mavi") -> "MAVI"
            lowerTitle.contains("green") || lowerTitle.contains("yeşil") -> "YESIL"
            else -> null
        }

        return Pair(tur, renk)
    }

    private fun extractBrand(title: String): String {
        val words = title.split(" ")
        return words.firstOrNull { it.first().isUpperCase() && it.length > 2 } ?: ""
    }
}
