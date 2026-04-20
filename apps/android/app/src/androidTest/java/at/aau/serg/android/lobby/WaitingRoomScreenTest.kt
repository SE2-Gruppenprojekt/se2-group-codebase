package at.aau.serg.android.lobby

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState
import at.aau.serg.android.ui.screens.lobby.main.LobbyViewModel
import at.aau.serg.android.ui.screens.lobby.waiting.WaitingRoomScreen
import at.aau.serg.android.ui.state.LoadState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

// UI-Component tests
class WaitingRoomScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // --- Helpers ---

    private fun testPlayer(
        userId: String = "user1",
        displayName: String = "Alice",
        isReady: Boolean = false
    ) = LobbyPlayer(
        userId = userId,
        displayName = displayName,
        isReady = isReady
    )

    private fun testLobby(
        lobbyId: String = "ABC123",
        hostUserId: String = "user1",
        players: List<LobbyPlayer> = emptyList(),
        maxPlayers: Int = 4
    ) = Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        players = players,
        status = LobbyStatus.OPEN,
        settings = LobbySettings(maxPlayers = maxPlayers)
    )

    private data class MockLobbyViewModelState(
        val viewModel: LobbyViewModel,
        val lobbyFlow: MutableStateFlow<Lobby?>,
        val isDeletedFlow: MutableStateFlow<Boolean>,
        val matchIdFlow: MutableStateFlow<String?>
    )

    private fun mockViewModelState(
        lobby: Lobby? = null,
        isDeleted: Boolean = false,
        matchId: String? = null,
        loadState: LoadState = LoadState.Idle
    ): MockLobbyViewModelState {
        val lobbyFlow = MutableStateFlow(lobby)
        val isDeletedFlow = MutableStateFlow(isDeleted)
        val matchIdFlow = MutableStateFlow(matchId)
        val loadStateFlow = MutableStateFlow(loadState)

        val viewModel = mockk<LobbyViewModel>(relaxed = true) {
            every { this@mockk.lobby } returns lobbyFlow
            every { this@mockk.isDeleted } returns isDeletedFlow
            every { this@mockk.matchId } returns matchIdFlow
            every { this@mockk.loadState } returns loadStateFlow
        }

        return MockLobbyViewModelState(
            viewModel = viewModel,
            lobbyFlow = lobbyFlow,
            isDeletedFlow = isDeletedFlow,
            matchIdFlow = matchIdFlow
        )
    }

    private fun setScreen(
        viewModel: LobbyViewModel,
        lobbyId: String = "ABC123",
        roomCode: String = "",
        onBack: () -> Unit = {},
        onGameStarted: () -> Unit = {}
    ) {
        LobbyUiState.roomCode.value = roomCode
        LobbyUiState.maxPlayers.intValue = 4

        var gameStarted = false

        composeRule.setContent {
            WaitingRoomScreen(
                onBack = onBack,
                onSettings = {},
                onGameStarted = { matchId ->
                    assertTrue(matchId.isNotBlank())
                    gameStarted = true
                },
                lobbyId = lobbyId,
                viewModel = viewModel
            )
        }
    }

    // --- Loading / empty rendering ---

    @Test
    fun loadingState_showsNoPlayers_whenLobbyIsNull() {
        val state = mockViewModelState(lobby = null)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("Alice").assertDoesNotExist()
    }

    // --- Player list rendering ---

    @Test
    fun displaysPlayerName() {
        val lobby = testLobby(players = listOf(testPlayer(displayName = "Alice")))
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    @Test
    fun displaysMultiplePlayers() {
        val lobby = testLobby(
            players = listOf(
                testPlayer(userId = "u1", displayName = "Alice"),
                testPlayer(userId = "u2", displayName = "Bob")
            )
        )
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("Alice").assertIsDisplayed()
        composeRule.onNodeWithText("Bob").assertIsDisplayed()
    }

    // --- Ready state rendering ---

    @Test
    fun displaysReady_whenPlayerIsReady() {
        val lobby = testLobby(players = listOf(testPlayer(isReady = true)))
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("Ready").assertIsDisplayed()
    }

    @Test
    fun displaysNotReady_whenPlayerIsNotReady() {
        val lobby = testLobby(players = listOf(testPlayer(isReady = false)))
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("Not ready").assertIsDisplayed()
    }

    // --- Host marker rendering ---

    @Test
    fun displaysHostBadge_forHostPlayer() {
        val lobby = testLobby(
            hostUserId = "user1",
            players = listOf(testPlayer(userId = "user1", displayName = "Alice"))
        )
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("HOST").assertIsDisplayed()
    }

    @Test
    fun doesNotDisplayHostBadge_forNonHostPlayer() {
        val lobby = testLobby(
            hostUserId = "user1",
            players = listOf(testPlayer(userId = "user2", displayName = "Bob"))
        )
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("HOST").assertDoesNotExist()
    }

    // --- Room code and settings display ---

    @Test
    fun displaysRoomCode() {
        val state = mockViewModelState()

        setScreen(
            viewModel = state.viewModel,
            roomCode = "ABC123"
        )

        composeRule.onNodeWithText("ABC123").assertIsDisplayed()
    }

    @Test
    fun displaysCorrectPlayerCount() {
        val lobby = testLobby(
            players = listOf(
                testPlayer("u1"),
                testPlayer("u2")
            ),
            maxPlayers = 4
        )
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("2/4").assertIsDisplayed()
    }

    // --- Placeholder rendering ---

    @Test
    fun displaysWaitingPlaceholders_forEmptySlots() {
        val lobby = testLobby(
            players = listOf(testPlayer()),
            maxPlayers = 4
        )
        val state = mockViewModelState(lobby = lobby)

        setScreen(viewModel = state.viewModel)

        composeRule
            .onAllNodesWithText("Waiting for player...")[0]
            .assertIsDisplayed()
    }

    // --- State changes after ViewModel updates ---

    @Test
    fun updatesPlayerList_whenLobbyFlowChanges() {
        val state = mockViewModelState(lobby = null)

        setScreen(viewModel = state.viewModel)

        composeRule.onNodeWithText("Alice").assertDoesNotExist()

        composeRule.runOnIdle {
            state.lobbyFlow.value = testLobby(
                players = listOf(testPlayer(displayName = "Alice"))
            )
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    @Test
    fun navigatesBack_whenLobbyIsDeletedAfterUpdate() {
        var backCalled = false
        val state = mockViewModelState(isDeleted = false)

        setScreen(
            viewModel = state.viewModel,
            onBack = { backCalled = true }
        )

        composeRule.runOnIdle {
            state.isDeletedFlow.value = true
        }

        composeRule.waitForIdle()
        assertTrue(backCalled)
    }

    @Test
    fun navigatesToGame_whenMatchIdIsSetAfterUpdate() {
        var gameStarted = false
        val state = mockViewModelState(matchId = null)

        setScreen(
            viewModel = state.viewModel,
            onGameStarted = { gameStarted = true }
        )

        composeRule.runOnIdle {
            state.matchIdFlow.value = "match-1"
        }

        composeRule.waitForIdle()
        assertTrue(gameStarted)
    }
}
