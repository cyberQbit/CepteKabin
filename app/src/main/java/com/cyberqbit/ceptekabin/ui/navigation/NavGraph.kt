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
import com.cyberqbit.ceptekabin.ui.screens.dolap.KiyaketDetayScreen   // NEW
import com.cyberqbit.ceptekabin.ui.screens.havadurumu.HavaDurumuScreen
import com.cyberqbit.ceptekabin.ui.screens.home.HomeScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinDetayScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinOlusturScreen  // NEW
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinScreen
import com.cyberqbit.ceptekabin.ui.screens.kombin.KombinViewModel
import com.cyberqbit.ceptekabin.ui.screens.tarama.KiyaketEkleScreen
import com.cyberqbit.ceptekabin.ui.screens.tarama.TaramaScreen

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route,        Icons.Default.Home,       "Ana Sayfa"),
    BottomNavItem(Screen.Dolap.route,       Icons.Default.Checkroom,  "Dolap"),
    BottomNavItem(Screen.Kombin.route,      Icons.Default.Style,      "Kombin"),
    BottomNavItem(Screen.HavaDurumu.route,  Icons.Default.WbSunny,    "Hava")
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
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop back to the first destination for proper back stack management
                                        popUpTo(Screen.Home.route) {
                                            saveState = item.route == Screen.Home.route
                                        }
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {

            composable(Screen.Splash.route) {
                com.cyberqbit.ceptekabin.ui.screens.splash.SplashScreen(
                    onNavigateToHome = {
                        val destination = if (isLoggedIn) Screen.Home.route else Screen.Auth.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                GoogleSignInScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToDolap      = { navController.navigate(Screen.Dolap.route) },
                    onNavigateToKombin     = { navController.navigate(Screen.Kombin.route) },
                    onNavigateToTarama     = { navController.navigate(Screen.Tarama.route) },
                    onNavigateToHavaDurumu = { navController.navigate(Screen.HavaDurumu.route) },
                    onNavigateToKiyaket    = { id -> navController.navigate(Screen.KiyaketDetay.createRoute(id)) }
                )
            }

            composable(Screen.Dolap.route) {
                DolapScreen(
                    onNavigateToTarama = { navController.navigate(Screen.Tarama.route) },
                    onNavigateToKiyaketDetay = { id ->
                        navController.navigate(Screen.KiyaketDetay.createRoute(id))
                    }
                )
            }

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

            composable(Screen.HavaDurumu.route) {
                HavaDurumuScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.Tarama.route) {
                TaramaScreen(
                    onBarkodFound = { barkod ->
                        navController.navigate(Screen.KiyaketEkle.createRoute(barkod))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

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
                            popUpTo(Screen.KiyaketEkle.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // FIX: properly implemented KiyaketDetay
            composable(
                route = Screen.KiyaketDetay.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                KiyaketDetayScreen(
                    kiyaketId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.KombinDetay.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                KombinDetayScreen(
                    kombinId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToKiyaket = { kiyaketId -> navController.navigate(Screen.KiyaketDetay.createRoute(kiyaketId)) }
                )
            }

            // FIX: properly implemented KombinOlustur
            composable(Screen.KombinOlustur.route) {
                KombinOlusturScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onKombinSaved = {
                        navController.navigate(Screen.Dolap.route) {
                            popUpTo(Screen.KombinOlustur.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}


