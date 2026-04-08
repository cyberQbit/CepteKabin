package com.cyberqbit.ceptekabin.domain.engine

import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.HavaDurumuDurum
import com.cyberqbit.ceptekabin.domain.model.ForecastItem

object WeatherOutfitEngine {

    data class OutfitRecommendation(
        val ustGiyim: List<String>,
        val altGiyim: List<String>,
        val disGiyim: List<String>,
        val ayakkabi: List<String>,
        val aksesuar: List<String>,
        val katmanSayisi: Int,
        val oncelikSeviyesi: String,
        val kisaAciklama: String,
        val detayliAciklama: String,
        val konforEndeksi: Int
    )

    enum class HavaKategori {
        KAVURUCU, SICAK, ILIK, SERIN, SOGUK, COK_SOGUK, DONDURUCU
    }

    fun getRecommendation(havaDurumu: HavaDurumu): OutfitRecommendation {
        val sicaklik = havaDurumu.sicaklik
        val hissedilen = havaDurumu.hissedilenSicaklik
        val nem = havaDurumu.nemOrani
        val ruzgar = havaDurumu.ruzgarHizi
        val durum = havaDurumu.durum

        val efektifSicaklik = (sicaklik * 0.3 + hissedilen * 0.7)
        val kategori = sicaklikKategorisi(efektifSicaklik)
        val yagisVar = durum.isYagisli()
        val ruzgarSert = ruzgar > 20.0
        val nemliVeSicak = nem > 70 && sicaklik > 25

        val konforEndeksi = hesaplaKonforEndeksi(efektifSicaklik, nem, ruzgar, yagisVar)

        return when (kategori) {
            HavaKategori.KAVURUCU -> buildKavurucu(nemliVeSicak, yagisVar)
            HavaKategori.SICAK -> buildSicak(nemliVeSicak, yagisVar, ruzgarSert)
            HavaKategori.ILIK -> buildIlik(yagisVar, ruzgarSert, nem)
            HavaKategori.SERIN -> buildSerin(yagisVar, ruzgarSert, nem)
            HavaKategori.SOGUK -> buildSoguk(yagisVar, ruzgarSert)
            HavaKategori.COK_SOGUK -> buildCokSoguk(yagisVar, ruzgarSert)
            HavaKategori.DONDURUCU -> buildDondurucu(yagisVar)
        }.copy(konforEndeksi = konforEndeksi)
    }

    fun getRecommendationForForecast(forecast: ForecastItem, sehir: String = ""): OutfitRecommendation {
        val ortalamaSicaklik = (forecast.sicaklikMax + forecast.sicaklikMin) / 2.0
        val simuleHava = HavaDurumu(
            sehir = sehir, sehirId = sehir,
            sicaklik = ortalamaSicaklik,
            hissedilenSicaklik = ortalamaSicaklik - 2,
            durum = forecast.durum,
            aciklama = forecast.durum.displayName,
            nemOrani = forecast.nemOrani.takeIf { it > 0 } ?: 50,
            ruzgarHizi = forecast.ruzgarHizi.takeIf { it > 0.0 } ?: 10.0,
            gunBatimi = 0L, gunDogumu = 0L, guncelTarih = forecast.tarih
        )
        return getRecommendation(simuleHava)
    }

    private fun sicaklikKategorisi(efektif: Double): HavaKategori = when {
        efektif >= 35 -> HavaKategori.KAVURUCU
        efektif >= 28 -> HavaKategori.SICAK
        efektif >= 20 -> HavaKategori.ILIK
        efektif >= 12 -> HavaKategori.SERIN
        efektif >= 5  -> HavaKategori.SOGUK
        efektif >= 0  -> HavaKategori.COK_SOGUK
        else          -> HavaKategori.DONDURUCU
    }

    private fun hesaplaKonforEndeksi(
        sicaklik: Double, nem: Int, ruzgar: Double, yagisVar: Boolean
    ): Int {
        var skor = 100.0
        val sicaklikFark = Math.abs(sicaklik - 22.0)
        skor -= sicaklikFark * 2.5
        if (nem > 60) skor -= (nem - 60) * 0.4
        if (nem < 20) skor -= (20 - nem) * 0.3
        if (ruzgar > 10) skor -= (ruzgar - 10) * 1.0
        if (yagisVar) skor -= 15.0
        return skor.toInt().coerceIn(0, 100)
    }

