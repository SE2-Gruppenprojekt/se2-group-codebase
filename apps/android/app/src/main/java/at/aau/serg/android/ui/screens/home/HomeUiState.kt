package at.aau.serg.android.ui.screens.home

import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState

data class HomeUiState(
    val loadState: LoadState = LoadState.Success,
    val user: User? = null
)
