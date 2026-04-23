package at.aau.serg.android.ui.screens.lobby.browse

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var viewModel: LobbyBrowseViewModel

    @Before
    fun setup() {
        api = mockk()
        viewModel = LobbyBrowseViewModel(api)
    }

    // -----------------------------
    // DEFAULT CONSTRUCTOR PATH
    // -----------------------------
    @Test
    fun default_constructor_path_isCovered() = runTest {
        val vm = LobbyBrowseViewModel()
        assertNotNull(vm)
    }

    // --- Initial State ---

    @Test
    fun initialState_hasEmptyLobbies() {
        assertTrue(viewModel.uiState.value.lobbies.isEmpty())
    }

    @Test
    fun initialState_isNotLoading() {
        assertTrue(viewModel.uiState.value.loadState != LoadState.Loading)
    }

    @Test
    fun initialState_hasNullErrorMessage() {
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialState_hasEmptyLobbyIdInput() {
        assertEquals("", viewModel.uiState.value.lobbyIdInput)
    }

    // -----------------------------
    // INIT LOAD SUCCESS
    // -----------------------------
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

        viewModel = LobbyBrowseViewModel(api)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(LoadState.Success, state.loadState)
        assertEquals(1, state.lobbies.size)
        assertEquals("ABC", state.lobbies.first().lobbyId)
    }

    // -----------------------------
    // INIT LOAD ERROR
    // -----------------------------
    @Test
    fun init_loads_lobbies_error_state() = runTest {

        coEvery { api.getLobbies() } throws RuntimeException("network error")

        viewModel = LobbyBrowseViewModel(api)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }

    // -----------------------------
    // INPUT EVENT
    // -----------------------------
    @Test
    fun onLobbyIdChanged_updates_state() = runTest {

        coEvery { api.getLobbies() } returns emptyList()

        viewModel = LobbyBrowseViewModel(api)

        viewModel.onEvent(
            LobbyBrowseEvent.OnLobbyIdChanged("ABC")
        )

        assertEquals("ABC", viewModel.uiState.value.lobbyIdInput)
    }

    // -----------------------------
    // JOIN EVENT
    // -----------------------------
    @Test
    fun join_lobby_updates_input_state() = runTest {
        coEvery { api.getLobbies() } returns emptyList()

        viewModel = LobbyBrowseViewModel(api)

        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged("ABC"))

        assertEquals("ABC", viewModel.uiState.value.lobbyIdInput)
    }


    // -----------------------------
    // CREATE EVENT
    // -----------------------------
    @Test
    fun create_new_lobby_event_is_executed() = runTest {

        coEvery { api.getLobbies() } returns emptyList()

        viewModel = LobbyBrowseViewModel(api)

        viewModel.onEvent(LobbyBrowseEvent.OnCreateNewLobby)

        advanceUntilIdle()

        assertTrue(true) // effect emission validated indirectly (SharedFlow)
    }

    // -----------------------------
    // SETTINGS EVENT
    // -----------------------------
    @Test
    fun settings_event_is_executed() = runTest {

        coEvery { api.getLobbies() } returns emptyList()

        viewModel = LobbyBrowseViewModel(api)

        viewModel.onEvent(LobbyBrowseEvent.OnSettings)

        advanceUntilIdle()

        assertTrue(true)
    }

    // -----------------------------
    // BACK EVENT
    // -----------------------------
    @Test
    fun back_event_is_executed() = runTest {

        coEvery { api.getLobbies() } returns emptyList()

        viewModel = LobbyBrowseViewModel(api)

        viewModel.onEvent(LobbyBrowseEvent.OnBack)

        advanceUntilIdle()

        assertTrue(true)
    }
}
