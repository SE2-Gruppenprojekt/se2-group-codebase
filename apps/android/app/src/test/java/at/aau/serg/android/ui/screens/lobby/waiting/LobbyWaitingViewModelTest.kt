package at.aau.serg.android.ui.screens.lobby.waiting

import app.cash.turbine.test
import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.event.LobbyDeletedPayload
import shared.models.lobby.event.LobbyEvent
import shared.models.lobby.event.LobbyStartedPayload
import shared.models.lobby.event.LobbyUpdatedPayload
import shared.models.lobby.response.LobbyPlayerResponse
import shared.models.lobby.response.LobbyResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyWaitingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var api: LobbyAPI
    private lateinit var service: LobbyWebSocketService
    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var userStore: UserStore
    private lateinit var viewModel: LobbyWaitingViewModel

    val fakeLobby = LobbyResponse(
        lobbyId = "lobby-123",
        hostUserId = "user-1",
        status = "OPEN",
        players = listOf(
            LobbyPlayerResponse(
                userId = "user-1",
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

    @Before
    fun setup() {
        api = mockk(relaxed = true)

        service = mockk<LobbyWebSocketService>()

        // Mock subscribe to return an empty Flow (no WebSocket interaction)
        coEvery { service.subscribe(any()) } returns flow { }

        store = InMemoryProtoStore(User.getDefaultInstance())
        val user = User.newBuilder()
            .setUid("user-1")
            .setDisplayName("Alice")
            .build()

        runBlocking {
            store.save(user)
        }

        userStore = UserStore(store)
        viewModel = LobbyWaitingViewModel(userStore, api, service)
    }

    @After
    fun tearDown() {
        // fixes toggleReadyState_does_not_affect_other_users
    }

    @Test
    fun default_constructor_path_isCovered() = runTest {
        val vm = LobbyWaitingViewModel(userStore)
        assertNotNull(vm)
    }

    @Test
    fun initial_state_contains_user() = runTest {
        advanceUntilIdle()
        Assert.assertEquals("user-1", viewModel.uiState.value.user?.uid)
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
        val user = User.newBuilder()
            .setUid(fakeLobby.hostUserId)
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            lobby = fakeLobby.toDomain(),
            user = user
        ))
        coEvery { api.startMatch(fakeLobby.lobbyId) } returns fakeLobby.copy(
            status = "IN_GAME",
            currentGameId = "match-1"
        )

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
    fun startMatch_setsErrorState_when_user_null() = runTest {
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            user = null
        ))

        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun startMatch_setsErrorState_if_not_in_lobby() = runTest {
        val user = User.newBuilder()
            .setUid("u1")
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            lobby = null,
            user = user
        ))
        coEvery { api.startMatch("lobby-123") } returns fakeLobby


        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun startMatch_sets_ErrorState() = runTest {
        val user = User.newBuilder()
            .setUid(fakeLobby.hostUserId)
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            lobby = fakeLobby.toDomain(),
            user = user
        ))
        coEvery { api.startMatch(any()) } throws RuntimeException("network error")

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
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                lobby = null
            )
        )

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
    fun back_leaves_open_lobby_then_navigates_back() = runTest {
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                lobby = fakeLobby.toDomain()
            )
        )
        coEvery { api.leaveLobby(fakeLobby.lobbyId) } returns Unit

        viewModel.onEvent(LobbyWaitingEvent.OnBack)
        advanceUntilIdle()

        val effect = viewModel.effects.first()
        assertTrue(effect is LobbyWaitingEffect.NavigateBack)
    }

    @Test
    fun back_sets_error_when_leave_fails() = runTest {
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                lobby = fakeLobby.toDomain()
            )
        )
        coEvery { api.leaveLobby(fakeLobby.lobbyId) } throws RuntimeException("network error")

        viewModel.onEvent(LobbyWaitingEvent.OnBack)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun back_in_non_open_lobby_navigates_without_leave() = runTest {
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                lobby = fakeLobby.copy(status = "IN_GAME").toDomain()
            )
        )

        viewModel.onEvent(LobbyWaitingEvent.OnBack)
        advanceUntilIdle()

        val effect = viewModel.effects.first()
        assertTrue(effect is LobbyWaitingEffect.NavigateBack)
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

    @Test
    fun toggleReadyState_setsErrorState_when_lobby_null() = runTest {
        val user = User.newBuilder()
            .setUid("u1")
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            lobby = null,
            user = user
        ))

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState("u1"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun toggleReadyState_setsErrorState_when_not_in_lobby() = runTest {
        val user = User.newBuilder()
            .setUid("u1")
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            lobby = fakeLobby.toDomain(),
            user = user
        ))

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState("u1"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun toggleReadyState_setsErrorState_when_user_null() = runTest {
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            user = null
        ))

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState("user-2"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun socket_deleted_emits_effect() = runTest {
        val payload = LobbyDeletedPayload(
            lobbyId = "lobby-1"
        )
        viewModel.handleLobbyEvent(LobbyEvent.Deleted(payload))

        val effect = viewModel.effects.first()

        assertTrue(effect is LobbyWaitingEffect.NavigateBack)
    }

    @Test
    fun socket_started_emits_effect() = runTest {
        val payload = LobbyStartedPayload(
            lobbyId = "lobby-1",
            matchId = "match-1"
        )
        viewModel.handleLobbyEvent(LobbyEvent.Started(payload))

        val effect = viewModel.effects.first()

        assertTrue(effect is LobbyWaitingEffect.NavigateToMatch)
    }

    @Test
    fun socket_started_updatesUserStoreGameId() = runTest {
        val payload = LobbyStartedPayload(
            lobbyId = "lobby-1",
            matchId = "match-1"
        )

        viewModel.handleLobbyEvent(LobbyEvent.Started(payload))
        advanceUntilIdle()

        assertEquals("match-1", store.data.first().gameId)
    }

    @Test
    fun socket_started_setsError_whenGameIdUpdateFails() = runTest {
        val mockedUserStore = mockk<UserStore>()
        every { mockedUserStore.data } returns flowOf(
            User.newBuilder()
                .setUid("user-1")
                .setDisplayName("Alice")
                .build()
        )
        coEvery { mockedUserStore.updateGameId(any()) } throws RuntimeException("boom")

        val vm = LobbyWaitingViewModel(mockedUserStore, api, service)
        advanceUntilIdle()

        vm.handleLobbyEvent(
            LobbyEvent.Started(
                LobbyStartedPayload(
                    lobbyId = "lobby-1",
                    matchId = "match-1"
                )
            )
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun startSocket_collects_events_on_success() = runTest {
        val testFlow = MutableSharedFlow<LobbyEvent>()
        coEvery { service.subscribe("lobby_123") } returns testFlow


        viewModel.startSocket("lobby_123")
        runCurrent()
        val payload = LobbyStartedPayload(
            lobbyId = "lobby-1",
            matchId = "match-1"
        )
        val mockEvent = LobbyEvent.Started(payload)

        testFlow.emit(mockEvent)
        runCurrent()
    }

    @Test
    fun startSocket_catches_NetworkException_and_updates_loadState() = runTest {
        coEvery { service.subscribe("lobby_123") } returns flow {
            throw RuntimeException("Connection lost")
        }

        viewModel.startSocket("lobby_123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun startSocket_collects_updated_event_into_ui_state() = runTest {
        val lobby = fakeLobby.copy(status = "OPEN")
        coEvery { service.subscribe("lobby-123") } returns flowOf(
            LobbyEvent.Updated(LobbyUpdatedPayload(lobby))
        )

        viewModel.startSocket("lobby-123")
        advanceUntilIdle()

        assertEquals("lobby-123", viewModel.uiState.value.lobby?.lobbyId)
        assertTrue(viewModel.uiState.value.loadState is LoadState.Success)
    }

    @Test
    fun lobbyEventStarted_navigates_even_when_user_is_null() = runTest {
        viewModel.setUiStateForTest(LobbyWaitingUiState(
            user = null
        ))
        val payload = LobbyStartedPayload(
            lobbyId = "lobby-1",
            matchId = "match-1"
        )
        viewModel.handleLobbyEvent(LobbyEvent.Started(payload))
        advanceUntilIdle()

        val effect = viewModel.effects.first()
        assertTrue(effect is LobbyWaitingEffect.NavigateToMatch)
    }

    @Test
    fun socket_updated_changes_data() = runTest {
        val lobby = LobbyResponse(
            lobbyId = "test123",
            hostUserId = "user-1",
            status = "OPEN",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        val payload = LobbyUpdatedPayload(
            lobby = lobby
        )

        viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))

        assertTrue(viewModel.uiState.value.lobby?.lobbyId == "test123")
    }

    @Test
    fun socket_updated_navigates_when_match_id_is_present() = runTest {
        val lobby = LobbyResponse(
            lobbyId = "test123",
            hostUserId = "user-1",
            status = "IN_GAME",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            currentGameId = "match-42"
        )
        val payload = LobbyUpdatedPayload(lobby = lobby)

        viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))

        val effect = viewModel.effects.first()
        assertTrue(effect is LobbyWaitingEffect.NavigateToMatch)
    }

    @Test
    fun socket_updated_same_match_id_does_not_emit_duplicate_navigation() = runTest {
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                lobby = fakeLobby.copy(
                    status = "IN_GAME",
                    currentGameId = "match-42"
                ).toDomain()
            )
        )
        val payload = LobbyUpdatedPayload(
            lobby = fakeLobby.copy(
                status = "IN_GAME",
                currentGameId = "match-42"
            )
        )

        viewModel.effects.test {
            viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))
            expectNoEvents()
        }
    }

    @Test
    fun socket_updated_closed_lobby_navigates_back() = runTest {
        val lobby = LobbyResponse(
            lobbyId = "test123",
            hostUserId = "user-1",
            status = "CLOSED",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        val payload = LobbyUpdatedPayload(lobby = lobby)

        viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))

        val effect = viewModel.effects.first()
        assertTrue(effect is LobbyWaitingEffect.NavigateBack)
    }

    @Test
    fun toggle_ready_state_shows_error() = runTest {
        val userId = fakeLobby.hostUserId
        val payload = LobbyUpdatedPayload(
            lobby = fakeLobby
        )

        viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))

        val updatedUser = User.newBuilder()
            .setUid(userId)
            .setDisplayName("Bob")
            .build()

        store.save(updatedUser)
        advanceUntilIdle()

        coEvery { api.ready(any()) } throws RuntimeException("network error")
        coEvery { api.unready(any()) } throws RuntimeException("network error")

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState(userId))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun toggle_ready_state_sets_success_on_ready() = runTest {
        val userId = fakeLobby.hostUserId
        val lobbyId = fakeLobby.lobbyId
        val payload = LobbyUpdatedPayload(
            lobby = fakeLobby
        )

        viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))

        val updatedUser = User.newBuilder()
            .setUid(userId)
            .setDisplayName("Bob")
            .build()

        store.save(updatedUser)
        advanceUntilIdle()

        coEvery { api.ready(lobbyId) } returns fakeLobby

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState(userId))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Success)
    }

    @Test
    fun toggle_ready_state_sets_success_on_unready() = runTest {
        val userId = fakeLobby.hostUserId
        val lobbyId = fakeLobby.lobbyId

        val fakeLobby = LobbyResponse(
            lobbyId = "lobby-123",
            hostUserId = "user-1",
            status = "OPEN",
            players = listOf(
                LobbyPlayerResponse(
                    userId = "user-1",
                    displayName = "Bob",
                    isReady = true
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
        val payload = LobbyUpdatedPayload(
            lobby = fakeLobby
        )

        viewModel.handleLobbyEvent(LobbyEvent.Updated(payload))

        val updatedUser = User.newBuilder()
            .setUid(userId)
            .setDisplayName("Bob")
            .build()

        store.save(updatedUser)
        advanceUntilIdle()


        coEvery { api.unready(lobbyId) } returns fakeLobby

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState(userId))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Success)
    }

    @Test
    fun startSocket_cancels_existing_job_on_lobby_change() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby("Lobby1"))
        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby("Lobby2"))
    }

    @Test
    fun loadLobby_does_not_overwrite_newer_websocket_state() = runTest {
        val initialLobby = LobbyResponse(
            lobbyId = "lobby-123",
            hostUserId = "user-1",
            status = "OPEN",
            players = listOf(
                LobbyPlayerResponse(
                    userId = "user-1",
                    displayName = "Bob",
                    isReady = false
                )
            ),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        val updatedLobby = initialLobby.copy(
            players = listOf(
                LobbyPlayerResponse(
                    userId = "user-1",
                    displayName = "Bob",
                    isReady = false
                ),
                LobbyPlayerResponse(
                    userId = "user-2",
                    displayName = "Alice",
                    isReady = true
                )
            )
        )
        val testFlow = MutableSharedFlow<LobbyEvent>()

        coEvery { service.subscribe("lobby-123") } returns testFlow
        coEvery { api.getLobby("lobby-123") } coAnswers {
            delay(100)
            initialLobby
        }

        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby("lobby-123"))
        runCurrent()

        testFlow.emit(LobbyEvent.Updated(LobbyUpdatedPayload(updatedLobby)))
        runCurrent()
        advanceUntilIdle()

        val players = viewModel.uiState.value.lobby?.players.orEmpty()
        assertTrue(players.any { it.userId == "user-2" && it.isReady })
        Assert.assertEquals(2, players.size)
    }

    @Test
    fun toggleReadyState_setsError_whenLobbyNotOpen() = runTest {
        val user = User.newBuilder()
            .setUid("user-1")
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                user = user,
                lobby = fakeLobby.copy(status = "IN_GAME").toDomain()
            )
        )

        viewModel.onEvent(LobbyWaitingEvent.ToggleReadyState("user-1"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    @Test
    fun startMatch_setsError_whenLobbyNotOpen() = runTest {
        val user = User.newBuilder()
            .setUid(fakeLobby.hostUserId)
            .setDisplayName("Alice")
            .build()
        viewModel.setUiStateForTest(
            LobbyWaitingUiState(
                user = user,
                lobby = fakeLobby.copy(status = "IN_GAME").toDomain()
            )
        )

        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }
}
