package at.aau.serg.android.ui.screens.leaderboard

import at.aau.serg.android.core.network.leaderboard.LeaderboardAPI
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.util.DispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
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
import shared.models.LeaderboardEntry

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val api = mockk<LeaderboardAPI>()
    private val testDispatcher = StandardTestDispatcher()

    // Test dispatcher provider
    private class TestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher
    ) : DispatcherProvider {
        override val main = dispatcher
        override val io = dispatcher
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- DEFAULT CONSTRUCTOR COVERAGE ---

    @Test
    fun defaultConstructor_initializesRetrofitApi() = runTest {
        val vm = LeaderboardViewModel(
            dispatchers = TestDispatcherProvider(testDispatcher)
        )
        assertEquals(LoadState.Idle, vm.loadState.value)
    }

    @Test
    fun defaultDispatcherProvider_isUsedWhenNoDispatcherIsPassed() = runTest {
        val vm = LeaderboardViewModel(api)
        assertEquals(LoadState.Idle, vm.loadState.value)
    }

    @Test
    fun loadLeaderboard_usesDefaultCallbacks() = runTest {
        coEvery { api.fetchLeaderboard() } returns emptyList()

        val vm = LeaderboardViewModel(
            api = api,
            dispatchers = TestDispatcherProvider(testDispatcher)
        )

        vm.loadLeaderboard()

        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<LeaderboardEntry>(), vm.players.value)
        assertEquals(LoadState.Success, vm.loadState.value)
    }


    // --- SUCCESS BRANCH ---

    @Test
    fun playersFlowUpdatesOnSuccessfulLoad() = runTest {
        val expected = listOf(
            LeaderboardEntry(1, "Alice", 100, 10, 8),
            LeaderboardEntry(2, "Bob", 80, 12, 6)
        )

        coEvery { api.fetchLeaderboard() } returns expected

        val vm = LeaderboardViewModel(
            api = api,
            dispatchers = TestDispatcherProvider(testDispatcher)
        )

        vm.loadLeaderboard(
            onSuccess = {},
            onError = {}
        )

        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(expected, vm.players.value)
        assertEquals(LoadState.Success, vm.loadState.value)
    }

    @Test
    fun loadLeaderboard_callsOnSuccessCallback() = runTest {
        coEvery { api.fetchLeaderboard() } returns emptyList()

        var callbackCalled = false

        val vm = LeaderboardViewModel(
            api = api,
            dispatchers = TestDispatcherProvider(testDispatcher)
        )

        vm.loadLeaderboard(
            onSuccess = { callbackCalled = true },
            onError = {}
        )

        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, callbackCalled)
    }

    // --- ERROR BRANCH ---

    @Test
    fun playersFlowBecomesEmptyOnFailure() = runTest {
        coEvery { api.fetchLeaderboard() } throws RuntimeException()

        val vm = LeaderboardViewModel(
            api = api,
            dispatchers = TestDispatcherProvider(testDispatcher)
        )

        vm.loadLeaderboard(
            onSuccess = {},
            onError = {}
        )

        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<LeaderboardEntry>(), vm.players.value)

        val state = vm.loadState.value as LoadState.Error
        //assertEquals(ErrorCatalog.UNKNOWN, state.message)
    }

    @Test
    fun loadLeaderboard_callsOnErrorCallback() = runTest {
        coEvery { api.fetchLeaderboard() } throws RuntimeException()

        var callbackCalled = false

        val vm = LeaderboardViewModel(
            api = api,
            dispatchers = TestDispatcherProvider(testDispatcher)
        )

        vm.loadLeaderboard(
            onSuccess = {},
            onError = { callbackCalled = true }
        )

        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, callbackCalled)
    }
}
