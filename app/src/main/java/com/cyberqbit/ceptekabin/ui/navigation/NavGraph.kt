package com.cyberqbit.ceptekabin.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cyberqbit.ceptekabin.ui.screens.auth.AuthViewModel
import com.cyberqbit.ceptekabin.ui.screens.auth.GoogleSignInScreen
import com.cyberqbit.ceptekabin.ui.screens.dolap.DolapScreen
import com.cyberqbit.ceptekabin.ui.screens.havadurumu.HavaDurumuScreen
import com.cyberqbit.ceptekabin.ui.screens.home.HomeScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinDetayScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinImportScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinOlusturScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinViewModel
import com.cyberqbit.ceptekabin.ui.screens.tarama.KiyaketEkleScreen
import com.cyberqbit.ceptekabin.ui.screens.tarama.TaramaScreen
import com.cyberqbit.ceptekabin.ui.theme.*

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route,       Icons.Default.Home,      "Ana Sayfa"),
    BottomNavItem(Screen.Dolap.route,      Icons.Default.Checkroom, "Dolap"),
    BottomNavItem(Screen.Kombin.route,     Icons.Default.Style,     "Kombin"),
    BottomNavItem(Screen.HavaDurumu.route, Icons.Default.WbSunny,   "Hava")
)

