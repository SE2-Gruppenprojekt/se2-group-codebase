package at.aau.serg.android.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.jackson.*
import shared.models.LeaderboardEntry

object LeaderboardApi {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    suspend fun fetchLeaderboard(): List<LeaderboardEntry> {
        return client.get("http://10.0.2.2:8080/api/leaderboard").body()
    }
}
