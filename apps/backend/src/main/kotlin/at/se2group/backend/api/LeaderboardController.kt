package at.se2group.backend.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val gamesPlayed: Int,
    val wins: Int
)

@RestController
@RequestMapping("/api/leaderboard")
class LeaderboardController {

    @GetMapping
    fun getLeaderboard(): List<LeaderboardEntry> =
        listOf(
            LeaderboardEntry(1, "Julian", 1840, 42, 28),
            LeaderboardEntry(2, "Erik", 1765, 40, 24),
            LeaderboardEntry(3, "Vanessa", 1690, 38, 22),
            LeaderboardEntry(4, "Stefan", 1615, 36, 20),
            LeaderboardEntry(5, "Katrin", 1580, 35, 18),
            LeaderboardEntry(6, "Miriam", 1495, 33, 16),
            LeaderboardEntry(7, "Sabine", 1430, 31, 14),
            LeaderboardEntry(8, "Alex", 1375, 29, 12),
            LeaderboardEntry(9, "Nina", 1310, 27, 11),
            LeaderboardEntry(10, "Lukas", 1260, 25, 9)
        )
}
