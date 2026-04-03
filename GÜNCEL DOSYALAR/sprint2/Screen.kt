package com.cyberqbit.ceptekabin.ui.navigation

// Screen.kt'ye eklenecek yeni rotalar

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Auth          : Screen("auth")
    object Home          : Screen("home")
    object Dolap         : Screen("dolap")
    object Kombin        : Screen("kombin")
    object HavaDurumu    : Screen("hava_durumu")
    object Tarama        : Screen("tarama")
    object Ayarlar       : Screen("ayarlar")
    object KombinTakvim  : Screen("kombin_takvim")   // Sprint 2 — #5
    object StyleChatbot  : Screen("style_chatbot")   // Sprint 3
    object FriendDolap   : Screen("friend_dolap/{userId}") {
        fun createRoute(userId: String) = "friend_dolap/$userId"
    }
    object VirtualTryOn  : Screen("virtual_tryon/{kombinId}") {
        fun createRoute(kombinId: Long) = "virtual_tryon/$kombinId"
    }

    object KiyaketEkle : Screen("kiyaket_ekle?barkod={barkod}&kiyaketId={kiyaketId}") {
        fun createRoute(barkod: String = "", kiyaketId: Long = 0L) =
            "kiyaket_ekle?barkod=${android.net.Uri.encode(barkod)}&kiyaketId=$kiyaketId"
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

/*
 * NavGraph.kt'deki NavHost bloğuna eklenecek composable'lar:
 *
 * // ── Kombin Takvimi ──────────────────────────────────────────────────────
 * composable(Screen.KombinTakvim.route) {
 *     KombinTakvimScreen(
 *         onNavigateBack          = { navController.popBackStack() },
 *         onNavigateToKombinDetay = { id ->
 *             navController.navigate(Screen.KombinDetay.createRoute(id))
 *         }
 *     )
 * }
 *
 * // ── Style Chatbot ────────────────────────────────────────────────────────
 * composable(Screen.StyleChatbot.route) {
 *     StyleChatbotScreen(onNavigateBack = { navController.popBackStack() })
 * }
 *
 * // ── Arkadaş Dolabı ───────────────────────────────────────────────────────
 * composable(
 *     route = Screen.FriendDolap.route,
 *     arguments = listOf(navArgument("userId") { type = NavType.StringType })
 * ) { backStack ->
 *     val userId = backStack.arguments?.getString("userId") ?: ""
 *     FriendDolapScreen(
 *         friendUserId   = userId,
 *         onNavigateBack = { navController.popBackStack() }
 *     )
 * }
 *
 * // ── Virtual Try-On ────────────────────────────────────────────────────────
 * composable(
 *     route = Screen.VirtualTryOn.route,
 *     arguments = listOf(navArgument("kombinId") { type = NavType.LongType })
 * ) { backStack ->
 *     val kombinId = backStack.arguments?.getLong("kombinId") ?: 0L
 *     VirtualTryOnScreen(
 *         kombinId       = kombinId,
 *         onNavigateBack = { navController.popBackStack() }
 *     )
 * }
 *
 * Ayrıca TaramaScreen'deki onBarkodFound'a DPP yönlendirmesi:
 *
 * TaramaScreen(
 *     onBarkodFound = { barkod ->
 *         navController.navigate(Screen.KiyaketEkle.createRoute(barkod))
 *     },
 *     onDppUrlFound = { url ->
 *         navController.navigate(Screen.KiyaketEkle.createRoute("DPP:$url"))
 *     },
 *     onNavigateBack = { navController.popBackStack() }
 * )
 */
