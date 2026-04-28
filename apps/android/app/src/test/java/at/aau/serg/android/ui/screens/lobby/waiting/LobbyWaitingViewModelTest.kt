package at.aau.serg.android.ui.screens.lobby.waiting

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.response.LobbyPlayerResponse
import shared.models.lobby.response.LobbyResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyWaitingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var api: LobbyAPI

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: LobbyWaitingViewModel

    @Before
    fun setup() {
        api = mockk(relaxed = true)

        store = InMemoryProtoStore(User.getDefaultInstance())
        viewModel = LobbyWaitingViewModel(store, api)
    }

    @Test
    fun default_constructor_path_isCovered() = runTest {
        val vm = LobbyWaitingViewModel(store)
        assertNotNull(vm)
    }

    @Test
    fun initial_state_contains_user() = runTest {
        advanceUntilIdle()
        Assert.assertEquals("", viewModel.uiState.value.user?.uid)
    }


    @Test
    fun turnTimer_increase_works() = runTest {
        val before = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        assertTrue(viewModel.uiState.value.turnTimer > before)
    }

    @Test
    fun turnTimer_decrease_works() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        val before = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerDecrease)

        assertTrue(viewModel.uiState.value.turnTimer < before)
    }

    @Test
    fun startMatch_emits_effect() = runTest {
        coEvery { api.startMatch("host", "lobby-123") } returns true

        val job = launch {
            val effect = viewModel.effects.first()
            assertTrue(effect is LobbyWaitingEffect.NavigateToMatch)
        }

        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)

        advanceUntilIdle()
        job.cancel()

        assertTrue(viewModel.uiState.value.loadState == LoadState.Success)
    }

    @Test
    fun startMatch_sets_ErrorState() = runTest {
        coEvery { api.startMatch(any(), any()) } throws RuntimeException("network error")

        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun loadLobby_updates_lobby_and_state() = runTest {
        val lobbyId = "lobby-123"
        val fakeLobby = LobbyResponse(
            lobbyId = lobbyId,
            hostUserId = "user-1",
            status = "OPEN",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery { api.getLobby(any()) } returns fakeLobby

        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby(lobbyId))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.lobby?.lobbyId == lobbyId)
        assertTrue(viewModel.uiState.value.loadState is LoadState.Success)
    }

    @Test
    fun loadLobby_sets_ErrorState() = runTest {
        coEvery { api.getLobby(any()) } throws RuntimeException("network error")

        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby("lobby-1"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun back_emits_effect() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnBack)

        val effect = viewModel.effects.first()

        assertTrue(effect is LobbyWaitingEffect.NavigateBack)
    }

    @Test
    fun settings_emits_effect() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnSettings)

        val effect = viewModel.effects.first()

        assertTrue(effect is LobbyWaitingEffect.NavigateToSettings)
    }


    @Test
    fun onStackToggle_updates_state_correctly() = runTest {
        val current = viewModel.uiState.value.stackEnabled

        viewModel.onEvent(
            LobbyWaitingEvent.OnStackToggle(enabled = !current)
        )

        assertTrue(viewModel.uiState.value.stackEnabled != current)
    }

    @Test
    fun onStartingCardsIncrease_updates_state_correctly() = runTest {
        val current = viewModel.uiState.value.startingCards

        viewModel.onEvent(LobbyWaitingEvent.OnStartingCardsIncrease)

        assertTrue(viewModel.uiState.value.startingCards != current)
    }

    @Test
    fun onStartingCardsDecrease_decrements_bounds() = runTest {
        val amount = viewModel.uiState.value.startingCards

        repeat(amount + 1) {
            viewModel.onEvent(LobbyWaitingEvent.OnStartingCardsDecrease)
        }

        assertTrue(viewModel.uiState.value.startingCards > 0)
    }

    @Test
    fun onturnTimerIncrease_increases() = runTest {
        val current = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        assertTrue(viewModel.uiState.value.turnTimer != current)
    }

    @Test
    fun onturnTimerDecrease_decrements_when_above_one() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        val before = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerDecrease)

        assertTrue(before != viewModel.uiState.value.turnTimer)
    }

    @Test
    fun onturnTimerDecrease_decrements_bounds() = runTest {
        val amount = viewModel.uiState.value.turnTimer

        repeat(amount + 1) {
            viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerDecrease)
        }

        assertTrue(viewModel.uiState.value.turnTimer > 0)
    }

    @Test
    fun toggleReadyState_toggles_current_user() = runTest {
        val userId = "me"
        val lobbyId = "lobby-123"
        val fakeLobby = LobbyResponse(
            lobbyId = lobbyId,
            hostUserId = "user-1",
            status = "OPEN",
            players = listOf(
                LobbyPlayerResponse(
                    userId = userId,
                    displayName = "Bob",
                    isReady = false
                ),
                LobbyPlayerResponse(
                    userId = "user-2",
                    displayName = "Alice",
                    isReady = false
                )
            ),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery { api.getLobby(any()) } returns fakeLobby

        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby(lobbyId))
        advanceUntilIdle()
        val updatedUser = User.newBuilder()
            .setUid(userId)
            .setDisplayName("Bob")
            .build()

        store.save(updatedUser)
        advanceUntilIdle()

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState(userId))

        val updated = viewModel.uiState.value.lobby!!.players.first()

        assertTrue(updated.isReady)
    }

    @Test
    fun toggleReadyState_does_not_affect_other_users() = runTest {
        val userId = "me"
        val lobbyId = "lobby-123"

        val fakeLobby = LobbyResponse(
            lobbyId = lobbyId,
            hostUserId = "user-1",
            status = "OPEN",
            players = listOf(
                LobbyPlayerResponse(
                    userId = userId,
                    displayName = "Bob",
                    isReady = false
                ),
                LobbyPlayerResponse(
                    userId = "user-2",
                    displayName = "Alice",
                    isReady = false
                )
            ),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        coEvery { api.getLobby(any()) } returns fakeLobby

        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby(lobbyId))
        advanceUntilIdle()

        val updatedUser = User.newBuilder()
            .setUid(userId)
            .setDisplayName("Bob")
            .build()

        store.save(updatedUser)
        advanceUntilIdle()

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState("user-2"))
        advanceUntilIdle()

        val alice = viewModel.uiState.value.lobby!!
            .players
            .first { it.userId == "user-2" }

        assertFalse(alice.isReady)
    }
}
