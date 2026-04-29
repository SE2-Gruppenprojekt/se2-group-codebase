package at.aau.serg.android.ui.screens.lobby.waiting

import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import shared.models.lobby.domain.Lobby


data class LobbyWaitingUiState(
    val lobby: Lobby? = null,
    val lobbyName: String = "",
    val turnTimer: Int = 60,
    val startingCards: Int = 5,
    val stackEnabled: Boolean = false,
    val loadState: LoadState = LoadState.Success,
    val user: User? = null
)
