package at.aau.serg.android.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import at.aau.serg.android.ui.screens.auth.AuthScreen
import at.aau.serg.android.ui.screens.auth.AuthViewModel

fun NavGraphBuilder.authGraph(navController: NavHostController) {

    navigation(
        startDestination = Routes.USERNAME,
        route = Routes.AUTH
    ) {

        composable(Routes.USERNAME) {
            val vm: AuthViewModel = viewModel()

            AuthScreen(
                viewModel = vm,
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
    }
}
