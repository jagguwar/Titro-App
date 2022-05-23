package com.example.titro.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.example.titro.navigation.destinations.listComposable
import com.example.titro.navigation.destinations.noteComposable
import com.example.titro.navigation.destinations.splashComposable
import com.example.titro.ui.viewmodels.SharedViewModel
import com.example.titro.util.Constants.SPLASH_SCREEN
import com.google.accompanist.navigation.animation.AnimatedNavHost

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SetupNavigation(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {

    val screen = remember(navController) {
        Screens(navController = navController)
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = SPLASH_SCREEN
    ) {
        splashComposable(
            navigateToListScreen = screen.splash
        )
        listComposable(
            navigateToNoteScreen = screen.list,
            sharedViewModel = sharedViewModel
        )
        noteComposable(
            navigateToListScreen = screen.note,
            sharedViewModel = sharedViewModel
        )
    }

}