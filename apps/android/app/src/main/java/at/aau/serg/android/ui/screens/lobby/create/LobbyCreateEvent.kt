package at.aau.serg.android.ui.screens.lobby.create

sealed class LobbyCreateEvent {
    object OnSettings : LobbyCreateEvent()
    object OnBack : LobbyCreateEvent()
    object CreateLobby : LobbyCreateEvent()
    data class SetMaxPlayers(val value: Int) : LobbyCreateEvent()
    data class SetIsPrivate(val value: Boolean) : LobbyCreateEvent()
    data class SetQuickMode(val value: Boolean) : LobbyCreateEvent()
    data class SetRequireInitialMeld(val value: Boolean) : LobbyCreateEvent()
    data class ChangeTurnTimer(val delta: Int) : LobbyCreateEvent()
    data class ChangeStartingTiles(val delta: Int) : LobbyCreateEvent()
    data class ChangeWinScore(val delta: Int) : LobbyCreateEvent()
}
