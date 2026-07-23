package com.airmouse3d.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airmouse3d.ui.about.AboutScreen
import com.airmouse3d.ui.home.HomeScreen
import com.airmouse3d.ui.scan.ScanScreen
import com.airmouse3d.ui.splash.SplashScreen

@Composable
fun AirMouseNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onAboutClick = { navController.navigate(Screen.About.route) },
                onScanClick = { navController.navigate(Screen.Scan.route) },
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                onPaired = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
