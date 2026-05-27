package com.example.inventra.presentation.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login_screen")
    object Dashboard : Routes("dashboard_screen")
    object Catalog : Routes("catalog_screen")
    object History : Routes("history_screen")
    object AskAI : Routes("ask_ai_screen")
    object AddEditItem : Routes("add_edit_item_screen")
    object Profile : Routes("profile_screen")

    // Rute dengan argumen dinamis (misal: ID Barang)
    object ItemDetail : Routes("item_detail_screen/{itemId}") {
        fun createRoute(itemId: String) = "item_detail_screen/$itemId"
    }
}