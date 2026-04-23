package at.aau.serg.android.ui.screens.lobby.waiting

sealed class LobbyWaitingEffect {

    data class NavigateToMatch(val matchId: String) : LobbyWaitingEffect()

    object NavigateBack : LobbyWaitingEffect()

    data class ShowError(val message: String) : LobbyWaitingEffect()
}
