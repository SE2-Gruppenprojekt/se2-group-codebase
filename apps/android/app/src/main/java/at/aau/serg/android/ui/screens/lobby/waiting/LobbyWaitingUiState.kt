package at.aau.serg.android.ui.screens.lobby.waiting

import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer


data class LobbyWaitingUiState(
    val lobby: Lobby? = null,
    val lobbyName: String = "",
    val turnTimer: Int = 60,
    val startingCards: Int = 5,
    val stackEnabled: Boolean = false,
    val players: List<LobbyPlayer> = emptyList(),
    val isLoading: Boolean = true
)
