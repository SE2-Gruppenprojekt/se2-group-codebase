package at.aau.serg.android.network.leaderboard

import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import shared.models.LeaderboardEntry


class LeaderboardAPITest {

    private val service = mockk<LeaderboardService>()
    private val api = LeaderboardAPI(service)

    @Test
    fun fetchLeaderboard_returnsListFromService() = runTest {
        val expected = listOf(
            LeaderboardEntry(
                rank = 1,
                playerName = "Alice",
                score = 100,
                gamesPlayed = 10,
                wins = 8
            ),
            LeaderboardEntry(
                rank = 2,
                playerName = "Bob",
                score = 80,
                gamesPlayed = 12,
                wins = 6
            )
        )

        coEvery { service.fetchLeaderboard() } returns expected

        val result = api.fetchLeaderboard()

        assertEquals(expected, result)
        coVerify { service.fetchLeaderboard() }
    }
}