    private fun buildKavurucu(nemli: Boolean, yagisVar: Boolean) = OutfitRecommendation(
        ustGiyim = listOf("Tişört", "Atlet", "Tank Top"),
        altGiyim = listOf("Şort", "Etek"),
        disGiyim = emptyList(),
        ayakkabi = listOf("Sandalet", "Terlik"),
        aksesuar = buildList {
            add("Güneş Gözlüğü"); add("Şapka")
            if (yagisVar) add("Şemsiye")
        },
        katmanSayisi = 1, oncelikSeviyesi = "hafif",
        kisaAciklama = if (nemli) "Hafif & nefes alan kıyafetler" else "Serin kalacak kıyafetler",
        detayliAciklama = buildString {
            append("Hava çok sıcak! Açık renkli, pamuklu ve bol kesim kıyafetler tercih edin. ")
            if (nemli) append("Nem çok yüksek, sentetik kumaşlardan kaçının. ")
            append("Bol su için ve güneş kremi sürmeyi unutmayın.")
        }, konforEndeksi = 0
    )

    private fun buildSicak(nemli: Boolean, yagisVar: Boolean, ruzgarSert: Boolean) = OutfitRecommendation(
        ustGiyim = listOf("Tişört", "Polo", "Bluz"),
        altGiyim = listOf("Şort", "Etek", "Chino"),
        disGiyim = if (yagisVar) listOf("Yağmurluk") else emptyList(),
        ayakkabi = listOf("Sneaker", "Sandalet", "Spor Ayakkabı"),
        aksesuar = buildList {
            add("Güneş Gözlüğü")
            if (yagisVar) add("Şemsiye")
            if (ruzgarSert) add("Şapka")
        },
        katmanSayisi = 1, oncelikSeviyesi = "hafif",
        kisaAciklama = "Tişört + şort günü" + if (yagisVar) " (şemsiye al!)" else "",
        detayliAciklama = buildString {
            append("Sıcak bir gün, hafif kıyafetler yeterli. ")
            if (yagisVar) append("Yağmur bekleniyor, yanınıza hafif bir yağmurluk veya şemsiye alın. ")
            if (nemli) append("Nem yüksek, pamuklu kıyafetler daha rahat olacaktır.")
        }, konforEndeksi = 0
    )

    private fun buildIlik(yagisVar: Boolean, ruzgarSert: Boolean, nem: Int) = OutfitRecommendation(
        ustGiyim = listOf("Tişört", "Gömlek", "Bluz"),
        altGiyim = listOf("Pantolon", "Chino", "Kot Pantolon"),
        disGiyim = buildList {
            if (ruzgarSert) add("Hırka")
            if (yagisVar) add("Yağmurluk")
        },
        ayakkabi = listOf("Sneaker", "Spor Ayakkabı", "Loafer"),
        aksesuar = buildList {
            if (yagisVar) add("Şemsiye")
            if (ruzgarSert) add("Eşarp")
        },
        katmanSayisi = if (ruzgarSert || yagisVar) 2 else 1, oncelikSeviyesi = "hafif",
        kisaAciklama = buildString {
            append("Gömlek + pantolon ideal")
            if (ruzgarSert) append(" (hırka ekle)")
        },
        detayliAciklama = buildString {
            append("Ilık bir gün, tek kat kıyafet genelde yeterli. ")
            if (ruzgarSert) append("Rüzgar sert esiyor, üstüne ince bir hırka atmanızı öneririz. ")
            if (yagisVar) append("Yağmur ihtimali var, yağmurluk veya şemsiye bulundurun. ")
            if (nem > 70) append("Nem yüksek, nefes alan kumaşlar tercih edin.")
        }, konforEndeksi = 0
    )

    private fun buildSerin(yagisVar: Boolean, ruzgarSert: Boolean, nem: Int) = OutfitRecommendation(
        ustGiyim = listOf("Sweatshirt", "Kazak", "Hırka"),
        altGiyim = listOf("Pantolon", "Kot Pantolon", "Jogger"),
        disGiyim = buildList {
            if (yagisVar) add("Su Geçirmez Ceket") else add("Ceket")
            if (ruzgarSert) add("Rüzgarlık")
        },
        ayakkabi = listOf("Spor Ayakkabı", "Bot", "Sneaker"),
        aksesuar = buildList {
            if (yagisVar) add("Şemsiye")
            if (ruzgarSert) add("Eşarp")
        },
        katmanSayisi = 2, oncelikSeviyesi = "orta",
        kisaAciklama = "Kazak + ceket günü",
        detayliAciklama = buildString {
            append("Hava serin, iki kat giyinmenizi tavsiye ederiz. ")
            append("Sabah ve akşam saatleri daha soğuk olabilir. ")
            if (yagisVar) append("Yağmur bekleniyor, su geçirmez bir dış giyim şart. ")
            if (ruzgarSert) append("Rüzgar güçlü, boynunuzu koruyan bir eşarp iyi olur.")
        }, konforEndeksi = 0
    )

