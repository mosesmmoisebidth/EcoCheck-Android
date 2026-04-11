package com.moses.inspectionapp.ui.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Onboarding : AppRoute("onboarding")
    data object Language : AppRoute("language")
    data object Login : AppRoute("login")
    data object Main : AppRoute("main")
}
