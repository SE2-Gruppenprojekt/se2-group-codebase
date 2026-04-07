package at.aau.serg.android.ui.screens.leaderboard

sealed interface LeaderboardLoadState {
    object Idle : LeaderboardLoadState
    object Loading : LeaderboardLoadState
    data class Error(val message: String) : LeaderboardLoadState
    object Success : LeaderboardLoadState
}
