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
}
