package com.moses.inspectionapp.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.ui.screens.auth.LoginScreen
import com.moses.inspectionapp.ui.screens.onboarding.OnboardingScreen
import com.moses.inspectionapp.ui.screens.splash.SplashScreen

@Composable
fun AppNavHost(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val nextRoute = when {
        !AppPreferences.hasOnboarded -> AppRoute.Onboarding.route
        !AppPreferences.hasSession -> AppRoute.Login.route
        else -> AppRoute.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { it / 2 },
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetX = { -it / 2 },
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { -it / 2 },
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetX = { it / 2 },
            ) + fadeOut(animationSpec = tween(300))
        },
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onFinish = {
                    navController.navigate(nextRoute) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoute.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    AppPreferences.hasOnboarded = true
                    val destination = if (!AppPreferences.hasSession) {
                        AppRoute.Login.route
                    } else {
                        AppRoute.Main.route
                    }
                    navController.navigate(destination) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Main.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoute.Main.route) {
            MainScaffold(
                windowSizeClass = windowSizeClass,
                onLogout = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Main.route) { inclusive = true }
                    }
                },
            )
        }
    }
}
