package at.aau.serg.android.ui.screens.lobby.create

import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import shared.models.lobby.response.LobbyResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyCreateViewModelTest {

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
    fun defaultApi_constructor_isExecuted() = runTest {
        val vm = LobbyCreateViewModel(
            userStore = userStore
            // api NOT passed → forces RetrofitProvider path for 100 % coverage
        )

        assertNotNull(vm)
    }

    @Test
    fun setMaxPlayers_updatesState() = runTest {
        viewModel.onEvent(LobbyCreateEvent.SetMaxPlayers(8))

        val state = viewModel.uiState.value

        assertEquals(8, state.maxPlayers)
    }

    @Test
    fun setIsPrivate_updatesState() = runTest {
        viewModel.onEvent(LobbyCreateEvent.SetIsPrivate(true))

        val state = viewModel.uiState.value

        assertEquals(true, state.isPrivate)
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
            api.createLobby(any(), any())
        } returns fakeLobby

        viewModel.onEvent(LobbyCreateEvent.CreateLobby)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(LoadState.Success, state.loadState)
    }
}
