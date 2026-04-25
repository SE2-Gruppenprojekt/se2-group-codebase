package at.aau.serg.android.ui.screens.lobby.waiting

import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseEffect

sealed class LobbyWaitingEffect {

    object NavigateToMatch: LobbyWaitingEffect()

    object NavigateBack : LobbyWaitingEffect()


    object NavigateToSettings : LobbyWaitingEffect()
}
