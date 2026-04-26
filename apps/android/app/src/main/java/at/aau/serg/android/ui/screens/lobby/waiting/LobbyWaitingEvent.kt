package at.aau.serg.android.ui.screens.lobby.waiting

import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseEvent

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

    object OnSettings : LobbyWaitingEvent()
    object OnBack : LobbyWaitingEvent()

}
