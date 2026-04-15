package at.aau.serg.android.ui.screens.lobby

import android.util.Log
import at.aau.serg.android.data.lobby.mapper.toDomain
import at.aau.serg.android.network.lobby.LobbyAPI
import at.aau.serg.android.network.lobby.LobbyDeletedPayload
import at.aau.serg.android.network.lobby.LobbyStartedPayload
import at.aau.serg.android.network.lobby.LobbyUpdatedPayload
import at.aau.serg.android.network.lobby.LobbyWebSocketService
import at.aau.serg.android.ui.lobby.LobbiesUiState
import at.aau.serg.android.ui.lobby.LobbyUiStateLoading
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.util.DispatcherProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import shared.models.lobby.request.CreateLobbyRequest
import shared.models.lobby.request.JoinLobbyRequest
import shared.models.lobby.response.LobbyListItemResponse
import shared.models.lobby.response.LobbyPlayerResponse
import shared.models.lobby.response.LobbyResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyViewModelTest {

    private val api = mockk<LobbyAPI>()
    private val webSocket = mockk<LobbyWebSocketService>()
    private val testDispatcher = StandardTestDispatcher()

    // Test dispatcher provider
    private class TestDispatcherProvider(
        dispatcher: CoroutineDispatcher
    ) : DispatcherProvider {
        override val main = dispatcher
        override val io = dispatcher
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Before
    fun mockLog() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVM(): LobbyViewModel =
        LobbyViewModel(api, webSocket, TestDispatcherProvider(testDispatcher))


    // --- DEFAULT CONSTRUCTOR COVERAGE ---

    @Test
    fun defaultConstructor_initializesRetrofitApi() = runTest {
        val vm = LobbyViewModel(
            dispatchers = TestDispatcherProvider(testDispatcher)
        )
        assertEquals(LoadState.Idle, vm.loadState.value)
    }

    @Test
    fun defaultDispatcherProvider_isUsedWhenNoDispatcherIsPassed() = runTest {
        val vm = LobbyViewModel(api)
        assertEquals(LoadState.Idle, vm.loadState.value)
    }
    @Test
    fun initialState_isCorrect() {
        val vm = createVM()

        assertEquals(LobbyUiStateLoading.Loading, vm.state.value)
        assertEquals(LobbiesUiState.Loading, vm.lobbiesState.value)
        assertNull(vm.lobby.value)
        assertTrue(vm.lobbies.value.isEmpty())
        assertFalse(vm.isDeleted.value)
        assertNull(vm.matchId.value)
    }

    // --- CREATE LOBBY ---
    @Test
    fun createLobby_success() = runTest {
        val response = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = emptyList(),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        coEvery {
            api.createLobby(any(), any<CreateLobbyRequest>())
        } returns response

        var callbackCalled = false

        val vm = createVM()

        vm.createLobby(
            userId = "host1",
            displayName = "Player1",
            onSuccess = { callbackCalled = true }
        )

        testDispatcher.scheduler.runCurrent()

        assertTrue(vm.state.value is LobbyUiStateLoading.Success)
        assertTrue(callbackCalled)
    }

    @Test
    fun createLobby_error() = runTest {
        coEvery { api.createLobby(any(), any<CreateLobbyRequest>()) } throws RuntimeException()

        var callbackCalled = false

        val vm = createVM()

        vm.createLobby(onError = { callbackCalled = true })

        testDispatcher.scheduler.runCurrent()

        assertTrue(vm.state.value is LobbyUiStateLoading.Error)
        assertTrue(callbackCalled)
    }

    // --- BROWSE LOBBIES ---
    @Test
    fun loadLobbies_success() = runTest {
        val list = listOf(
            LobbyListItemResponse(
                lobbyId = "1",
                hostUserId = "host",
                status = "OPEN",
                currentPlayerCount = 1,
                maxPlayers = 1,
                isPrivate = false)
        )

        coEvery { api.getLobbies() } returns list
        val vm = createVM()
        vm.loadLobbies()
        testDispatcher.scheduler.runCurrent()

        assertEquals(list, vm.lobbies.value)
        assertTrue(vm.lobbiesState.value is LobbiesUiState.Success)
    }

    @Test
    fun loadLobbies_error() = runTest {
        coEvery { api.getLobbies() } throws RuntimeException()
        var callbackCalled = false
        val vm = createVM()
        vm.loadLobbies { callbackCalled = true }
        testDispatcher.scheduler.runCurrent()

        assertTrue(vm.lobbiesState.value is LobbiesUiState.Error)
        assertTrue(callbackCalled)
    }

    @Test
    fun loadLobby_success() = runTest {
        val response = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = emptyList(),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        coEvery { api.getLobby("123") } returns response
        val vm = createVM()
        vm.loadLobby("123")
        testDispatcher.scheduler.runCurrent()

        val state = vm.state.value
        assertTrue(state is LobbyUiStateLoading.Success)

        val lobby = vm.lobby.value
        assertEquals("123", lobby?.lobbyId)
    }

    @Test
    fun loadLobby_error() = runTest {
        coEvery { api.getLobby("123") } throws RuntimeException()
        val vm = createVM()
        vm.loadLobby("123")
        testDispatcher.scheduler.runCurrent()

        assertTrue(vm.state.value is LobbyUiStateLoading.Error)
    }

    // --- JOIN LOBBY ---
    @Test
    fun joinLobby_success() = runTest {
        val request = JoinLobbyRequest(
            userId = "111",
            displayName="host1"
        )
        val response = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = emptyList(),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        coEvery { api.joinLobby("123", request) } returns response
        var callbackCalled = false
        val vm = createVM()
        vm.joinLobby(
            lobbyId = "123",
            userId = "111",
            displayName = "host1",
            onSuccess = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)
    }

    @Test
    fun joinLobby_error() = runTest {
        val request = JoinLobbyRequest(
            userId = "111",
            displayName="host1"
        )

        coEvery { api.joinLobby("123", request) } throws RuntimeException()
        var callbackCalled = false
        val vm = createVM()
        vm.joinLobby(
            lobbyId = "123",
            userId = "111",
            displayName = "host1",
            onError = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)
    }

    @Test
    fun joinLobbyOrOpen_userAlreadyInLobby() = runTest {
        val response = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = listOf(
                LobbyPlayerResponse(
                    userId = "USER",
                    displayName = "host1",
                    isReady = false
                )
            ),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        coEvery { api.getLobby("123") } returns response
        var callbackCalled = false
        val vm = createVM()
        vm.joinLobbyOrOpen("123", userId = "USER", onSuccess = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)
        assertTrue(vm.lobby.value?.players?.any { it.userId == "USER" } == true)
        coVerify(exactly = 0) { api.joinLobby(any(), any()) }
    }

    @Test
    fun joinLobbyOrOpen_userNotInLobby() = runTest {
        val response = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = listOf(
                LobbyPlayerResponse(
                    userId = "host1",
                    displayName = "Host",
                    isReady = false
                )
            ),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        val joinedResponse = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = response.players + LobbyPlayerResponse(
                userId = "USER",
                displayName = "Me",
                isReady = false
            ),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        coEvery { api.getLobby("123") } returns response
        coEvery { api.joinLobby(eq("123"), any()) } returns joinedResponse

        var callbackCalled = false
        val vm = createVM()
        vm.joinLobbyOrOpen("123", userId = "USER", onSuccess = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)

        val lobby = vm.lobby.value
        assertNotNull(lobby)
        assertTrue(lobby!!.players.any { it.userId == "USER" })
    }

    @Test
    fun joinLobbyOrOpen_error() = runTest {
        coEvery { api.getLobby("123") } throws RuntimeException()
        var callbackCalled = false
        val vm = createVM()
        vm.joinLobbyOrOpen("123", onError = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)
    }

    @Test
    fun leaveLobby_success() = runTest {
        coEvery { api.leaveLobby("USER", "123") } returns true
        var callbackCalled = false
        val vm = createVM()
        vm.leaveLobby("123", userId = "USER", onSuccess = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)
    }

    @Test
    fun leaveLobby_error() = runTest {
        coEvery { api.leaveLobby("USER", "123") } throws RuntimeException()
        var callbackCalled = false
        val vm = createVM()
        vm.leaveLobby("123", userId = "USER", onError = { callbackCalled = true })
        testDispatcher.scheduler.runCurrent()

        assertTrue(callbackCalled)
    }

    // --- WEBSOCKET  ---
    @Test
    fun connectWebSocket_OnLobbyUpdated() = runTest {
        val vm = createVM()
        val response = LobbyResponse(
            lobbyId = "123",
            hostUserId = "host1",
            players = emptyList(),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery {
            webSocket.connect(
                lobbyId = "123",
                onLobbyUpdated = captureLambda(),
                onLobbyDeleted = any(),
                onLobbyStarted = any()
            )
        } coAnswers {
            lambda<(LobbyUpdatedPayload) -> Unit>().invoke(
                LobbyUpdatedPayload(
                    type = "lobby.update",
                    lobby = response
                )
            )
        }
        vm.connectWebSocket("123")
        testDispatcher.scheduler.runCurrent()

        val expected = response.toDomain()
        assertEquals(expected, vm.lobby.value)
    }

    @Test
    fun connectWebSocket_OnLobbyDeleted() = runTest {
        val vm = createVM()
        coEvery {
            webSocket.connect(
                lobbyId = "123",
                onLobbyUpdated = any(),
                onLobbyDeleted = captureLambda(),
                onLobbyStarted = any()
            )
        } coAnswers {
            lambda<(LobbyDeletedPayload) -> Unit>().invoke(
                LobbyDeletedPayload(
                    type = "lobby.deleted",
                    lobbyId = "123"
                )
            )
        }
        vm.connectWebSocket("123")
        testDispatcher.scheduler.runCurrent()

        assertTrue(vm.isDeleted.value)
    }

    @Test
    fun connectWebSocket_setsMatchIdOnLobbyStarted() = runTest {
        val vm = createVM()
        coEvery {
            webSocket.connect(
                lobbyId = "123",
                onLobbyUpdated = any(),
                onLobbyDeleted = any(),
                onLobbyStarted = captureLambda()
            )
        } coAnswers {
            lambda<(LobbyStartedPayload) -> Unit>().invoke(
                LobbyStartedPayload(
                    type = "lobby.started",
                    lobbyId = "123",
                    matchId = "MATCH42"
                )
            )
        }
        vm.connectWebSocket("123")
        testDispatcher.scheduler.runCurrent()

        assertEquals("MATCH42", vm.matchId.value)
    }

    @Test
    fun connectWebSocket_handlesException() = runTest {
        val vm = createVM()
        coEvery { webSocket.connect(any(), any(), any(), any()) } throws RuntimeException()
        vm.connectWebSocket("123")
        testDispatcher.scheduler.runCurrent()

        // No crash, state unchanged
        assertNull(vm.lobby.value)
        assertFalse(vm.isDeleted.value)
        assertNull(vm.matchId.value)
    }

    @Test
    fun onCleared_cancelsWebSocketJobAndDisconnects() = runTest {
        val vm = createVM()
        // requires 'just Runs' because disconnect is a suspended function
        coEvery { webSocket.disconnect() } just Runs
        vm.connectWebSocket("123")
        testDispatcher.scheduler.runCurrent()
        // Android sets onCleared as protected so we can't access it directly
        val method = androidx.lifecycle.ViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(vm)

        testDispatcher.scheduler.runCurrent()
        coVerify { webSocket.disconnect() }
    }
}
