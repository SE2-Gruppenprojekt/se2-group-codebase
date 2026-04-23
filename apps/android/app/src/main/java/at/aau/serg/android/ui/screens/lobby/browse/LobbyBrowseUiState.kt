package at.aau.serg.android.ui.screens.lobby.browse

data class LobbyBrowseUiState(
    val lobbies: List<LobbyBrowseItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lobbyIdInput: String = ""
)
