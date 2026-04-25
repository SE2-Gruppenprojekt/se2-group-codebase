package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class LobbyWaitingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var uiState: MutableState<LobbyWaitingUiState>

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
                (data as MutableStateFlow<User>).value = value
            }

            override suspend fun wipe() {
                (data as MutableStateFlow<User>).value = User.getDefaultInstance()
            }
        }

        api = mockk()

    }


    private fun setContent(onEvent: (LobbyWaitingEvent) -> Unit = {}) {
        composeRule.setContent {
            LobbyWaitingScreenContent(
                uiState = uiState.value,
                onEvent = onEvent
            )
        }
    }

    @Test
    fun screen_renders_correctly() {
        setContent()

        composeRule.onNodeWithTag(LobbyWaitingTestTags.ROOM_CODE).assertExists()
        composeRule.onNodeWithTag(LobbyWaitingTestTags.PLAYER_LIST).assertExists()
        composeRule.onNodeWithTag(LobbyWaitingTestTags.TURN_TIMER_VALUE).assertExists()
        composeRule.onNodeWithTag(LobbyWaitingTestTags.STARTING_CARDS_VALUE).assertExists()
    }

    @Test
    fun turn_timer_plus_click_triggers_event() {
        var called = false

        setContent {
            if (it is LobbyWaitingEvent.OnTurnTimerIncrease) {
                called = true
            }
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.TURN_TIMER_PLUS)
            .performClick()

        assertTrue(called)
    }

    @Test
    fun turn_timer_minus_click_triggers_event() {
        var called = false

        setContent {
            if (it is LobbyWaitingEvent.OnTurnTimerDecrease) {
                called = true
            }
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.TURN_TIMER_MINUS)
            .performClick()

        assertTrue(called)
    }

    @Test
    fun starting_cards_plus_click_triggers_event() {
        var called = false

        setContent {
            if (it is LobbyWaitingEvent.OnStartingCardsIncrease) {
                called = true
            }
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.STARTING_CARDS_PLUS)
            .performClick()

        assertTrue(called)
    }

    @Test
    fun starting_cards_minus_click_triggers_event() {
        var called = false

        setContent {
            if (it is LobbyWaitingEvent.OnStartingCardsDecrease) {
                called = true
            }
        }

        composeRule
            .onNodeWithTag(LobbyWaitingTestTags.STARTING_CARDS_MINUS)
            .performClick()

        assertTrue(called)
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

        assertTrue(lastValue != null)
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

        composeRule
            .onNodeWithTag("waiting_start_game_button")
            .performClick()

        assertTrue(called)
    }

    @Test
    fun start_button_not_visible_for_non_host() {

        uiState.value = uiState.value.copy(
            user = User.newBuilder().setUid("other").build()
        )

        setContent()

        composeRule
            .onNodeWithTag("waiting_start_game_button")
            .assertDoesNotExist()
    }
}
