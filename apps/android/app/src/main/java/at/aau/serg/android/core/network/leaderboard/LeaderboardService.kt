package at.aau.serg.android.core.network.leaderboard


import retrofit2.http.GET
import shared.models.LeaderboardEntry

interface LeaderboardService {
    @GET("leaderboard")
    suspend fun fetchLeaderboard(): List<LeaderboardEntry>
}
