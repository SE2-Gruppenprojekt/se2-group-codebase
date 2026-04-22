package at.aau.serg.android.ui.screens.lobby.browse

sealed class LobbyBrowseEffect {
    data class JoinLobby(val lobbyId: String) : LobbyBrowseEffect()
    object NavigateToCreate : LobbyBrowseEffect()
    object NavigateToSettings : LobbyBrowseEffect()
    object NavigateBack : LobbyBrowseEffect()
}
