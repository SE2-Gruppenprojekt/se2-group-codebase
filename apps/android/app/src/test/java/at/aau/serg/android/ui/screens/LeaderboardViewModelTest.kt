package at.aau.serg.android.ui.screens

import at.aau.serg.android.network.leaderboard.LeaderboardAPI
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import shared.models.LeaderboardEntry

class LeaderboardViewModelTest {
    private val api = mockk<LeaderboardAPI>()

    @Test
    fun playersFlowUpdatesOnSuccessfulLoad() = runTest {
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

        coEvery { api.fetchLeaderboard() } returns expected

        val vm = LeaderboardViewModel(api)

        assertEquals(expected, vm.players.value)
    }

    @Test
    fun playersFlowBecomesEmptyOnFailure() = runTest {
        coEvery { api.fetchLeaderboard() } throws RuntimeException("Network error")

        val vm = LeaderboardViewModel(api)

        assertEquals(emptyList<LeaderboardEntry>(), vm.players.value)
    }
}
