package at.aau.serg.android.ui.screens.lobby.create

sealed class LobbyCreateEffect {
    data class NavigateToWaitingRoom(val lobbyId: String) : LobbyCreateEffect()
    object NavigateToSettings : LobbyCreateEffect()
    object NavigateBack : LobbyCreateEffect()
}
