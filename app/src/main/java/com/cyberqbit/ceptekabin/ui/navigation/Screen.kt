package com.cyberqbit.ceptekabin.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Dolap : Screen("dolap")
    object Kombin : Screen("kombin")
    object HavaDurumu : Screen("havadurumu")
    object Tarama : Screen("tarama")
    object KiyaketEkle : Screen("kiyaket_ekle?barkod={barkod}&kiyaketId={kiyaketId}") {
        fun createRoute(barkod: String = "", kiyaketId: Long = 0L) = "kiyaket_ekle?barkod=$barkod&kiyaketId=$kiyaketId"
    }
    object KiyaketDetay : Screen("kiyaket_detay/{id}") {
        fun createRoute(id: Long) = "kiyaket_detay/$id"
    }
    object KombinDetay : Screen("kombin_detay/{id}") {
        fun createRoute(id: Long) = "kombin_detay/$id"
    }
    object KombinOlustur : Screen("kombin_olustur?kombinId={kombinId}") {
        fun createRoute(kombinId: Long = 0L) = "kombin_olustur?kombinId=$kombinId"
    }
    object KombinImport : Screen("kombin_import/{uri}") {
        fun createRoute(uri: String) = "kombin_import/$uri"
    }
    
    // V2 EKRANLARI (Kullanılmayanlar Silindi)
    object KombinTakvim : Screen("kombin_takvim")
    object VirtualTryOn : Screen("virtual_try_on")
}
