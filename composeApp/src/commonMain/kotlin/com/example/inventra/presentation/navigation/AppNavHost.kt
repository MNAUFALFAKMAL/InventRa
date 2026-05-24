package com.example.inventra.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navigationActions = createNavigationActions(navController)
    
    NavHost(
        navController = navController,
        startDestination = Route.Dashboard,
        modifier = modifier
    ) {
        composable<Route.Dashboard> {
            PlaceholderScreen("Dashboard Screen")
        }
        
        composable<Route.Catalog> {
            PlaceholderScreen("Catalog Screen")
        }

        composable<Route.History> {
            PlaceholderScreen("History Screen")
        }
        
        composable<Route.ItemDetail> { backStackEntry ->
            val route: Route.ItemDetail = backStackEntry.toRoute()
            PlaceholderScreen("Item Detail Screen for ID: ${route.itemId}")
        }
        
        composable<Route.AddEditItem> { backStackEntry ->
            val route: Route.AddEditItem = backStackEntry.toRoute()
            PlaceholderScreen("Add/Edit Item Screen for ID: ${route.itemId ?: "New"}")
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}

interface NavigationActions {
    fun navigateToDashboard()
    fun navigateToCatalog()
    fun navigateToHistory()
    fun navigateToItemDetail(itemId: Long)
    fun navigateToAddEditItem(itemId: Long? = null)
    fun navigateBack()
}

private fun createNavigationActions(navController: NavHostController): NavigationActions {
    return object : NavigationActions {
        override fun navigateToDashboard() {
            navController.navigate(Route.Dashboard) {
                popUpTo(Route.Dashboard) { inclusive = true }
            }
        }
        
        override fun navigateToCatalog() {
            navController.navigate(Route.Catalog)
        }

        override fun navigateToHistory() {
            navController.navigate(Route.History)
        }
        
        override fun navigateToItemDetail(itemId: Long) {
            navController.navigate(Route.ItemDetail(itemId))
        }
        
        override fun navigateToAddEditItem(itemId: Long?) {
            navController.navigate(Route.AddEditItem(itemId))
        }

        override fun navigateBack() {
            navController.popBackStack()
        }
    }
}
