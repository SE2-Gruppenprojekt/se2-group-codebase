package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

class LobbyWaitingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var userStore: ProtoStore<User>
    private lateinit var api: LobbyAPI
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
                data.value = value
            }

            override suspend fun wipe() {
                data.value = User.getDefaultInstance()
            }
        }

        api = mockk()

    }


    private fun setContent(onEvent: (LobbyWaitingEvent) -> Unit = {}) {
        composeRule.setContent {
            LobbyWaitingScreenContent(
                uiState = LobbyWaitingUiState(),
                onEvent = onEvent
            )
        }
    }

    private fun click(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
    }

    @Test
    fun screen_renders_correctly() {
        setContent()

        composeRule.onNodeWithTag(LobbyWaitingTestTags.SCREEN).assertExists()
    }

    @Test
    fun turn_timer_click_events() {
        val events = mutableListOf<LobbyWaitingEvent>()

        setContent(onEvent = { events.add(it) })

        click(LobbyWaitingTestTags.TURN_TIMER_MINUS)
        click(LobbyWaitingTestTags.TURN_TIMER_PLUS)


        assertEquals(
            listOf(
                LobbyWaitingEvent.OnTurnTimerDecrease,
                LobbyWaitingEvent.OnTurnTimerIncrease
            ),
            events
        )
    }

    @Test
    fun starting_cards_click_events() {
        val events = mutableListOf<LobbyWaitingEvent>()

        setContent(onEvent = { events.add(it) })

        click(LobbyWaitingTestTags.STARTING_CARDS_MINUS)
        click(LobbyWaitingTestTags.STARTING_CARDS_PLUS)


        assertEquals(
            listOf(
                LobbyWaitingEvent.OnStartingCardsDecrease,
                LobbyWaitingEvent.OnStartingCardsIncrease
            ),
            events
        )
    }

    @Test
    fun stack_switch_triggers_event() {
        var lastValue: Boolean? = null

        setContent {
            if (it is LobbyWaitingEvent.OnStackToggle) {
                lastValue = it.enabled
            }
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.STACK_SWITCH)
            .performClick()

        Assert.assertTrue(lastValue != null)
    }


    @Test
    fun start_button_visible_for_host() {
        setContent()

        composeRule
            .onNodeWithTag("waiting_start_game_button")
            .assertExists()
    }

    @Test
    fun start_button_click_triggers_event() {
        var called = false

        setContent {
            if (it is LobbyWaitingEvent.onMatchStart) {
                called = true
            }
        }

        click(LobbyWaitingTestTags.START_BUTTON)

        Assert.assertTrue(called)
    }

    @Test
    fun start_button_not_visible_for_non_host() {
        val uiUser = User.newBuilder()
            .setUid("me")
            .setDisplayName("Alice")
            .build()

        val lobby = Lobby(
            lobbyId = "lobby-123",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            players = listOf(
                LobbyPlayer(
                    userId = "host-1",
                    displayName = "HostPlayer",
                    isReady = true
                ),
                LobbyPlayer(
                    userId = "me",
                    displayName = "Alice",
                    isReady = false
                )
            ),
            settings = LobbySettings()
        )

        composeRule.setContent {
            LobbyWaitingScreenContent(
                uiState = LobbyWaitingUiState(
                    lobby = lobby,
                    user = uiUser
                ),
                onEvent = {}
            )
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.START_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun ready_toggle_works_for_self() {
        val uiUser = User.newBuilder()
            .setUid("me")
            .setDisplayName("Alice")
            .build()

        val lobby = Lobby(
            lobbyId = "lobby-123",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            players = listOf(
                LobbyPlayer(
                    userId = "host-1",
                    displayName = "HostPlayer",
                    isReady = false
                ),
                LobbyPlayer(
                    userId = "me",
                    displayName = "Alice",
                    isReady = false
                )
            ),
            settings = LobbySettings()
        )

        val events = mutableListOf<LobbyWaitingEvent>()
        composeRule.setContent {
            LobbyWaitingScreenContent(
                uiState = LobbyWaitingUiState(
                    lobby = lobby,
                    user = uiUser
                ),
                onEvent = { events.add(it) }
            )
        }

        val tag = LobbyWaitingTestTags.Players.ready_tag("me")

        composeRule
            .onNodeWithTag(tag)
            .assertExists()
            .assertHasClickAction()

        click(tag)

        assert(events.first() is LobbyWaitingEvent.ToggleReadyState)
        val event = events.first() as LobbyWaitingEvent.ToggleReadyState
        assert(event.userId == "me")
    }

    @Test
    fun ready_toggle_does_not_work_for_other_players() {
        val uiUser = User.newBuilder()
            .setUid("me")
            .setDisplayName("Alice")
            .build()

        val lobby = Lobby(
            lobbyId = "lobby-123",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            players = listOf(
                LobbyPlayer(
                    userId = "host-1",
                    displayName = "HostPlayer",
                    isReady = false
                ),
                LobbyPlayer(
                    userId = "other",
                    displayName = "Bob",
                    isReady = false
                )
            ),
            settings = LobbySettings()
        )

        val events = mutableListOf<LobbyWaitingEvent>()

        composeRule.setContent {
            LobbyWaitingScreenContent(
                uiState = LobbyWaitingUiState(
                    lobby = lobby,
                    user = uiUser
                ),
                onEvent = { events.add(it) }
            )
        }

        val tag = LobbyWaitingTestTags.Players.ready_tag("host-1")

        composeRule
            .onNodeWithTag(tag)
            .assertExists()

        composeRule
            .onNodeWithTag(tag)
            .assertHasClickAction()

        click(tag)

        assert(events.isEmpty())
    }
}
