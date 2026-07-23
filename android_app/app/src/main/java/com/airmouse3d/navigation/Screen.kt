package com.airmouse3d.navigation

/** Type-safe route names for the app's destinations. */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object About : Screen("about")
    data object Scan : Screen("scan")
}