    private fun buildSoguk(yagisVar: Boolean, ruzgarSert: Boolean) = OutfitRecommendation(
        ustGiyim = listOf("Kazak", "Kalın Sweatshirt"),
        altGiyim = listOf("Pantolon", "Kalın Pantolon"),
        disGiyim = buildList {
            if (yagisVar) add("Su Geçirmez Mont") else add("Mont")
            if (ruzgarSert) add("Kaban")
        },
        ayakkabi = listOf("Bot", "Çizme"),
        aksesuar = buildList {
            add("Atkı")
            if (ruzgarSert) add("Bere")
            if (yagisVar) add("Şemsiye")
        },
        katmanSayisi = 3, oncelikSeviyesi = "ağır",
        kisaAciklama = "Mont + kazak şart",
        detayliAciklama = buildString {
            append("Soğuk bir gün, üç katmanlı giyinmeniz gerekiyor: iç (termal/tişört), orta (kazak), dış (mont). ")
            if (yagisVar) append("Yağış var, su geçirmez mont tercih edin. ")
            if (ruzgarSert) append("Rüzgar şiddetli, bere ve atkı ile boynunuzu ve kulaklarınızı koruyun.")
        }, konforEndeksi = 0
    )

    private fun buildCokSoguk(yagisVar: Boolean, ruzgarSert: Boolean) = OutfitRecommendation(
        ustGiyim = listOf("Kalın Kazak", "Termal İçlik"),
        altGiyim = listOf("Kalın Pantolon", "Termal Tayt + Pantolon"),
        disGiyim = buildList {
            if (yagisVar) add("Su Geçirmez Kaban") else add("Kaban")
            add("Parka")
        },
        ayakkabi = listOf("Bot", "Çizme", "Su Geçirmez Bot"),
        aksesuar = listOf("Atkı", "Bere", "Eldiven"),
        katmanSayisi = 3, oncelikSeviyesi = "ağır",
        kisaAciklama = "Kaban + atkı + eldiven günü",
        detayliAciklama = buildString {
            append("Çok soğuk! Mutlaka termal iç çamaşır giyin. ")
            append("Üç kat: termal içlik + kalın kazak + kaban/parka. ")
            append("Eldiven, bere ve atkıyı unutmayın. ")
            if (yagisVar) append("Yağış bekleniyor, su geçirmez dış giyim kritik.")
        }, konforEndeksi = 0
    )

    private fun buildDondurucu(yagisVar: Boolean) = OutfitRecommendation(
        ustGiyim = listOf("Kalın Kazak", "Termal İçlik"),
        altGiyim = listOf("Termal Tayt + Kalın Pantolon"),
        disGiyim = listOf(if (yagisVar) "Su Geçirmez Kaban" else "Kaban", "Parka"),
        ayakkabi = listOf("Su Geçirmez Bot", "Çizme"),
        aksesuar = listOf("Atkı", "Bere", "Eldiven", "Yün Çorap"),
        katmanSayisi = 3, oncelikSeviyesi = "ağır",
        kisaAciklama = "Dondurucu soğuk! Tam teçhizat",
        detayliAciklama = "Dondurucu soğuk! Mümkünse dışarı çıkmayın. " +
            "Çıkmanız gerekiyorsa: termal içlik + kalın kazak + kaban, " +
            "eldiven, bere, atkı ve su geçirmez bot şart. Yüzünüzü de koruyun.",
        konforEndeksi = 0
    )

    private fun HavaDurumuDurum.isYagisli(): Boolean = when (this) {
        HavaDurumuDurum.YAGMURLU, HavaDurumuDurum.YAGIS_HAKLI,
        HavaDurumuDurum.KARLI, HavaDurumuDurum.FIRTINALI -> true
        else -> false
    }
}
