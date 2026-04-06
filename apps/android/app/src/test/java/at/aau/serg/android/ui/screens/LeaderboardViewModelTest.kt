package at.aau.serg.android.ui.screens

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import at.aau.serg.android.network.leaderboard.LeaderboardAPI
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardViewModel
import shared.models.LeaderboardEntry

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val api = mockk<LeaderboardAPI>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun playersFlowUpdatesOnSuccessfulLoad() = runTest {
        val expected = listOf(
            LeaderboardEntry(1, "Alice", 100, 10, 8),
            LeaderboardEntry(2, "Bob", 80, 12, 6)
        )

        coEvery { api.fetchLeaderboard() } returns expected

        val vm = LeaderboardViewModel(api)

        // Run pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(expected, vm.players.value)
    }

    @Test
    fun playersFlowBecomesEmptyOnFailure() = runTest {
        coEvery { api.fetchLeaderboard() } throws RuntimeException("Network error")

        val vm = LeaderboardViewModel(api)

        // Run pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<LeaderboardEntry>(), vm.players.value)
    }
}
