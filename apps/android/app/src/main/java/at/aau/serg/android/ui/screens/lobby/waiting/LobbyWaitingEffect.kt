package at.aau.serg.android.ui.screens.lobby.waiting

sealed class LobbyWaitingEffect {

    data class NavigateToMatch(val matchId: String) : LobbyWaitingEffect()

    object NavigateBack : LobbyWaitingEffect()


    object NavigateToSettings : LobbyWaitingEffect()
}
