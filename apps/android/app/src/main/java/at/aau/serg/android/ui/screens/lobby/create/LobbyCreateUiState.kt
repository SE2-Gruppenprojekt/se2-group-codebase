package at.aau.serg.android.ui.screens.lobby.create

import at.aau.serg.android.ui.state.LoadState

data class LobbyCreateUiState(
    val loadState: LoadState = LoadState.Success,
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val turnTimer: Int = 60,
    val startingTiles: Int = 14,
    val winScore: Int = 500,
    val quickMode: Boolean = false,
    val requireInitialMeld: Boolean = true,
)
