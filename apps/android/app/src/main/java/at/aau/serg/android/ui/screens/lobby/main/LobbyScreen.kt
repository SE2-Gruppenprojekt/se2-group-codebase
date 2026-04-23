package at.aau.serg.android.ui.screens.lobby.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.getStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.lobby.main.components.LobbyContent

@Composable
fun LobbyScreen(
    navController: NavHostController,
    viewModel: LobbyViewModel,
    lobbyId: String
) {

    val dataStoreProvider = remember {
        DataStoreProvider
            .getInstance(navController.context)
    }

    val userStore = dataStoreProvider.getStore<User>()

    val user by userStore.data.collectAsState(
        initial = User.getDefaultInstance()
    )

    val lobbyState by viewModel.lobby.collectAsState()

    LaunchedEffect(lobbyId) {
        viewModel.loadLobby(lobbyId)
    }

    LobbyContent(
        lobby = lobbyState,
        onLeaveLobby = { id ->
            viewModel.leaveLobby(
                lobbyId = id,
                userId = user.uid,
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    )
}
