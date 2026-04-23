package at.aau.serg.android.ui.screens.lobby.main.components

import shared.models.lobby.response.LobbyListItemResponse

data class LobbiesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: List<LobbyListItemResponse> = emptyList()
)
