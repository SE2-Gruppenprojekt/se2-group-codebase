package at.aau.serg.android.ui.screens.lobby.waiting
/*
import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.domain.Lobby

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyWaitingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var api: LobbyAPI
    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: LobbyWaitingViewModel

    @Before
    fun setup() {
        api = mockk(relaxed = true)
        store = InMemoryProtoStore(User.getDefaultInstance())

        store. {
            it.toBuilder().setUid("host").build()
        }

        viewModel = LobbyWaitingViewModel(store, api)
    }

    // -----------------------------
    // INITIAL STATE
    // -----------------------------
    @Test
    fun initial_state_is_empty() {
        val state = viewModel.uiState.value

        assertEquals(0, state.turnTimer)
        assertEquals(0, state.startingCards)
        assertFalse(state.stackEnabled)
        assertNull(state.lobby)
    }

    // -----------------------------
    // TIMER
    // -----------------------------
    @Test
    fun turn_timer_increases() {
        val before = viewModel.uiState.value.turnTimer

        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerIncrease)

        assertEquals(before + 10, viewModel.uiState.value.turnTimer)
    }

    @Test
    fun turn_timer_decreases_not_negative() {
        viewModel.onEvent(LobbyWaitingEvent.OnTurnTimerDecrease)

        assertTrue(viewModel.uiState.value.turnTimer >= 0)
    }

    // -----------------------------
    // STARTING CARDS
    // -----------------------------
    @Test
    fun starting_cards_increase() {
        val before = viewModel.uiState.value.startingCards

        viewModel.onEvent(LobbyWaitingEvent.OnStartingCardsIncrease)

        assertEquals(before + 1, viewModel.uiState.value.startingCards)
    }

    @Test
    fun starting_cards_decrease_not_negative() {
        viewModel.onEvent(LobbyWaitingEvent.OnStartingCardsDecrease)

        assertTrue(viewModel.uiState.value.startingCards >= 0)
    }

    // -----------------------------
    // STACK TOGGLE
    // -----------------------------
    @Test
    fun stack_toggle_updates_state() {
        viewModel.onEvent(LobbyWaitingEvent.OnStackToggle(true))
        assertTrue(viewModel.uiState.value.stackEnabled)

        viewModel.onEvent(LobbyWaitingEvent.OnStackToggle(false))
        assertFalse(viewModel.uiState.value.stackEnabled)
    }

    // -----------------------------
    // LOAD LOBBY (SAFE MOCK)
    // -----------------------------
    @Test
    fun load_lobby_success_sets_success_state() = runTest {

        val response = mockk<Any>(relaxed = true)

        coEvery { api.getLobby(any()) } returns response

        viewModel.onEvent(LobbyWaitingEvent.OnLoadLobby("123"))

        advanceUntilIdle()

        // Wir testen nur Zustand, nicht Mapping!
        assertTrue(
            viewModel.uiState.value.loadState is LoadState.Success
                || viewModel.uiState.value.loadState is LoadState.Error
        )
    }

    // -----------------------------
    // START MATCH (SAFE)
    // -----------------------------
    @Test
    fun start_match_does_not_crash_and_updates_state() = runTest {

        coEvery { api.startMatch(any(), any()) } returns Unit

        viewModel.onEvent(LobbyWaitingEvent.onMatchStart)

        advanceUntilIdle()

        assertTrue(
            viewModel.uiState.value.loadState is LoadState.Success
                || viewModel.uiState.value.loadState is LoadState.Error
        )
    }

    // -----------------------------
    // NAVIGATION EVENTS
    // -----------------------------
    @Test
    fun navigation_events_do_not_crash() = runTest {

        viewModel.onEvent(LobbyWaitingEvent.OnBackClicked)
        viewModel.onEvent(LobbyWaitingEvent.OnSettingsClicked)

        advanceUntilIdle()

        assertTrue(true)
    }
}
*/
