package com.cyberqbit.ceptekabin.data.service

import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LC Waikiki, Koton, Mango vb. markaların DPP QR kodlarından ürün bilgisi çeker.
 * Örnek: https://dpp.lcwaikiki.com/86846907999431837936
 */
@Singleton
class DppUrlService @Inject constructor(private val okHttpClient: OkHttpClient) {

    companion object {
        val DESTEKLENEN_DOMAINLER = listOf(
            "dpp.lcwaikiki.com", "dpp.koton.com", "dpp.mango.com",
            "product.zara.com", "dpp.hm.com", "product.defacto.com.tr"
        )
        private val SAYI_URL_PATTERN = Regex("https?://[^/]+/[0-9]{10,}.*")

        fun isDppUrl(url: String): Boolean =
            url.startsWith("https://") &&
            (DESTEKLENEN_DOMAINLER.any { url.contains(it) } || url.matches(SAYI_URL_PATTERN))
    }

    suspend fun urunBilgisiCek(url: String): Result<BarkodSonuc> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:120.0)")
                .build()
            val html = okHttpClient.newCall(request).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.failure(Exception("HTTP ${res.code}"))
                res.body?.string() ?: return@withContext Result.failure(Exception("Boş yanıt"))
            }

            val doc      = Jsoup.parse(html, url)
            val ogTitle  = doc.select("meta[property=og:title]").attr("content").trim()
            val ogImage  = doc.select("meta[property=og:image]").attr("content").trim()
            val ogDesc   = doc.select("meta[property=og:description]").attr("content").trim()

            val marka = resolveMarka(url, doc)

            // JSON-LD öncelikli, yoksa fallback
            val jsonLd = parseJsonLd(doc, marka)
            val sonuc = BarkodSonuc(
                barkod    = url,
                marka     = jsonLd?.marka    ?: marka,
                isim      = jsonLd?.isim     ?: ogTitle,
                tur       = jsonLd?.tur      ?: inferTur(ogTitle),
                renk      = jsonLd?.renk     ?: extractColor(ogDesc + " " + ogTitle),
                gorselUrl = jsonLd?.gorselUrl ?: ogImage.takeIf { it.isNotBlank() },
                kaynak    = "dpp"
            )
            Result.success(sonuc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── JSON-LD ──────────────────────────────────────────────────────────────
    private fun parseJsonLd(doc: org.jsoup.nodes.Document, fallbackMarka: String): BarkodSonuc? {
        for (el in doc.select("script[type=application/ld+json]")) {
            runCatching {
                val obj = JSONObject(el.data())
                if (obj.optString("@type") != "Product") return@runCatching null
                val imgRaw = obj.opt("image")
                val img = when (imgRaw) {
                    is String    -> imgRaw
                    is JSONArray -> imgRaw.optString(0)
                    else -> ""
                }
                BarkodSonuc(
                    barkod    = "",
                    marka     = obj.optJSONObject("brand")?.optString("name")
                                   ?.ifBlank { fallbackMarka } ?: fallbackMarka,
                    isim      = obj.optString("name"),
                    tur       = inferTur(obj.optString("name")),
                    renk      = obj.optString("color").ifBlank { null },
                    gorselUrl = img.ifBlank { null },
                    kaynak    = "dpp_jsonld"
                )
            }.getOrNull()?.let { return it }
        }
        return null
    }

    // ── Yardımcılar ──────────────────────────────────────────────────────────
    private fun resolveMarka(url: String, doc: org.jsoup.nodes.Document): String = when {
        url.contains("lcwaikiki") -> "LC Waikiki"
        url.contains("koton")     -> "Koton"
        url.contains("mango")     -> "Mango"
        url.contains("zara")      -> "Zara"
        url.contains("defacto")   -> "DeFacto"
        url.contains("hm.com")    -> "H&M"
        else -> doc.select("meta[property=og:site_name]").attr("content")
                    .ifBlank { doc.title().split("|", "-").lastOrNull()?.trim() ?: "" }
    }

    private fun extractColor(text: String): String? {
        val low = text.lowercase()
        return mapOf(
            "siyah" to "Siyah", "beyaz" to "Beyaz", "kırmızı" to "Kırmızı",
            "mavi" to "Mavi", "lacivert" to "Lacivert", "yeşil" to "Yeşil",
            "sarı" to "Sarı", "pembe" to "Pembe", "mor" to "Mor",
            "bej" to "Bej", "kahve" to "Kahverengi", "gri" to "Gri",
            "black" to "Siyah", "white" to "Beyaz", "red" to "Kırmızı",
            "blue" to "Mavi", "green" to "Yeşil", "navy" to "Lacivert",
            "grey" to "Gri", "gray" to "Gri", "pink" to "Pembe",
            "beige" to "Bej", "brown" to "Kahverengi"
        ).entries.firstOrNull { low.contains(it.key) }?.value
    }

    private fun inferTur(ad: String): String {
        val a = ad.lowercase()
        return when {
            a.contains("tişört") || a.contains("t-shirt") -> "Tişört"
            a.contains("gömlek") || a.contains("shirt")   -> "Gömlek"
            a.contains("kot") || a.contains("jean")       -> "Kot Pantolon"
            a.contains("pantolon") || a.contains("pant")  -> "Pantolon"
            a.contains("etek") || a.contains("skirt")     -> "Etek"
            a.contains("elbise") || a.contains("dress")   -> "Elbise"
            a.contains("ceket") || a.contains("jacket")   -> "Ceket"
            a.contains("mont") || a.contains("coat")      -> "Mont"
            a.contains("kazak") || a.contains("sweater")  -> "Kazak"
            a.contains("hırka") || a.contains("cardigan") -> "Hırka"
            a.contains("sweatshirt") || a.contains("hoodie") -> "Sweatshirt"
            a.contains("şort") || a.contains("short")     -> "Şort"
            a.contains("çanta") || a.contains("bag")      -> "Çanta"
            else -> "Diğer"
        }
    }
}
