package at.aau.serg.android.ui.screens.lobby.browse

sealed class LobbyBrowseEvent {
    data class OnLobbyIdChanged(val input: String) : LobbyBrowseEvent()
    data class OnJoinLobby(val lobbyId: String) : LobbyBrowseEvent()
    object OnCreateNewLobby : LobbyBrowseEvent()
    object OnSettings : LobbyBrowseEvent()
    object OnBack : LobbyBrowseEvent()
}
