package at.aau.serg.android.ui.screens.lobby.browse

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.response.LobbyListItemResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyBrowseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var api: LobbyAPI
    private lateinit var userStore: ProtoStore<User>
    private lateinit var viewModel: LobbyBrowseViewModel

    @Before
    fun setup() {
        api = mockk()
        userStore = mockk()
        coEvery { api.getLobbies() } returns emptyList()
        viewModel = LobbyBrowseViewModel(userStore, api)
    }

    // --- Initial State ---

    @Test
    fun initialState_hasEmptyLobbies() {
        assertTrue(viewModel.uiState.value.lobbies.isEmpty())
    }

    @Test
    fun initialState_hasNullErrorMessage() {
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialState_hasEmptyLobbyIdInput() {
        assertEquals("", viewModel.uiState.value.lobbyIdInput)
    }

    // --- load lobbies success ---

    @Test
    fun init_loads_lobbies_successfully() = runTest {
        coEvery { api.getLobbies() } returns listOf(
            LobbyListItemResponse(
                lobbyId = "ABC",
                hostUserId = "host",
                status = "OPEN",
                currentPlayerCount = 1,
                maxPlayers = 4,
                isPrivate = false
            )
        )

        viewModel = LobbyBrowseViewModel(userStore, api)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LoadState.Success, state.loadState)
        assertEquals(1, state.lobbies.size)
        assertEquals("ABC", state.lobbies.first().lobbyId)
    }

    // --- load lobbies error ---

    @Test
    fun init_loads_lobbies_error_state() = runTest {
        coEvery { api.getLobbies() } throws RuntimeException("network error")

        viewModel = LobbyBrowseViewModel(userStore, api)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState is LoadState.Error)
    }

    // --- input event ---

    @Test
    fun onLobbyIdChanged_updates_state() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged("ABC"))
        assertEquals("ABC", viewModel.uiState.value.lobbyIdInput)
    }

    // --- effect emissions ---

    @Test
    fun onJoinLobby_emitsNavigateToWaitingRoom() = runTest {
        every { userStore.data } returns flowOf(User.getDefaultInstance())
        coEvery { api.joinLobby(any(), any()) } returns mockk()

        val effectDeferred = async { viewModel.effects.first() }
        viewModel.onEvent(LobbyBrowseEvent.OnJoinLobby("ABC"))
        advanceUntilIdle()

        assertEquals(LobbyBrowseEffect.NavigateToWaitingRoom("ABC"), effectDeferred.await())
    }

    @Test
    fun onCreateNewLobby_emitsNavigateToCreate() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnCreateNewLobby)
        val effect = viewModel.effects.first()
        assertEquals(LobbyBrowseEffect.NavigateToCreate, effect)
    }

    @Test
    fun onSettings_emitsNavigateToSettings() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnSettings)
        val effect = viewModel.effects.first()
        assertEquals(LobbyBrowseEffect.NavigateToSettings, effect)
    }

    @Test
    fun onBack_emitsNavigateBack() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnBack)
        val effect = viewModel.effects.first()
        assertEquals(LobbyBrowseEffect.NavigateBack, effect)
    }
}
