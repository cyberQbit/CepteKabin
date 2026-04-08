package com.cyberqbit.ceptekabin.domain.engine

import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket

object SmartKombinSuggester {

    data class KombinOnerisi(
        val ustGiyim: Kiyaket?,
        val altGiyim: Kiyaket?,
        val disGiyim: Kiyaket?,
        val ayakkabi: Kiyaket?,
        val aksesuar: Kiyaket?,
        val puanDetay: PuanDetay,
        val aciklama: String
    )

    data class PuanDetay(
        val renkUyumu: Int,
        val havaUyumu: Int,
        val toplamPuan: Int
    )

    private val SICAK_HAVA_UST = setOf("Tişört", "Atlet", "Tank Top", "Polo", "Bluz", "Crop Top")
    private val ILIK_HAVA_UST = setOf("Tişört", "Gömlek", "Polo", "Bluz")
    private val SERIN_HAVA_UST = setOf("Sweatshirt", "Kazak", "Hırka", "Gömlek")
    private val SOGUK_HAVA_UST = setOf("Kazak", "Sweatshirt", "Hırka")

    private val SICAK_HAVA_ALT = setOf("Şort", "Etek", "Bermuda", "Chino")
    private val ILIK_HAVA_ALT = setOf("Pantolon", "Kot Pantolon", "Chino", "Etek", "Jogger")
    private val SOGUK_HAVA_ALT = setOf("Pantolon", "Kot Pantolon", "Eşofman Altı", "Jogger")

    private val YAGMUR_DIS = setOf("Yağmurluk", "Trençkot")
    private val SERIN_DIS = setOf("Ceket", "Blazer", "Bomber", "Hırka", "Deri Ceket")
    private val SOGUK_DIS = setOf("Mont", "Kaban", "Parka", "Trençkot")

    private val SICAK_AYAKKABI = setOf("Sandalet", "Terlik", "Sneaker", "Spor Ayakkabı")
    private val NORMAL_AYAKKABI = setOf("Sneaker", "Spor Ayakkabı", "Loafer", "Oxford", "Mokasen")
    private val SOGUK_AYAKKABI = setOf("Bot", "Çizme", "Spor Ayakkabı")

    fun onerilerUret(
        kiyafetler: List<Kiyaket>,
        havaDurumu: HavaDurumu,
        maxOneri: Int = 3
    ): List<KombinOnerisi> {
        if (kiyafetler.isEmpty()) return emptyList()

        val weatherRec = WeatherOutfitEngine.getRecommendation(havaDurumu)
        val kategori = weatherRec.oncelikSeviyesi

        val ustler = kiyafetler.filter { it.isUstGiyim() && it.turHavayaUygun(kategori, "ust") }
        val altlar = kiyafetler.filter { it.isAltGiyim() && it.turHavayaUygun(kategori, "alt") }
        val disler = kiyafetler.filter { it.isDisGiyim() && it.turHavayaUygun(kategori, "dis") }
        val ayaklar = kiyafetler.filter { it.isAyakkabi() && it.turHavayaUygun(kategori, "ayak") }
        val aksesuarlar = kiyafetler.filter { it.isAksesuar() }

        val uygunUstler = ustler.ifEmpty { kiyafetler.filter { it.isUstGiyim() } }
        val uygunAltlar = altlar.ifEmpty { kiyafetler.filter { it.isAltGiyim() } }
        val uygunDisler = if (weatherRec.katmanSayisi >= 2) {
            disler.ifEmpty { kiyafetler.filter { it.isDisGiyim() } }
        } else emptyList()
        val uygunAyaklar = ayaklar.ifEmpty { kiyafetler.filter { it.isAyakkabi() } }

        val kombinasyonlar = mutableListOf<KombinOnerisi>()

        for (ust in uygunUstler.take(5)) {
            for (alt in uygunAltlar.take(5)) {
                val dis = uygunDisler.maxByOrNull {
                    ColorHarmonyUtil.uyumPuani(ust.renk ?: "", it.renk ?: "")
                }
                val ayak = uygunAyaklar.maxByOrNull {
                    ColorHarmonyUtil.uyumPuani(alt.renk ?: "", it.renk ?: "")
                }
                val aksesuar = aksesuarlar.firstOrNull()

                val renkler = listOfNotNull(ust.renk, alt.renk, dis?.renk, ayak?.renk)
                    .filter { it.isNotBlank() }
                val renkUyumu = ColorHarmonyUtil.toplamUyumPuani(renkler)
                val havaUyumu = hesaplaHavaUyumu(ust, alt, dis, ayak, kategori)
                val toplamPuan = (renkUyumu * 0.4 + havaUyumu * 0.6).toInt()

                kombinasyonlar.add(
                    KombinOnerisi(
                        ustGiyim = ust, altGiyim = alt, disGiyim = dis,
                        ayakkabi = ayak, aksesuar = aksesuar,
                        puanDetay = PuanDetay(renkUyumu, havaUyumu, toplamPuan),
                        aciklama = weatherRec.kisaAciklama
                    )
                )
            }
        }

        return kombinasyonlar
            .sortedByDescending { it.puanDetay.toplamPuan }
            .distinctBy { listOf(it.ustGiyim?.id, it.altGiyim?.id) }
            .take(maxOneri)
    }

    private fun hesaplaHavaUyumu(
        ust: Kiyaket, alt: Kiyaket, dis: Kiyaket?, ayak: Kiyaket?, seviye: String
    ): Int {
        var puan = 50
        if (ust.turHavayaUygun(seviye, "ust")) puan += 20
        if (alt.turHavayaUygun(seviye, "alt")) puan += 15
        if (ayak?.turHavayaUygun(seviye, "ayak") != false) puan += 15
        return puan.coerceIn(0, 100)
    }

    private fun Kiyaket.turHavayaUygun(seviye: String, slot: String): Boolean {
        val turAdi = this.tur.displayName
        return when (slot) {
            "ust" -> when (seviye) {
                "hafif" -> turAdi in SICAK_HAVA_UST || turAdi in ILIK_HAVA_UST
                "orta" -> turAdi in SERIN_HAVA_UST
                "ağır" -> turAdi in SOGUK_HAVA_UST
                else -> true
            }
            "alt" -> when (seviye) {
                "hafif" -> turAdi in SICAK_HAVA_ALT || turAdi in ILIK_HAVA_ALT
                "orta", "ağır" -> turAdi in SOGUK_HAVA_ALT
                else -> true
            }
            "dis" -> when (seviye) {
                "orta" -> turAdi in SERIN_DIS || turAdi in YAGMUR_DIS
                "ağır" -> turAdi in SOGUK_DIS || turAdi in YAGMUR_DIS
                else -> true
            }
            "ayak" -> when (seviye) {
                "hafif" -> turAdi in SICAK_AYAKKABI
                "orta" -> turAdi in NORMAL_AYAKKABI
                "ağır" -> turAdi in SOGUK_AYAKKABI
                else -> true
            }
            else -> true
        }
    }

    private fun Kiyaket.isUstGiyim(): Boolean = this.kategori == "Üst Giyim"
    private fun Kiyaket.isAltGiyim(): Boolean = this.kategori == "Alt Giyim"
    private fun Kiyaket.isDisGiyim(): Boolean = this.kategori == "Dış Giyim"
    private fun Kiyaket.isAyakkabi(): Boolean = this.kategori == "Ayakkabı"
    private fun Kiyaket.isAksesuar(): Boolean = this.kategori == "Aksesuar"
}
