package com.example.inventra.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.inventra.presentation.screens.addedit.AddEditItemScreen
import com.example.inventra.presentation.screens.ai.AIInventoryScreen
import com.example.inventra.presentation.screens.auth.LoginScreen
import com.example.inventra.presentation.screens.catalog.CatalogScreen
import com.example.inventra.presentation.screens.dashboard.DashboardScreen
import com.example.inventra.presentation.screens.detail.ItemDetailScreen
import com.example.inventra.presentation.screens.history.HistoryScreen
import com.example.inventra.presentation.screens.profile.ProfileScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.Login.route
) {
    // Mendapatkan route saat ini untuk mengontrol state Bottom Navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Dashboard.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Dashboard.route) {
            DashboardScreen(
                currentRoute = currentRoute ?: Routes.Dashboard.route,
                onNavigate = { route -> navigateBottomNav(navController, route) },
                onNavigateToAddItem = { navController.navigate(Routes.AddEditItem.route) }
            )
        }

        composable(Routes.Catalog.route) {
            CatalogScreen(
                currentRoute = currentRoute ?: Routes.Catalog.route,
                onNavigate = { route -> navigateBottomNav(navController, route) },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Routes.ItemDetail.createRoute(itemId))
                },
                onNavigateToAddItem = { navController.navigate(Routes.AddEditItem.route) }
            )
        }

        composable(Routes.ItemDetail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toLongOrNull() ?: 0L
            ItemDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("${Routes.AddEditItem.route}?itemId=$id")
                }
            )
        }

        composable(
            route = "${Routes.AddEditItem.route}?itemId={itemId}",
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId")
                ?.takeIf { it != -1L }
            AddEditItemScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.History.route) {
            HistoryScreen(
                currentRoute = currentRoute ?: Routes.History.route,
                onNavigate = { route -> navigateBottomNav(navController, route) }
            )
        }

        composable(Routes.AskAI.route) {
            AIInventoryScreen(
                currentRoute = currentRoute ?: Routes.AskAI.route,
                onNavigate = { route -> navigateBottomNav(navController, route) }
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                currentRoute = currentRoute ?: Routes.Profile.route,
                onNavigate = { route -> navigateBottomNav(navController, route) },
                onLogoutClick = {
                    // Membersihkan seluruh tumpukan layar dan kembali ke Login
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AddEditItem.route) {
            AddEditItemScreen(
                isEditMode = false,
                onNavigateBack = { navController.popBackStack() },
                onSaveItem = { navController.popBackStack() }
            )
        }
    }
}

// Fungsi pembantu untuk mencegah penumpukan halaman saat berpindah via Bottom Nav
private fun navigateBottomNav(navController: NavHostController, route: String) {
    if (navController.currentDestination?.route != route) {
        navController.navigate(route) {
            popUpTo(Routes.Dashboard.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}