private val mainScreenRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun NavGraph(
    navController: NavHostController,
    pendingImportUri: Uri? = null,
    onImportUriConsumed: () -> Unit = {},
    startDestination: String = Screen.Auth.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in mainScreenRoutes

    val isDark = isSystemInDarkTheme()

    LaunchedEffect(pendingImportUri, currentRoute) {
        val uri = pendingImportUri ?: return@LaunchedEffect
        if (currentRoute in mainScreenRoutes) {
            val encoded = Uri.encode(uri.toString())
            navController.navigate(Screen.KombinImport.createRoute(encoded)) { launchSingleTop = true }
            onImportUriConsumed()
        }
    }

    Scaffold(
        // Standart bottomBar'ı kullanmıyoruz, Box içinde kendimiz yüzen bar çizeceğiz.
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            // ANA İÇERİK
            NavHost(
                navController = navController, 
                startDestination = Screen.Splash.route, 
                // Padding'i sadece yukarı için veriyoruz, alt taraf serbest
                modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
            ) {

                composable(Screen.Splash.route) {
                    com.cyberqbit.ceptekabin.ui.screens.splash.SplashScreen(
                        onNavigateToHome = {
                            val dest = if (isLoggedIn) Screen.Home.route else Screen.Auth.route
                            navController.navigate(dest) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        }
                    )
                }

                composable(Screen.Auth.route) {
                    GoogleSignInScreen(
                        onSignInSuccess = {
                            navController.navigate(Screen.Home.route) { popUpTo(Screen.Auth.route) { inclusive = true } }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToDolap        = { navController.navigate(Screen.Dolap.route) },
                        onNavigateToKombin       = { navController.navigate(Screen.Kombin.route) },
                        onNavigateToTarama       = { navController.navigate(Screen.Tarama.route) },
                        onNavigateToHavaDurumu   = { navController.navigate(Screen.HavaDurumu.route) },
                        onNavigateToKiyaket      = { id -> navController.navigate(Screen.KiyaketDetay.createRoute(id)) },
                        onNavigateToKombinTakvim = { navController.navigate(Screen.KombinTakvim.route) },
                        onNavigateToVirtualTryOn = { navController.navigate(Screen.VirtualTryOn.route) }
                    )
                }

                composable(Screen.Dolap.route) {
                    DolapScreen(
                        onNavigateToTarama = { navController.navigate(Screen.Tarama.route) },
                        onNavigateToKiyaketDetay = { id -> navController.navigate(Screen.KiyaketDetay.createRoute(id)) }
                    )
                }

                composable(Screen.Kombin.route) {
                    val kombinViewModel: KombinViewModel = hiltViewModel()
                    KombinScreen(
                        viewModel = kombinViewModel,
                        onNavigateToKombinDetay  = { id -> navController.navigate(Screen.KombinDetay.createRoute(id)) },
                        onNavigateToKombinOlustur = { navController.navigate(Screen.KombinOlustur.route) }
                    )
                }

                composable(Screen.HavaDurumu.route) {
                    HavaDurumuScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(Screen.Tarama.route) {
                    TaramaScreen(
                        onBarkodFound  = { barkod -> navController.navigate(Screen.KiyaketEkle.createRoute(barkod)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(route = Screen.KiyaketEkle.route, arguments = listOf(navArgument("barkod") { type = NavType.StringType; defaultValue = "" }, navArgument("kiyaketId") { type = NavType.LongType; defaultValue = 0L })) { backStack ->
                    val barkod = backStack.arguments?.getString("barkod") ?: ""
                    val kiyaketId = backStack.arguments?.getLong("kiyaketId") ?: 0L
                    KiyaketEkleScreen(
                        barkod = barkod, kiyaketId = kiyaketId, onNavigateBack = { navController.popBackStack() },
                        onKiyaketSaved = {
                            navController.navigate(Screen.Dolap.route) { popUpTo(Screen.KiyaketEkle.route) { inclusive = true }; launchSingleTop = true }
                        }
                    )
                }

                composable(route = Screen.KiyaketDetay.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStack ->
                    val id = backStack.arguments?.getLong("id") ?: 0L
                    com.cyberqbit.ceptekabin.ui.screens.dolap.KiyaketDetayScreen(
                        kiyaketId = id, onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { kId -> navController.navigate(Screen.KiyaketEkle.createRoute("", kId)) }
                    )
                }

                composable(route = Screen.KombinDetay.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStack ->
                    val id = backStack.arguments?.getLong("id") ?: 0L
                    KombinDetayScreen(
                        kombinId = id, onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { kId -> navController.navigate(Screen.KombinOlustur.createRoute(kId)) },
                        onNavigateToKiyaket = { kId -> navController.navigate(Screen.KiyaketDetay.createRoute(kId)) }
                    )
                }

                composable(route = Screen.KombinOlustur.route, arguments = listOf(navArgument("kombinId") { type = NavType.LongType; defaultValue = 0L })) { backStack ->
                    val kombinId = backStack.arguments?.getLong("kombinId") ?: 0L
                    KombinOlusturScreen(
                        kombinId = kombinId, onNavigateBack = { navController.popBackStack() },
                        onKombinSaved = {
                            navController.navigate(Screen.Kombin.route) { popUpTo(Screen.KombinOlustur.route) { inclusive = true }; launchSingleTop = true }
                        }
                    )
                }

                composable(route = Screen.KombinImport.route, arguments = listOf(navArgument("uri") { type = NavType.StringType })) { backStack ->
                    val encodedUri = backStack.arguments?.getString("uri") ?: ""
                    val uri = Uri.parse(Uri.decode(encodedUri))
                    KombinImportScreen(
                        uri = uri, onNavigateBack = { navController.popBackStack() },
                        onImportSuccess = { kombinId ->
                            navController.navigate(Screen.KombinDetay.createRoute(kombinId)) { popUpTo(Screen.KombinImport.route) { inclusive = true }; launchSingleTop = true }
                        }
                    )
                }
                
                composable(Screen.KombinTakvim.route) {
                    com.cyberqbit.ceptekabin.ui.screens.kombin.KombinTakvimScreen(
                        onNavigateBack = { navController.popBackStack() }, 
                        onNavigateToKombinDetay = { id -> navController.navigate(Screen.KombinDetay.createRoute(id))}
                    )
                }
                composable(Screen.VirtualTryOn.route) {
                    com.cyberqbit.ceptekabin.ui.screens.tryon.VirtualTryOnScreen(
                        onNavigateBack = { navController.popBackStack() },
                        kombinId = 0L // Adding default so it compiles
                    )
                }
            }

            // YÜZEN (FLOATING) BOTTOM BAR EKLENTİSİ
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
                    containerColor = if (isDark) SurfaceVariantDark.copy(alpha = 0.95f) else White.copy(alpha = 0.95f),
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = currentRoute == item.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryLight,
                                selectedTextColor = PrimaryLight,
                                unselectedIconColor = if (isDark) Grey400 else Grey600,
                                unselectedTextColor = if (isDark) Grey400 else Grey600,
                                indicatorColor = PrimaryCyan.copy(alpha = 0.2f)
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = item.route == Screen.Home.route }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
