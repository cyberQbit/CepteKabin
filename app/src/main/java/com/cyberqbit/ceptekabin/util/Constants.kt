package com.cyberqbit.ceptekabin.util

object Constants {
    const val APP_NAME = "CepteKabin"
    const val DATABASE_NAME = "ceptekabin_database"

    // Hava Durumu API
    const val HAVA_DURUMU_BASE_URL = "https://mooweather-api.onrender.com/api/weather/"

    // Barcode APIs
    const val UPC_ITEM_DB_URL = "https://api.upcitemdb.com/pup/trial/lookup?upc="
    const val OPEN_BEAUTY_FACTS_URL = "https://world.openbeautyfacts.org/api/v2/product/"

    // SharedPreferences Keys
    const val PREFS_NAME = "ceptekabin_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_NAME = "user_name"
    const val PREF_LAST_CITY = "last_city"
    const val PREF_DARK_MODE = "dark_mode"

    // Intent Extras
    const val EXTRA_BARKOD = "extra_barkod"
    const val EXTRA_KIYAKET_ID = "extra_kiyaket_id"
    const val EXTRA_KOMBIN_ID = "extra_kombin_id"

    // --- MARKALAR (Alfabetik + Yeni 10 Türk Markası) ---
    val MARKALAR = listOf(
        "Adidas", "Avva", "Beymen", "Boyner", "Colin's", "Columbia", "Dagi", "DeFacto",
        "Derimod", "H&M", "İpekyol", "Kiğılı", "Koton", "LC Waikiki", "Levi's", "Lufian",
        "Mango", "Mavi", "Network", "Nike", "Polo Ralph Lauren", "Pull&Bear", "Puma",
        "Sarar", "Under Armour", "Vakko", "Yargıcı", "Zara"
    ).sorted() + "Diğer" // "Diğer" her zaman en sonda kalır

    // --- KIYAFET TÜRLERİ (Alfabetik + Ayakkabı Kategorileri Ayrıldı) ---
    val KIYAFET_TURLERI = listOf(
        "Ceket", "Çocuk Ayakkabısı", "Elbise", "Erkek Ayakkabısı", "Eşofman", "Etek",
        "Gömlek", "Hırka", "Kaban / Mont", "Kadın Ayakkabısı", "Kazak", "Kravat",
        "Pantolon", "Sweatshirt", "Şort", "Tişört"
    ).sorted() + "Diğer"

    // --- KATEGORİYE ÖZEL BEDEN/NUMARA LİSTELERİ ---
    val GENEL_BEDENLER = listOf("XXS", "XS", "S", "M", "L", "XL", "XXL", "3XL", "4XL", "Standart")
    
    val PANTOLON_BEDENLERI = listOf(
        "26", "28", "29", "30", "31", "32", "33", "34", "36", "38", "40", "42", "44", "46", "48", "50", "Standart"
    )
    
    // Kotlin'in aralık (range) özelliği ile numaraları otomatik üretiyoruz
    val KADIN_AYAKKABI_NUMARALARI = listOf("Standart") + (16..47).map { it.toString() }
    val ERKEK_AYAKKABI_NUMARALARI = listOf("Standart") + (16..47).map { it.toString() }
    val COCUK_AYAKKABI_NUMARALARI = listOf("Standart") + (16..47).map { it.toString() }

    // Kategori ve Tür Arasındaki Mantıksızlıkları Önleme
    val KATEGORI_ILISKILERI = mapOf(
        "Erkek Ayakkabısı" to listOf("Spor Ayakkabı", "Klasik Ayakkabı", "Bot", "Sandalet"),
        "Kadın Ayakkabısı" to listOf("Topuklu", "Spor Ayakkabı", "Bot", "Sandalet"),
        "Çocuk Ayakkabısı" to listOf("Spor Ayakkabı", "Bot", "Sandalet"),
        "Dış Giyim" to listOf("Kaban / Mont", "Kaban", "Mont", "Ceket", "Yağmurluk"),
        "Üst Giyim" to listOf("Tişört", "Gömlek", "Kazak", "Hırka", "Sweatshirt", "Sweat"),
        "Alt Giyim" to listOf("Pantolon", "Şort", "Etek", "Eşofman"),
        "Aksesuar" to listOf("Çanta", "Şapka", "Eşarp", "Takı", "Kravat"),
        "İç Giyim & Plaj" to listOf("İç Çamaşırı", "Çorap", "Plaj", "Terlik")
    )
    
    fun getGecerliTurler(secilenKategori: String): List<String> {
        return KATEGORI_ILISKILERI[secilenKategori] ?: KIYAFET_TURLERI
    }
}


