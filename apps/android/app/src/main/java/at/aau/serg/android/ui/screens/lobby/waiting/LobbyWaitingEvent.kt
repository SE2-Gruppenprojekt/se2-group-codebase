package at.aau.serg.android.ui.screens.lobby.waiting

sealed class LobbyWaitingEvent {

    object OnTurnTimerIncrease : LobbyWaitingEvent()
    object OnTurnTimerDecrease : LobbyWaitingEvent()

    object OnStartingCardsIncrease : LobbyWaitingEvent()
    object OnStartingCardsDecrease : LobbyWaitingEvent()

    data class OnStackToggle(val enabled: Boolean) : LobbyWaitingEvent()

    data class OnLoadLobby(val lobbyId: String) : LobbyWaitingEvent()

    object OnBackClicked : LobbyWaitingEvent()
    object OnSettingsClicked : LobbyWaitingEvent()

    object onMatchStart : LobbyWaitingEvent()

}
