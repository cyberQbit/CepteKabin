package com.cyberqbit.ceptekabin.ui.navigation

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Auth          : Screen("auth")
    object Home          : Screen("home")
    object Dolap         : Screen("dolap")
    object Kombin        : Screen("kombin")
    object HavaDurumu    : Screen("hava_durumu")
    object Tarama        : Screen("tarama")
    object Ayarlar       : Screen("ayarlar")
    object KombinTakvim  : Screen("kombin_takvim")
    object StyleChatbot  : Screen("style_chatbot")
    object FriendDolap   : Screen("friend_dolap/{userId}") {
        fun createRoute(userId: String) = "friend_dolap/$userId"
    }
    object VirtualTryOn  : Screen("virtual_tryon/{kombinId}") {
        fun createRoute(kombinId: Long) = "virtual_tryon/$kombinId"
    }

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

    object Kategori : Screen("kategori/{id}") {
        fun createRoute(id: Long) = "kategori/$id"
    }

    object KombinImport : Screen("kombin_import/{uri}") {
        fun createRoute(encodedUri: String) = "kombin_import/$encodedUri"
    }
}

