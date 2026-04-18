package at.aau.serg.android.ui.screens.lobby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import at.aau.serg.android.ui.screens.lobby.components.LobbyContent

@Composable
fun LobbyScreen(
    navController: NavHostController,
    viewModel: LobbyViewModel,
    lobbyId: String
) {

    val dataStoreProvider = remember {
        at.aau.serg.android.datastore.core.DataStoreProvider
            .getInstance(navController.context)
    }

    val userStore = dataStoreProvider.userStore

    val user by userStore.data.collectAsState(
        initial = at.aau.serg.android.datastore.proto.User.getDefaultInstance()
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
