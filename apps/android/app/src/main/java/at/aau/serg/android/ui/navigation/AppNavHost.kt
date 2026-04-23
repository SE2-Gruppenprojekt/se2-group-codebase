package at.aau.serg.android.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.map

sealed class UserState {
    data object Loading : UserState()
    data class Ready(val user: User) : UserState()
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun AppNavHost(
    navController: NavHostController,
    context: Context,
    innerPadding: PaddingValues = PaddingValues(),
    userStore: ProtoStore<User>
) {
    val dataStoreProvider = remember {
        DataStoreProvider.getInstance(context)
    }

    val userState by userStore.data
        .map<User, UserState> { user -> UserState.Ready(user) }
        .collectAsState(initial = UserState.Loading)

    when (val state = userState) {
        is UserState.Loading -> {
            Box(
                modifier = Modifier
                    .testTag(AppNavTestTags.LOADING)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UserState.Ready -> {
            val user = state.user

            val startDestination =
                if (user.uid.isNotBlank()) Routes.HOME else Routes.AUTH

            NavHost(
                navController = navController,
                startDestination = startDestination,
                route = "root",
                modifier = Modifier
                    .testTag(AppNavTestTags.SCREEN)
                    .padding(innerPadding)
            ) {
                authGraph(navController, dataStoreProvider)
                homeGraph(navController, dataStoreProvider)
            }
        }
    }
}
