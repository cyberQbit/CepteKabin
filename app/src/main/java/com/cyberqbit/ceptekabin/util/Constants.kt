package com.cyberqbit.ceptekabin.util

object Constants {
    const val APP_NAME = "CepteKabin"
    const val DATABASE_NAME = "ceptekabin_database"

    const val HAVA_DURUMU_BASE_URL = "https://mooweather-api.onrender.com/api/weather/"
    const val UPC_ITEM_DB_URL = "https://api.upcitemdb.com/pup/trial/lookup?upc="
    const val OPEN_BEAUTY_FACTS_URL = "https://world.openbeautyfacts.org/api/v2/product/"
    const val GITHUB_RELEASES_URL = "https://api.github.com/repos/cyberQbit/CepteKabin/releases/latest"

    const val PREFS_NAME = "ceptekabin_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_NAME = "user_name"
    const val PREF_LAST_CITY = "last_city"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_DISMISSED_UPDATE_VERSION = "dismissed_update_version"
    const val PREF_MANUAL_CITY = "manual_city"
    const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
    const val PREF_TUTORIAL_SHOWN = "tutorial_shown"

    const val EXTRA_BARKOD = "extra_barkod"
    const val EXTRA_KIYAKET_ID = "extra_kiyaket_id"
    const val EXTRA_KOMBIN_ID = "extra_kombin_id"

    // ── Ana 5 kategori (Dolap sekmesiyle birebir eşleşir) ─────────────────────
    val ANA_KATEGORILER = listOf("Üst Giyim", "Alt Giyim", "Dış Giyim", "Ayakkabı", "Aksesuar")

    // ── Kategoriye göre Tür listesi ───────────────────────────────────────────
    val KATEGORI_TURLERI = mapOf(
        "Üst Giyim"  to listOf(
            "Tişört", "Gömlek", "Polo", "Bluz", "Kazak", "Hırka",
            "Sweatshirt", "Crop Top", "Tank Top", "Elbise", "Atlet", "Diğer"
        ),
        "Alt Giyim"  to listOf(
            "Pantolon", "Kot Pantolon", "Şort", "Etek", "Eşofman Altı",
            "Tayt", "Jogger", "Chino", "Bermuda", "Diğer"
        ),
        "Dış Giyim"  to listOf(
            "Ceket", "Blazer", "Kaban", "Mont", "Parka",
            "Trençkot", "Yağmurluk", "Deri Ceket", "Bomber", "Diğer"
        ),
        "Ayakkabı"   to listOf(
            "Spor Ayakkabı", "Klasik Ayakkabı", "Sneaker", "Loafer",
            "Bot", "Çizme", "Sandalet", "Terlik", "Oxford", "Mokasen", "Diğer"
        ),
        "Aksesuar"   to listOf(
            "Çanta", "Sırt Çantası", "El Çantası", "Şapka", "Bere",
            "Eşarp", "Kravat", "Papyon", "Kemer", "Kolye", "Bileklik",
            "Gözlük", "Çorap", "Eldiven", "Diğer"
        )
    )

    // ── Aksesuar & beden gerektirmeyen türler ─────────────────────────────────
    val BEDEN_GEREKTIRMEYEN_KATEGORILER = setOf("Aksesuar")

    // ── Kategoriye göre beden listesi ─────────────────────────────────────────
    val UST_GIYIM_BEDENLERI = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL", "4XL", "Standart")
    val ALT_GIYIM_BEDENLERI = listOf(
        "26", "28", "29", "30", "31", "32", "33", "34",
        "36", "38", "40", "42", "44", "46", "48", "50",
        "XS", "S", "M", "L", "XL", "XXL", "Standart"
    )
    val DIS_GIYIM_BEDENLERI = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL", "Standart")
    val AYAKKABI_NUMARALARI  = listOf("Standart") + (16..47).map { it.toString() }

    fun getBedenListesi(kategori: String): List<String>? = when (kategori) {
        "Üst Giyim"  -> UST_GIYIM_BEDENLERI
        "Alt Giyim"  -> ALT_GIYIM_BEDENLERI
        "Dış Giyim"  -> DIS_GIYIM_BEDENLERI
        "Ayakkabı"   -> AYAKKABI_NUMARALARI
        "Aksesuar"   -> null  // null = beden alanını gizle
        else         -> UST_GIYIM_BEDENLERI
    }

    // ── Marka listesi ─────────────────────────────────────────────────────────
    val MARKALAR = listOf(
        "Adidas", "Avva", "Beymen", "Boyner", "Colin's", "Columbia", "Dagi", "DeFacto",
        "Derimod", "H&M", "İpekyol", "Kiğılı", "Koton", "LC Waikiki", "Levi's", "Lufian",
        "Mango", "Mavi", "Network", "Nike", "Polo Ralph Lauren", "Pull&Bear", "Puma",
        "Sarar", "Under Armour", "Vakko", "Yargıcı", "Zara"
    ).sorted() + listOf("Diğer")

    // ── Renk listesi ──────────────────────────────────────────────────────────
    val RENKLER = listOf(
        "Siyah", "Beyaz", "Gri", "Lacivert", "Mavi", "Kırmızı", "Bordo", "Pembe",
        "Mor", "Yeşil", "Haki", "Sarı", "Turuncu", "Kahverengi", "Bej", "Krem",
        "Ekru", "Hardal", "Petrol", "Çoklu / Desenli", "Diğer"
    )

    // ── Geriye uyumluluk (eski kod referansları için) ──────────────────────────
    val GENEL_BEDENLER          = UST_GIYIM_BEDENLERI
    val PANTOLON_BEDENLERI      = ALT_GIYIM_BEDENLERI
    val KADIN_AYAKKABI_NUMARALARI  = AYAKKABI_NUMARALARI
    val ERKEK_AYAKKABI_NUMARALARI  = AYAKKABI_NUMARALARI
    val COCUK_AYAKKABI_NUMARALARI  = AYAKKABI_NUMARALARI
    val KIYAFET_TURLERI = KATEGORI_TURLERI.values.flatten().distinct().sorted() + listOf("Diğer")

    fun getGecerliTurler(kategori: String): List<String> =
        KATEGORI_TURLERI[kategori] ?: KIYAFET_TURLERI

    val KATEGORI_ILISKILERI = KATEGORI_TURLERI
}
