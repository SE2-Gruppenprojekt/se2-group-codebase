package at.aau.serg.android.network.leaderboard

import shared.models.LeaderboardEntry

class LeaderboardAPI(
    private val service: LeaderboardService
) {
    suspend fun fetchLeaderboard(): List<LeaderboardEntry> {
        return service.fetchLeaderboard()
    }
}
