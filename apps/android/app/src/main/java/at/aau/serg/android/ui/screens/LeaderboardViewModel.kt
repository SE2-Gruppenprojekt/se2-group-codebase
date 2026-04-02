package at.aau.serg.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.network.LeaderboardApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shared.models.LeaderboardEntry

class LeaderboardViewModel : ViewModel() {

    private val _players = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val players = _players.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            runCatching {
                LeaderboardApi.fetchLeaderboard()
            }.onSuccess { list ->
                _players.value = list
            }.onFailure {
                // you can log or show error state later
                _players.value = emptyList()
            }
        }
    }
}
