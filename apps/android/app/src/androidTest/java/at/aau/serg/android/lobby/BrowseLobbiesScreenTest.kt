package at.aau.serg.android.lobby

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseItem
//import at.aau.serg.android.ui.screens.browselobbies.components.BrowsingLobbiesScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// UI-Component tests
/*class BrowseLobbiesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun testLobby(
        id: String = "ABC123",
        currentPlayers: Int = 2,
        maxPlayers: Int = 4,
        turnTimerSeconds: Int = 60,
        startingCards: Int = 14,
        isOpen: Boolean = true
    ) = LobbyBrowseItem(
        lobbyId = id,
        hostId = "host1",
        currentPlayers = currentPlayers,
        maxPlayers = maxPlayers,
        turnTimerSeconds = turnTimerSeconds,
        startingCards = startingCards,
        isOpen = isOpen,
        accentColor = Color.Blue
    )

    private fun setScreen(
        lobbies: List<LobbyBrowseItem> = emptyList(),
        isLoading: Boolean = false,
        errorMessage: String? = null,
        onJoinLobby: (String) -> Unit = {},
        onCreateNewLobby: () -> Unit = {},
        onSettings: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeRule.setContent {
            BrowsingLobbiesScreen(
                lobbies = lobbies,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onJoinLobby = onJoinLobby,
                onCreateNewLobby = onCreateNewLobby,
                onSettings = onSettings,
                onBack = onBack
            )
        }
    }

    // --- Lobby entries render correctly ---

    @Test
    fun displaysLobbyId() {
        setScreen(lobbies = listOf(testLobby(id = "ABC123")))
        composeRule.onNodeWithText("#ABC123").assertIsDisplayed()
    }

    @Test
    fun displaysTurnTimer() {
        setScreen(lobbies = listOf(testLobby(turnTimerSeconds = 60)))
        composeRule.onNodeWithText("60s").assertIsDisplayed()
    }

    @Test
    fun displaysStartingCards() {
        setScreen(lobbies = listOf(testLobby(startingCards = 14)))
        composeRule.onNodeWithText("14 cards").assertIsDisplayed()
    }

    @Test
    fun displaysPlayerCount() {
        setScreen(lobbies = listOf(testLobby(currentPlayers = 2, maxPlayers = 4)))
        composeRule.onNodeWithText("2/4").assertIsDisplayed()
    }

    @Test
    fun displaysJoinButton_whenLobbyIsOpen() {
        setScreen(lobbies = listOf(testLobby(isOpen = true)))
        composeRule.onNodeWithText("Join").assertIsDisplayed()
    }

    @Test
    fun displaysFullButton_whenLobbyIsFull() {
        setScreen(lobbies = listOf(testLobby(isOpen = false)))
        composeRule.onNodeWithText("Full").assertIsDisplayed()
    }

    @Test
    fun displaysMultipleLobbies() {
        setScreen(lobbies = listOf(testLobby("AAA"), testLobby("BBB")))
        composeRule.onNodeWithText("#AAA").assertIsDisplayed()
        composeRule.onNodeWithText("#BBB").assertIsDisplayed()
    }

    // --- Tapping a lobby triggers join ---

    @Test
    fun clickingJoin_callsOnJoinLobby_withCorrectId() {
        var joinedId = ""
        setScreen(
            lobbies = listOf(testLobby(id = "ABC123", isOpen = true)),
            onJoinLobby = { joinedId = it }
        )

        composeRule.onNodeWithText("Join").performClick()

        assertEquals("ABC123", joinedId)
    }

    @Test
    fun joinButton_isDisabled_whenLobbyIsFull() {
        setScreen(lobbies = listOf(testLobby(isOpen = false)))
        composeRule.onNodeWithText("Full").assertIsNotEnabled()
    }

    // --- Loading state ---

    @Test
    fun loadingState_showsProgressIndicator() {
        setScreen(isLoading = true)
        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun notLoading_doesNotShowProgressIndicator() {
        setScreen(isLoading = false)
        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()
    }

    // --- Error state ---

    @Test
    fun errorMessage_isDisplayed() {
        setScreen(errorMessage = "Failed to load lobbies")
        composeRule.onNodeWithText("Failed to load lobbies").assertIsDisplayed()
    }

    @Test
    fun noErrorMessage_isNotDisplayed() {
        setScreen(errorMessage = null)
        composeRule.onNodeWithText("Failed to load lobbies").assertDoesNotExist()
    }

    // --- Direct join flow ---

    @Test
    fun directJoin_buttonIsDisabled_whenInputIsEmpty() {
        setScreen()
        composeRule.onNodeWithText("Join by ID").assertIsNotEnabled()
    }

    @Test
    fun directJoin_buttonIsEnabled_whenInputIsNotEmpty() {
        setScreen()

        composeRule
            .onNodeWithText("Lobby ID")
            .performTextInput("ABC123")

        composeRule
            .onNodeWithText("Join by ID")
            .assertIsEnabled()
    }

    @Test
    fun directJoin_callsOnJoinLobby_withEnteredId() {
        var joinedId = ""

        setScreen(onJoinLobby = { joinedId = it })

        composeRule
            .onNodeWithText("Lobby ID")
            .performTextInput("XYZ99")

        composeRule
            .onNodeWithText("Join by ID")
            .performClick()

        assertEquals("XYZ99", joinedId)
    }
}*/
