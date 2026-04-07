package at.aau.serg.android.ui.screens.leaderboard

import at.aau.serg.android.network.RetrofitProvider
import at.aau.serg.android.network.leaderboard.LeaderboardAPI
import at.aau.serg.android.network.leaderboard.LeaderboardService
import at.aau.serg.android.viewmodel.BaseViewModel
import at.aau.serg.android.util.DispatcherProvider
import at.aau.serg.android.util.DefaultDispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import shared.models.LeaderboardEntry

class LeaderboardViewModel(
    private val api: LeaderboardAPI = LeaderboardAPI(
        RetrofitProvider.retrofit.create(LeaderboardService::class.java)
    ),
    dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : BaseViewModel(dispatchers) {

    private val _players = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val players: StateFlow<List<LeaderboardEntry>> = _players

    fun loadLeaderboard(
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = { api.fetchLeaderboard() },
            onSuccess = { result ->
                _players.value = result
                onSuccess()
            },
            onError = {
                _players.value = emptyList()
                onError()
            }
        )
    }
}
