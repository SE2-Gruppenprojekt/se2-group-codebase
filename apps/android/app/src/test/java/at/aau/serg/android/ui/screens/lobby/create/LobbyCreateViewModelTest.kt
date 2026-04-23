package at.aau.serg.android.ui.screens.lobby.create

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.response.LobbyResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyCreateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userStore: ProtoStore<User>
    private lateinit var api: LobbyAPI
    private lateinit var viewModel: LobbyCreateViewModel

    private lateinit var user: User

    @Before
    fun setup() {
        user = User.newBuilder()
            .setUid("user-1")
            .setDisplayName("Alice")
            .build()

        userStore = object : ProtoStore<User> {
            override val data = MutableStateFlow(user)

            override suspend fun save(value: User) {
                (data as MutableStateFlow<User>).value = value
            }

            override suspend fun wipe() {
                (data as MutableStateFlow<User>).value = User.getDefaultInstance()
            }
        }

        api = mockk()

        viewModel = LobbyCreateViewModel(
            userStore = userStore,
            api = api
        )
    }

    @Test
    fun default_constructor_path_isCovered() = runTest {
        val vm = LobbyCreateViewModel(userStore)
        assertNotNull(vm)
    }

    @Test
    fun setMaxPlayers_updatesState() = runTest {
        viewModel.onEvent(LobbyCreateEvent.SetMaxPlayers(8))

        assertEquals(8, viewModel.uiState.value.maxPlayers)
    }

    @Test
    fun setIsPrivate_updatesState() = runTest {
        viewModel.onEvent(LobbyCreateEvent.SetIsPrivate(true))

        assertTrue(viewModel.uiState.value.isPrivate)
    }

    @Test
    fun quickMode_and_requireMeld_update_state() = runTest {
        viewModel.onEvent(LobbyCreateEvent.SetQuickMode(true))
        viewModel.onEvent(LobbyCreateEvent.SetRequireInitialMeld(false))

        val state = viewModel.uiState.value

        assertTrue(state.quickMode)
        assertFalse(state.requireInitialMeld)
    }

    @Test
    fun changeTurnTimer_never_goes_below_zero() = runTest {
        viewModel.onEvent(LobbyCreateEvent.ChangeTurnTimer(-999))

        assertEquals(0, viewModel.uiState.value.turnTimer)
    }

    @Test
    fun changeStartingTiles_increases_and_decreases_with_floor() = runTest {
        val initial = viewModel.uiState.value.startingTiles

        viewModel.onEvent(LobbyCreateEvent.ChangeStartingTiles(10))
        assertEquals(initial + 10, viewModel.uiState.value.startingTiles)

        viewModel.onEvent(LobbyCreateEvent.ChangeStartingTiles(-1000))
        assertEquals(0, viewModel.uiState.value.startingTiles)
    }

    @Test
    fun changeWinScore_increases_and_decreases_with_floor() = runTest {
        val initial = viewModel.uiState.value.winScore

        viewModel.onEvent(LobbyCreateEvent.ChangeWinScore(100))
        assertEquals(initial + 100, viewModel.uiState.value.winScore)

        viewModel.onEvent(LobbyCreateEvent.ChangeWinScore(-999999))
        assertEquals(0, viewModel.uiState.value.winScore)
    }

    @Test
    fun createLobby_emitsSuccessState() = runTest {
        val fakeLobby = LobbyResponse(
            lobbyId = "lobby-123",
            hostUserId = "user-1",
            status = "OPEN",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery {
            api.createLobby(
                any(),
                any()
            )
        } returns fakeLobby

        viewModel.onEvent (LobbyCreateEvent.CreateLobby)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals (LoadState.Success, state.loadState)
    }

    @Test
    fun createLobby_emitsErrorState_onFailure() = runTest {
        coEvery { api.createLobby(any(), any()) } throws RuntimeException("Failure")

        viewModel.onEvent(LobbyCreateEvent.CreateLobby)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.loadState is LoadState.Error)
    }
}
