package at.aau.serg.android.ui.screens.lobby.waiting

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.*
import io.mockk.just
import io.mockk.Runs
import junit.framework.TestCase.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyWaitingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var api: LobbyAPI
    private lateinit var store: ProtoStore<User>
    private lateinit var userFlow: MutableStateFlow<User>
    private lateinit var viewModel: LobbyWaitingViewModel

    @Before
    fun setup() {
        api = mockk(relaxed = true)

        userFlow = MutableStateFlow(
            User.newBuilder().setUid("host").build()
        )

        store = mockk()
        every { store.data } returns userFlow

        viewModel = LobbyWaitingViewModel(store, api)
    }


    @Test
    fun initial_state_contains_user() = runTest {
        advanceUntilIdle()
        Assert.assertEquals("host", viewModel.uiState.value.user?.uid)
    }


    @Test
    fun turnTimer_increase_works() = runTest {
        val before = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        Assert.assertTrue(viewModel.uiState.value.turnTimer > before)
    }

    @Test
    fun turnTimer_decrease_works() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        val before = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerDecrease)

        Assert.assertTrue(viewModel.uiState.value.turnTimer < before)
    }


    @Test
    fun startMatch_works() = runTest {

        coEvery { api.startMatch(any(), any()) }

        viewModel._uiState.value = viewModel.uiState.value.copy(
            lobby = mockk {
                every { lobbyId } returns "ABC"
            }
        )

        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)

        advanceUntilIdle()

        val effect = viewModel.effects.first()

        assertTrue(effect is LobbyWaitingEffect.NavigateToMatch)
    }

    @Test
    fun back_emits_effect() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnBackClicked)

        val effect = viewModel.effects.first()

        Assert.assertTrue(effect is LobbyWaitingEffect.NavigateBack)
    }

    @Test
    fun settings_emits_effect() = runTest {
        viewModel.onEvent(LobbyWaitingEvent.OnSettingsClicked)

        val effect = viewModel.effects.first()

        Assert.assertTrue(effect is LobbyWaitingEffect.NavigateToSettings)
    }
}
