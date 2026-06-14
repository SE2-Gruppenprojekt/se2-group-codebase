package at.aau.serg.android.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.getStore
import at.aau.serg.android.core.util.GenericViewModelFactory
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.auth.AuthEffect
import at.aau.serg.android.ui.screens.auth.AuthMode
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

            LaunchedEffect(Unit) {
                vm.setMode(AuthMode.CreateUser)
            }

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        is AuthEffect.NavigateBack ->
                            navController.popBackStack()

                        AuthEffect.NavigateContinue -> {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                    }
                }
            }

            AuthScreen(viewModel = vm)
        }
    }
}
