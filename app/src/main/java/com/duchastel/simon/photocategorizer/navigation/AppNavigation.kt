package com.duchastel.simon.photocategorizer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duchastel.simon.photocategorizer.navigation.NavDestination.Login
import com.duchastel.simon.photocategorizer.navigation.NavDestination.PhotoSwiper
import com.duchastel.simon.photocategorizer.navigation.NavDestination.Splash
import com.duchastel.simon.photocategorizer.screens.login.LoginScreen
import com.duchastel.simon.photocategorizer.screens.login.LoginViewModel
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperScreen
import com.duchastel.simon.photocategorizer.screens.splash.SplashScreen
import kotlinx.serialization.Serializable

@Composable
fun AppNavigation(viewModel: LoginViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        println("TODO NAV")
    }

    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    val isLoggedIn = state.isLoggedIn
    LaunchedEffect(isLoggedIn) {
        println("TODO - $isLoggedIn")
        when (isLoggedIn) {
            true -> navController.navigate(PhotoSwiper)
            false -> navController.navigate(Login)
            null -> Unit // stay on splash screen while waiting for login state
        }
    }

    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen()
        }
        composable<Login> {
            LoginScreen()
        }
        composable<PhotoSwiper> {
            PhotoSwiperScreen(logout = {
                viewModel.logout()
                navController.navigate(Login)
            })
        }
    }
}

object NavDestination {
    @Serializable
    object Splash

    @Serializable
    object Login

    @Serializable
    object PhotoSwiper
}