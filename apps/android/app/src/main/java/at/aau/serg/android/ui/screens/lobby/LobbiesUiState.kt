package at.aau.serg.android.ui.screens.lobby

import shared.models.lobby.response.LobbyListItemResponse

sealed class LobbiesUiState {

    data object Loading : LobbiesUiState()

    data class Success(
        val lobbies: List<LobbyListItemResponse>
    ) : LobbiesUiState()

    data class Error(
        val message: String
    ) : LobbiesUiState()
}
