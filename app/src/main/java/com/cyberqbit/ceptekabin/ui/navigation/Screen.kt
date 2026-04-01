package com.cyberqbit.ceptekabin.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Dolap : Screen("dolap")
    object Kombin : Screen("kombin")
    object HavaDurumu : Screen("hava_durumu")
    object Tarama : Screen("tarama")
    object KiyaketEkle : Screen("kiyaket_ekle/{barkod}") {
        fun createRoute(barkod: String) = "kiyaket_ekle/$barkod"
    }
    object KiyaketDetay : Screen("kiyaket_detay/{id}") {
        fun createRoute(id: Long) = "kiyaket_detay/$id"
    }
    object KombinDetay : Screen("kombin_detay/{id}") {
        fun createRoute(id: Long) = "kombin_detay/$id"
    }
    object KombinOlustur : Screen("kombin_olustur")
    object Ayarlar : Screen("ayarlar")
    object Kategori : Screen("kategori/{id}") {
        fun createRoute(id: Long) = "kategori/$id"
    }
}
