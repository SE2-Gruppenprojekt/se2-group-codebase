package at.aau.serg.android.ui.screens.lobby.browse

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import at.aau.serg.android.core.errors.AppError
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.util.ErrorUiMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LobbyBrowseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        lobbies: List<LobbyBrowseItem> = emptyList(),
        loadState: LoadState = LoadState.Success,
        lobbyIdInput: String = "",
        onEvent: (LobbyBrowseEvent) -> Unit = {}
    ) {
        composeRule.setContent {
            LobbyBrowseScreenContent(
                uiState = LobbyBrowseUiState(
                    lobbies = lobbies,
                    loadState = loadState,
                    lobbyIdInput = lobbyIdInput
                ),
                onEvent = onEvent
            )
        }
    }

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

    // --- screen structure ---

    @Test
    fun screen_isDisplayed() {
        setScreen()
        composeRule.onNodeWithTag(LobbyBrowseTestTags.SCREEN).assertIsDisplayed()
    }

    @Test
    fun displaysLobbyList() {
        setScreen(lobbies = listOf(testLobby()))
        composeRule.onNodeWithTag(LobbyBrowseTestTags.LOBBY_LIST).assertIsDisplayed()
    }

    // --- lobby entries ---

    @Test
    fun displaysMultipleLobbies() {
        setScreen(lobbies = listOf(testLobby("AAA"), testLobby("BBB")))
        composeRule.onNodeWithText("#AAA").assertIsDisplayed()
        composeRule.onNodeWithText("#BBB").assertIsDisplayed()
    }

    @Test
    fun displaysLobbyDataCorrectly() {
        setScreen(
            lobbies = listOf(
                testLobby(
                    id = "ABC123",
                    currentPlayers = 2,
                    maxPlayers = 4,
                    turnTimerSeconds = 60,
                    startingCards = 14
                )
            )
        )
        composeRule.onNodeWithText("#ABC123").assertIsDisplayed()
        composeRule.onNodeWithText("2/4").assertIsDisplayed()
        composeRule.onNodeWithText("60s").assertIsDisplayed()
        composeRule.onNodeWithText("14 cards").assertIsDisplayed()
    }

    @Test
    fun clickingJoin_triggersJoinEvent() {
        var event: LobbyBrowseEvent? = null
        setScreen(
            lobbies = listOf(testLobby(id = "ABC123", isOpen = true)),
            onEvent = { event = it }
        )

        composeRule
            .onNodeWithTag("${LobbyBrowseTestTags.LobbyItem.JOIN_BUTTON_PREFIX}_ABC123")
            .performClick()

        assertEquals(LobbyBrowseEvent.OnJoinLobby("ABC123"), event)
    }

    @Test
    fun fullLobby_showsDisabledState() {
        setScreen(lobbies = listOf(testLobby(isOpen = false)))
        composeRule.onNodeWithText("Full").assertIsNotEnabled()
    }

    // --- direct join ---

    @Test
    fun directJoin_inputFieldExists() {
        setScreen()
        composeRule.onNodeWithTag(LobbyBrowseTestTags.LOBBY_ID_INPUT).assertIsDisplayed()
    }

    @Test
    fun directJoin_buttonDisabled_whenInputEmpty() {
        setScreen()
        composeRule.onNodeWithTag(LobbyBrowseTestTags.JOIN_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun directJoin_buttonEnabled_whenInputNotEmpty() {
        setScreen(lobbyIdInput = "ABC123")
        composeRule.onNodeWithTag(LobbyBrowseTestTags.JOIN_BUTTON).assertIsEnabled()
    }

    @Test
    fun typingLobbyId_sendsEvent() {
        var event: LobbyBrowseEvent? = null
        setScreen(onEvent = { event = it })

        composeRule
            .onNodeWithTag(LobbyBrowseTestTags.LOBBY_ID_INPUT)
            .performTextInput("XYZ")

        assertTrue(event is LobbyBrowseEvent.OnLobbyIdChanged)
    }

    @Test
    fun directJoin_triggersJoinEvent() {
        var event: LobbyBrowseEvent? = null
        setScreen(lobbyIdInput = "XYZ99", onEvent = { event = it })

        composeRule.onNodeWithTag(LobbyBrowseTestTags.JOIN_BUTTON).performClick()

        assertEquals(LobbyBrowseEvent.OnJoinLobby("XYZ99"), event)
    }

    // --- loading / error state ---

    @Test
    fun loadingState_showsProgressIndicator() {
        setScreen(loadState = LoadState.Loading)
        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun notLoading_hidesProgressIndicator() {
        setScreen(loadState = LoadState.Success)
        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()
    }

    @Test
    fun errorMessage_isDisplayed() {
        setScreen(
            loadState = LoadState.Error(AppError.Network)
        )

        composeRule
            .onNodeWithText(ErrorUiMapper.toMessage(AppError.Network))
            .assertIsDisplayed()
    }

    @Test
    fun noErrorMessage_isNotDisplayed() {
        setScreen(
            loadState = LoadState.Success
        )

        composeRule
            .onNodeWithText("Network error")
            .assertDoesNotExist()
    }

    // --- buttons ---

    @Test
    fun createLobbyButton_triggersEvent() {
        var event: LobbyBrowseEvent? = null
        setScreen(onEvent = { event = it })

        composeRule.onNodeWithTag(LobbyBrowseTestTags.CREATE_BUTTON).performClick()

        assertEquals(LobbyBrowseEvent.OnCreateNewLobby, event)
    }

    @Test
    fun topBar_buttons_triggerEvents() {
        val events = mutableListOf<LobbyBrowseEvent>()
        setScreen(onEvent = { events.add(it) })

        composeRule.onNodeWithTag(LobbyBrowseTestTags.BACK_BUTTON).performClick()
        composeRule.onNodeWithTag(LobbyBrowseTestTags.SETTINGS_BUTTON).performClick()

        assertTrue(events.contains(LobbyBrowseEvent.OnBack))
        assertTrue(events.contains(LobbyBrowseEvent.OnSettings))
    }
}
