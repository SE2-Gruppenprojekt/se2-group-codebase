package at.aau.serg.android.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.network.RetrofitProvider
import at.aau.serg.android.network.leaderboard.LeaderboardAPI
import at.aau.serg.android.network.leaderboard.LeaderboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shared.models.LeaderboardEntry

class LeaderboardViewModel(
    private val api: LeaderboardAPI = LeaderboardAPI(
        RetrofitProvider.retrofit.create(LeaderboardService::class.java)
    )
) : ViewModel() {

    private val _players = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val players = _players.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            runCatching { api.fetchLeaderboard() }
                .onSuccess { _players.value = it }
                .onFailure { _players.value = emptyList() }
        }
    }
}

