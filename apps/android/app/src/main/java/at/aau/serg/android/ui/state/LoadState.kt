package at.aau.serg.android.ui.state

sealed interface LoadState {
    object Idle : LoadState
    object Loading : LoadState
    data class Error(val message: String) : LoadState
    object Success : LoadState
}
