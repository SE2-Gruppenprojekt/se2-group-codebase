package at.aau.serg.android.ui.screens.lobby.browse

import at.aau.serg.android.ui.state.LoadState

data class LobbyBrowseUiState(
    val loadState: LoadState = LoadState.Success,
    val lobbies: List<LobbyBrowseItem> = emptyList(),
    val lobbyIdInput: String = ""
)
