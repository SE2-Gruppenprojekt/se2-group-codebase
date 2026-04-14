package at.aau.serg.android.ui.lobby

import shared.models.lobby.domain.Lobby


sealed class LobbyUiStateLoading {
    object Loading : LobbyUiStateLoading()
    data class Success(val lobby: Lobby) : LobbyUiStateLoading()
    data class Error(val message: String) : LobbyUiStateLoading()
}

