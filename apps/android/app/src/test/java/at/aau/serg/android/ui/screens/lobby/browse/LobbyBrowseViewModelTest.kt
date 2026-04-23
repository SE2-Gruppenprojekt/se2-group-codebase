package at.aau.serg.android.ui.screens.lobby.browse

import at.aau.serg.android.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyBrowseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LobbyBrowseViewModel

    @Before
    fun setup() {
        viewModel = LobbyBrowseViewModel()
    }

    // --- Initial State ---

    @Test
    fun initialState_hasEmptyLobbies() {
        assertTrue(viewModel.uiState.value.lobbies.isEmpty())
    }

    @Test
    fun initialState_isNotLoading() {
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun initialState_hasNullErrorMessage() {
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialState_hasEmptyLobbyIdInput() {
        assertEquals("", viewModel.uiState.value.lobbyIdInput)
    }

    // --- update() ---

    @Test
    fun update_setsLobbiesList() = runTest {
        val lobbies = listOf(fakeLobbyBrowseItem("lobby-1"), fakeLobbyBrowseItem("lobby-2"))

        viewModel.update(lobbies, isLoading = false, errorMessage = null)

        assertEquals(lobbies, viewModel.uiState.value.lobbies)
    }

    @Test
    fun update_setsIsLoading_true() = runTest {
        viewModel.update(emptyList(), isLoading = true, errorMessage = null)

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun update_setsIsLoading_false() = runTest {
        viewModel.update(emptyList(), isLoading = false, errorMessage = null)

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun update_setsErrorMessage() = runTest {
        viewModel.update(emptyList(), isLoading = false, errorMessage = "Something went wrong")

        assertEquals("Something went wrong", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun update_clearsErrorMessage_whenNull() = runTest {
        viewModel.update(emptyList(), isLoading = false, errorMessage = "error")
        viewModel.update(emptyList(), isLoading = false, errorMessage = null)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun update_doesNotAffectLobbyIdInput() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged("ABC123"))
        viewModel.update(emptyList(), isLoading = true, errorMessage = null)

        assertEquals("ABC123", viewModel.uiState.value.lobbyIdInput)
    }

    // --- OnLobbyIdChanged ---

    @Test
    fun onLobbyIdChanged_updatesLobbyIdInput() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged("XYZ999"))

        assertEquals("XYZ999", viewModel.uiState.value.lobbyIdInput)
    }

    @Test
    fun onLobbyIdChanged_toEmpty_clearsInput() = runTest {
        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged("FILLED"))
        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged(""))

        assertEquals("", viewModel.uiState.value.lobbyIdInput)
    }

    @Test
    fun onLobbyIdChanged_doesNotEmitEffect() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnLobbyIdChanged("ABC"))
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.isEmpty())
    }

    // --- OnJoinLobby ---

    @Test
    fun onJoinLobby_emitsJoinLobbyEffect() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnJoinLobby("lobby-42"))
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, effects.size)
        assertEquals(LobbyBrowseEffect.JoinLobby("lobby-42"), effects.first())
    }

    @Test
    fun onJoinLobby_carriesCorrectLobbyId() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnJoinLobby("special-id"))
        advanceUntilIdle()
        job.cancel()

        val effect = effects.first() as LobbyBrowseEffect.JoinLobby
        assertEquals("special-id", effect.lobbyId)
    }

    @Test
    fun onJoinLobby_doesNotChangeUiState() = runTest {
        val stateBefore = viewModel.uiState.value

        viewModel.onEvent(LobbyBrowseEvent.OnJoinLobby("lobby-1"))
        advanceUntilIdle()

        assertEquals(stateBefore, viewModel.uiState.value)
    }

    // --- OnCreateNewLobby ---

    @Test
    fun onCreateNewLobby_emitsNavigateToCreateEffect() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnCreateNewLobby)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, effects.size)
        assertEquals(LobbyBrowseEffect.NavigateToCreate, effects.first())
    }

    @Test
    fun onCreateNewLobby_doesNotChangeUiState() = runTest {
        val stateBefore = viewModel.uiState.value

        viewModel.onEvent(LobbyBrowseEvent.OnCreateNewLobby)
        advanceUntilIdle()

        assertEquals(stateBefore, viewModel.uiState.value)
    }

    // --- OnSettings ---

    @Test
    fun onSettings_emitsNavigateToSettingsEffect() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnSettings)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, effects.size)
        assertEquals(LobbyBrowseEffect.NavigateToSettings, effects.first())
    }

    @Test
    fun onSettings_doesNotChangeUiState() = runTest {
        val stateBefore = viewModel.uiState.value

        viewModel.onEvent(LobbyBrowseEvent.OnSettings)
        advanceUntilIdle()

        assertEquals(stateBefore, viewModel.uiState.value)
    }

    // --- OnBack ---

    @Test
    fun onBack_emitsNavigateBackEffect() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnBack)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, effects.size)
        assertEquals(LobbyBrowseEffect.NavigateBack, effects.first())
    }

    @Test
    fun onBack_doesNotChangeUiState() = runTest {
        val stateBefore = viewModel.uiState.value

        viewModel.onEvent(LobbyBrowseEvent.OnBack)
        advanceUntilIdle()

        assertEquals(stateBefore, viewModel.uiState.value)
    }

    // --- Multiple Events ---

    @Test
    fun multipleEvents_eachEmitsOwnEffect() = runTest {
        val effects = mutableListOf<LobbyBrowseEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(LobbyBrowseEvent.OnBack)
        viewModel.onEvent(LobbyBrowseEvent.OnSettings)
        viewModel.onEvent(LobbyBrowseEvent.OnCreateNewLobby)
        advanceUntilIdle()
        job.cancel()

        assertEquals(3, effects.size)
        assertTrue(effects.contains(LobbyBrowseEffect.NavigateBack))
        assertTrue(effects.contains(LobbyBrowseEffect.NavigateToSettings))
        assertTrue(effects.contains(LobbyBrowseEffect.NavigateToCreate))
    }

    // --- Helpers ---

    private fun fakeLobbyBrowseItem(id: String) = LobbyBrowseItem(
        lobbyId = id,
        hostId = "host-$id",
        currentPlayers = 1,
        maxPlayers = 4,
        turnTimerSeconds = 60,
        startingCards = 7,
        isOpen = true,
        accentColor = androidx.compose.ui.graphics.Color.Blue
    )
}
