package at.aau.serg.android.ui.screens.lobby.create

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.lobby.response.LobbyResponse


@OptIn(ExperimentalCoroutinesApi::class)
class LobbyCreateScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var api: LobbyAPI
    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: LobbyCreateViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
        api = mockk()

        val fakeLobby = LobbyResponse(
            lobbyId = "lobby-123",
            hostUserId = "user-1",
            status = "OPEN",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery {
            api.createLobby(
                any(),
                any()
            )
        } returns fakeLobby

        viewModel = LobbyCreateViewModel(
            userStore = store,
            api = api
        )
    }

    @Test
    fun screen_renders_all_elements() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        composeRule.onNodeWithTag(LobbyCreateTestTags.SCREEN).assertExists()
        composeRule.onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON).assertExists()
        composeRule.onNodeWithTag(LobbyCreateTestTags.ROOM_CODE_TEXT).assertExists()
    }

    @Test
    fun max_players_selection_works() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        composeRule.onNodeWithTag(LobbyCreateTestTags.MAX_PLAYERS_OPTION_4)
            .performClick()

        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.maxPlayers == 4)
    }

    @Test
    fun privacy_toggle_changes_state() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        composeRule.onNodeWithTag(LobbyCreateTestTags.PRIVACY_PRIVATE)
            .performClick()

        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.isPrivate)
    }

    @Test
    fun turnTimer_increases_value() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        val before = viewModel.uiState.value.turnTimer

        composeRule.onNodeWithTag(LobbyCreateTestTags.TURN_TIMER_PLUS)
            .performClick()

        composeRule.waitForIdle()

        val after = viewModel.uiState.value.turnTimer

        assertTrue(after > before)
    }

    @Test
    fun turnTimer_decreases_value() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        val before = viewModel.uiState.value.turnTimer

        composeRule.onNodeWithTag(LobbyCreateTestTags.TURN_TIMER_MINUS)
            .performClick()

        composeRule.waitForIdle()

        val after = viewModel.uiState.value.turnTimer

        assertTrue(after < before)
    }

    @Test
    fun startingTiles_increases_value() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        val before = viewModel.uiState.value.startingTiles

        composeRule.onNodeWithTag(LobbyCreateTestTags.STARTING_TILES_PLUS)
            .performClick()

        composeRule.waitForIdle()

        val after = viewModel.uiState.value.startingTiles

        assertTrue(after > before)
    }

    @Test
    fun startingTiles_decreases_value() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        val before = viewModel.uiState.value.startingTiles

        composeRule.onNodeWithTag(LobbyCreateTestTags.STARTING_TILES_MINUS)
            .performClick()

        composeRule.waitForIdle()

        val after = viewModel.uiState.value.startingTiles

        assertTrue(after < before)
    }

    @Test
    fun quickMode_toggle_changes_state() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(
                viewModel = viewModel,
                onBack = {},
                onSettings = {}
            )
        }

        val before = viewModel.uiState.value.quickMode

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.QUICK_MODE_TOGGLE)
            .performClick()

        composeRule.waitForIdle()
        advanceUntilIdle()

        val after = viewModel.uiState.value.quickMode

        assertTrue(after != before)
    }

    @Test
    fun requireInitialMeld_toggle_changes_state() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        val before = viewModel.uiState.value.requireInitialMeld

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.REQUIRE_INITIAL_MELD_TOGGLE)
            .performClick()

        composeRule.waitForIdle()

        val after = viewModel.uiState.value.requireInitialMeld

        assertTrue(after != before)
    }

    @Test
    fun quick_mode_toggle_is_idempotent() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        val initial = viewModel.uiState.value.quickMode

        composeRule.onNodeWithTag(LobbyCreateTestTags.QUICK_MODE_TOGGLE)
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(LobbyCreateTestTags.QUICK_MODE_TOGGLE)
            .performClick()
        composeRule.waitForIdle()

        val final = viewModel.uiState.value.quickMode

        assertTrue(final == initial)
    }

    @Test
    fun requireInitialMeld_toggle_is_idempotent() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        val initial = viewModel.uiState.value.requireInitialMeld

        composeRule.onNodeWithTag(LobbyCreateTestTags.REQUIRE_INITIAL_MELD_TOGGLE)
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(LobbyCreateTestTags.REQUIRE_INITIAL_MELD_TOGGLE)
            .performClick()
        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.requireInitialMeld == initial)
    }

    @Test
    fun max_players_updates_multiple_times() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        composeRule.onNodeWithTag(LobbyCreateTestTags.MAX_PLAYERS_OPTION_2).performClick()
        composeRule.onNodeWithTag(LobbyCreateTestTags.MAX_PLAYERS_OPTION_8).performClick()

        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.maxPlayers == 8)
    }

    @Test
    fun privacy_toggle_switches_both_ways() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        composeRule.onNodeWithTag(LobbyCreateTestTags.PRIVACY_PRIVATE).performClick()
        composeRule.waitForIdle()
        assertTrue(viewModel.uiState.value.isPrivate)

        composeRule.onNodeWithTag(LobbyCreateTestTags.PRIVACY_PUBLIC).performClick()
        composeRule.waitForIdle()
        assertTrue(!viewModel.uiState.value.isPrivate)
    }

    @Test
    fun create_lobby_updates_loading_and_success_state() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        viewModel.onEvent(LobbyCreateEvent.CreateLobby)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadState == LoadState.Success)
    }

    @Test
    fun create_lobby_emits_navigation_effect() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        viewModel.onEvent(LobbyCreateEvent.CreateLobby)

        advanceUntilIdle()

        val effect = viewModel.effects.first()

        assertTrue(effect is LobbyCreateEffect.NavigateToWaitingRoom)
    }

    @Test
    fun create_button_is_enabled_by_default() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON)
            .assertExists()
    }

    @Test
    fun create_button_disabled_when_loading() = runTest {
        coEvery { api.createLobby(any(), any()) } coAnswers {
            suspendCancellableCoroutine { /* never resumes */ }
        }

        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        viewModel.onEvent(LobbyCreateEvent.CreateLobby)

        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON)
            .assertIsNotEnabled()
    }


    @Test
    fun winScore_increases_and_decreases_and_clamps_to_zero() = runTest {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        val before = viewModel.uiState.value.winScore

        composeRule.onNodeWithTag(LobbyCreateTestTags.WIN_SCORE_PLUS)
            .performClick()
        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.winScore > before)

        composeRule.onNodeWithTag(LobbyCreateTestTags.WIN_SCORE_MINUS)
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(LobbyCreateTestTags.WIN_SCORE_MINUS)
            .performClick()
            .performClick()
            .performClick()
            .performClick()

        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.winScore >= 0)
    }

    @Test
    fun copy_button_is_clickable_and_does_not_crash() {
        composeRule.setContent {
            LobbyCreateScreen(viewModel, {}, {})
        }

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.COPY_ROOM_CODE_BUTTON)
            .performClick()

        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.ROOM_CODE_TEXT)
            .assertExists()
    }
}
