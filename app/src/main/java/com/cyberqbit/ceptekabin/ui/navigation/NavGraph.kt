package com.cyberqbit.ceptekabin.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinViewModel
import com.cyberqbit.ceptekabin.ui.screens.tarama.KiyaketEkleScreen
import com.cyberqbit.ceptekabin.ui.screens.tarama.TaramaScreen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Default.Home, "Ana Sayfa"),
    BottomNavItem(Screen.Dolap.route, Icons.Default.Checkroom, "Dolap"),
    BottomNavItem(Screen.Kombin.route, Icons.Default.Style, "Kombin"),
    BottomNavItem(Screen.HavaDurumu.route, Icons.Default.WbSunny, "Hava")
)

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Auth.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth Screen
            composable(Screen.Auth.route) {
                GoogleSignInScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToDolap = { navController.navigate(Screen.Dolap.route) },
                    onNavigateToKombin = { navController.navigate(Screen.Kombin.route) },
                    onNavigateToTarama = { navController.navigate(Screen.Tarama.route) },
                    onNavigateToHavaDurumu = { navController.navigate(Screen.HavaDurumu.route) }
                )
            }

            // Dolap Screen
            composable(Screen.Dolap.route) {
                DolapScreen(
                    onNavigateToTarama = { navController.navigate(Screen.Tarama.route) },
                    onNavigateToKiyaketDetay = { id ->
                        navController.navigate(Screen.KiyaketDetay.createRoute(id))
                    }
                )
            }

            // Kombin Screen
            composable(Screen.Kombin.route) {
                val kombinViewModel: KombinViewModel = hiltViewModel()
                KombinScreen(
                    viewModel = kombinViewModel,
                    onNavigateToKombinDetay = { id ->
                        navController.navigate(Screen.KombinDetay.createRoute(id))
                    },
                    onNavigateToKombinOlustur = {
                        navController.navigate(Screen.KombinOlustur.route)
                    }
                )
            }

            // Hava Durumu Screen
            composable(Screen.HavaDurumu.route) {
                HavaDurumuScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tarama Screen
            composable(Screen.Tarama.route) {
                TaramaScreen(
                    onBarkodFound = { barkod ->
                        navController.navigate(Screen.KiyaketEkle.createRoute(barkod))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Kiyaket Ekle Screen
            composable(
                route = Screen.KiyaketEkle.route,
                arguments = listOf(navArgument("barkod") { type = NavType.StringType })
            ) { backStackEntry ->
                val barkod = backStackEntry.arguments?.getString("barkod") ?: ""
                KiyaketEkleScreen(
                    barkod = barkod,
                    onNavigateBack = { navController.popBackStack() },
                    onKiyaketSaved = {
                        navController.navigate(Screen.Dolap.route) {
                            popUpTo(Screen.Dolap.route) { inclusive = true }
                        }
                    }
                )
            }

            // Kiyaket Detay Screen
            composable(
                route = Screen.KiyaketDetay.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                // KiyaketDetayScreen(id = id)
                // TODO: Implement detay ekranı
            }

            // Kombin Detay Screen
            composable(
                route = Screen.KombinDetay.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                KombinDetayScreen(
                    kombinId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Kombin Olustur Screen
            composable(Screen.KombinOlustur.route) {
                // KombinOlusturScreen()
                // TODO: Implement kombin oluştur ekranı
            }

            // Ayarlar Screen
            composable(Screen.Ayarlar.route) {
                // AyarlarScreen()
                // TODO: Implement ayarlar ekranı
            }
        }
    }
}
