package at.aau.serg.android.ui.lobby

import shared.models.lobby.response.LobbyListItemResponse

sealed class LobbiesUiState {
    object Loading : LobbiesUiState()
    data class Success(val lobbies: List<LobbyListItemResponse>) : LobbiesUiState()
    data class Error(val message: String) : LobbiesUiState()
}
