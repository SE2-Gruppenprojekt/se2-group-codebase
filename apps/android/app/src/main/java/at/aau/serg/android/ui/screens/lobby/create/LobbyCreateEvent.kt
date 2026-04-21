package at.aau.serg.android.ui.screens.lobby.create

sealed class LobbyCreateEvent {

    object CreateLobby : LobbyCreateEvent()
    data class SetMaxPlayers(val value: Int) : LobbyCreateEvent()
    data class SetIsPrivate(val value: Boolean) : LobbyCreateEvent()
}
