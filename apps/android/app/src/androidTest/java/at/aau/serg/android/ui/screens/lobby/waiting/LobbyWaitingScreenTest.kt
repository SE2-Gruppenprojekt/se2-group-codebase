package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import at.aau.serg.android.datastore.proto.User
import junit.framework.TestCase.assertTrue
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

class LobbyWaitingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun start_button_click_triggers_event() {
        var called = false

        val state = LobbyWaitingUiState(
            turnTimer = 20,
            startingCards = 5,
            stackEnabled = false,
            user = User.newBuilder().setUid("host").build(),
            lobby = Lobby(
                lobbyId = "ABCDEF123",
                hostUserId = "host", // wichtig → damit Button sichtbar ist!
                players = emptyList(),
                status = LobbyStatus.OPEN,
                settings = LobbySettings(maxPlayers = 4)
            )
        )

        composeRule.setContent {
            LobbyWaitingScreenContent(
                uiState = state,
                onEvent = {
                    if (it is LobbyWaitingEvent.onMatchStart) {
                        called = true
                    }
                }
            )
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.START_BUTTON)
            .assertExists()
            .performClick()

        assertTrue(called)
    }
}
