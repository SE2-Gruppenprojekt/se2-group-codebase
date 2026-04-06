package shared.models

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val gamesPlayed: Int,
    val wins: Int
)
