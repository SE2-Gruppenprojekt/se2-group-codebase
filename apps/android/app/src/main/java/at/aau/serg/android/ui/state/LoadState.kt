package at.aau.serg.android.ui.state

import at.aau.serg.android.core.errors.AppError

sealed interface LoadState {
    object Idle : LoadState
    object Loading : LoadState
    data class Error(val error: AppError) : LoadState
    object Success : LoadState
}
