package at.aau.serg.android.navigation

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import at.aau.serg.android.core.ui.GenericViewModelFactory
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.getStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.auth.AuthScreen
import at.aau.serg.android.ui.screens.auth.AuthViewModel

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    provider: DataStoreProvider
) {

    navigation(
        startDestination = Routes.USERNAME,
        route = Routes.AUTH
    ) {

        composable(Routes.USERNAME) {
            val userStore = remember { provider.getStore<User>() }

            val vm: AuthViewModel = viewModel(
                factory = GenericViewModelFactory { AuthViewModel(userStore) }
            )

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
