package at.aau.serg.android.ui.screens.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(
            User.newBuilder()
                .setUid("1")
                .setDisplayName("Alice")
                .build()
        )

        viewModel = HomeViewModel(store)
    }

    @Test
    fun screen_is_displayed() {
        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = {},
                onBrowseFancyLobbies = {},
                onShowLeaderboard = {},
                onSettings = {}
            )
        }

        composeRule
            .onNodeWithTag(HomeTestTags.SCREEN)
            .assertExists()
    }

    @Test
    fun username_isDisplayed() = runTest {
        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = {},
                onBrowseFancyLobbies = {},
                onShowLeaderboard = {},
                onSettings = {}
            )
        }

        composeRule
            .onNodeWithText("Alice")
            .assertExists()
    }

    @Test
    fun username_is_displayed_with_tag() {
        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = {},
                onBrowseFancyLobbies = {},
                onShowLeaderboard = {},
                onSettings = {}
            )
        }

        composeRule
            .onNodeWithTag(HomeTestTags.USERNAME_TEXT)
            .assertExists()
    }

    @Test
    fun createLobbyButton_triggersCallback() = runTest {
        var clicked = false

        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = { clicked = true },
                onBrowseFancyLobbies = {},
                onShowLeaderboard = {},
                onSettings = {}
            )
        }

        composeRule
            .onNodeWithTag(HomeTestTags.CREATE_LOBBY_BUTTON)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun browseLobbyButton_triggersCallback() {
        var clicked = false

        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = {},
                onBrowseFancyLobbies = { clicked = true },
                onShowLeaderboard = {},
                onSettings = {}
            )
        }

        composeRule
            .onNodeWithTag(HomeTestTags.BROWSE_LOBBIES_BUTTON)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun settingsButton_triggersCallback() {
        var clicked = false

        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = {},
                onBrowseFancyLobbies = {},
                onShowLeaderboard = {},
                onSettings = { clicked = true }
            )
        }

        composeRule
            .onNodeWithTag(HomeTestTags.SETTINGS_BUTTON)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun leaderboardButton_triggersCallback() {
        var clicked = false

        composeRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onNewLobbyScreen = {},
                onBrowseFancyLobbies = {},
                onShowLeaderboard = { clicked = true },
                onSettings = {}
            )
        }

        composeRule
            .onNodeWithTag(HomeTestTags.LEADERBOARD_BUTTON)
            .performClick()

        assertTrue(clicked)
    }
}
