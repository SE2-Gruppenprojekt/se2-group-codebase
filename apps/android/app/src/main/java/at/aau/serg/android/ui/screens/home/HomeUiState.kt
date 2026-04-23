package at.aau.serg.android.ui.screens.home

import at.aau.serg.android.ui.state.LoadState

data class HomeUiState(
    val username: String = "Guest",
    val uid: String = "",
    val loadState: LoadState = LoadState.Success
)
