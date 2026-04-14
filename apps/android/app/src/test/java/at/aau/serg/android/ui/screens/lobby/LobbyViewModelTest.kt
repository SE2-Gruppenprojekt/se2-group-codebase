package at.aau.serg.android.ui.screens.lobby

import at.aau.serg.android.network.lobby.LobbyAPI
import at.aau.serg.android.network.lobby.LobbyWebSocketService
import at.aau.serg.android.ui.lobby.LobbiesUiState
import at.aau.serg.android.ui.lobby.LobbyUiStateLoading
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import shared.models.lobby.request.CreateLobbyRequest
import shared.models.lobby.response.LobbyResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyViewModelTest {

    private val api = mockk<LobbyAPI>()
    private val webSocket = mockk<LobbyWebSocketService>()
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

    private fun createVM(): LobbyViewModel =
        LobbyViewModel(api, webSocket, TestDispatcherProvider(testDispatcher))


    // --- DEFAULT CONSTRUCTOR COVERAGE ---

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

}
