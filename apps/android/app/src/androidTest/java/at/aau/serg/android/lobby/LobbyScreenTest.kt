package at.aau.serg.android.lobby

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.lobby.components.LobbyContent
import at.aau.serg.android.ui.screens.lobby.components.LobbyErrorContent
import at.aau.serg.android.ui.state.LoadState
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

// UI-Component tests
class LobbyScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private companion object {
        const val NETWORK_ERROR = "Network error"
        const val NOT_FOUND_ERROR = "Resource not found"
        const val ACCESS_DENIED_ERROR = "Access denied"
        const val LOBBY_ID = "ABC123"
        const val HOST_ID = "user1"
        const val PLAYER_NAME = "Alice"
    }

    private fun testPlayer(
        userId: String = HOST_ID,
        displayName: String = PLAYER_NAME,
        isReady: Boolean = false
    ) = LobbyPlayer(
        userId = userId,
        displayName = displayName,
        isReady = isReady
    )

    private fun testLobby(
        lobbyId: String = LOBBY_ID,
        hostUserId: String = HOST_ID,
        players: List<LobbyPlayer> = listOf(testPlayer()),
        maxPlayers: Int = 4
    ) = Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        players = players,
        status = LobbyStatus.OPEN,
        settings = LobbySettings(maxPlayers = maxPlayers)
    )

    private fun setHomeScreen(state: LoadState) {
        composeRule.setContent {
            HomeScreen(
                state = state,
                onCreateLobby = {},
                onBrowseFancyLobbies = {},
                onShowLeaderboard = {},
                onSettings = {}
            )
        }
    }

    private fun setLobbyContent(lobby: Lobby?) {
        composeRule.setContent {
            LobbyContent(
                lobby = lobby,
                onLeaveLobby = {}
            )
        }
    }

    private fun setLobbyErrorContent(message: String) {
        composeRule.setContent {
            LobbyErrorContent(message = message)
        }
    }

    // --- HomeScreen ---

    @Test
    fun homeScreen_showsLoadingIndicator_whenStateIsLoading() {
        setHomeScreen(state = LoadState.Loading)

        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun homeScreen_hidesLoadingIndicator_whenStateIsIdle() {
        setHomeScreen(state = LoadState.Idle)

        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_showsErrorMessage_whenStateIsError() {
        setHomeScreen(state = LoadState.Error(NETWORK_ERROR))

        composeRule.onNodeWithText(NETWORK_ERROR).assertIsDisplayed()
    }

    @Test
    fun homeScreen_hidesErrorMessage_whenStateIsIdle() {
        setHomeScreen(state = LoadState.Idle)

        composeRule.onNodeWithText(NETWORK_ERROR).assertDoesNotExist()
    }

    // --- LobbyContent ---

    @Test
    fun lobbyContent_showsLoadingText_whenLobbyIsNull() {
        setLobbyContent(lobby = null)

        composeRule.onNodeWithText("Loading lobby…").assertIsDisplayed()
    }

    @Test
    fun lobbyContent_showsProgressIndicator_whenLobbyIsNull() {
        setLobbyContent(lobby = null)

        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun lobbyContent_showsLobbyId_whenLobbyIsLoaded() {
        setLobbyContent(lobby = testLobby())

        composeRule.onNodeWithText("Lobby: $LOBBY_ID").assertIsDisplayed()
    }

    @Test
    fun lobbyContent_showsPlayers_whenLobbyIsLoaded() {
        setLobbyContent(lobby = testLobby())

        composeRule.onNodeWithText(PLAYER_NAME, substring = true).assertIsDisplayed()
    }

    @Test
    fun lobbyContent_hidesLoadingText_whenLobbyIsLoaded() {
        setLobbyContent(lobby = testLobby())

        composeRule.onNodeWithText("Loading lobby…").assertDoesNotExist()
    }

    // --- LobbyErrorContent ---

    @Test
    fun lobbyErrorContent_showsErrorMessage() {
        setLobbyErrorContent(message = NOT_FOUND_ERROR)

        composeRule.onNodeWithText(NOT_FOUND_ERROR).assertIsDisplayed()
    }

    @Test
    fun lobbyErrorContent_showsDifferentMessages() {
        setLobbyErrorContent(message = ACCESS_DENIED_ERROR)

        composeRule.onNodeWithText(ACCESS_DENIED_ERROR).assertIsDisplayed()
    }
}
