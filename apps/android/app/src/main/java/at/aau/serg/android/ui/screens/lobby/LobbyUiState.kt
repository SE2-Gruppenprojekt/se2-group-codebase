package at.aau.serg.android.ui.screens.lobby

import shared.models.lobby.domain.Lobby

sealed class LobbyUiState {

    data object Loading : LobbyUiState()

    data class Success(
        val lobby: Lobby
    ) : LobbyUiState()

    data class Error(
        val message: String
    ) : LobbyUiState()
}
