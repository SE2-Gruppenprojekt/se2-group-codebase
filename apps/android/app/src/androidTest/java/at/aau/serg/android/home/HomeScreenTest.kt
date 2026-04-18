package at.aau.serg.android.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.state.LoadState

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        state: LoadState = LoadState.Idle,
        onNewLobbyScreen: () -> Unit = {},
        onBrowseFancyLobbies: () -> Unit = {},
        onShowLeaderboard: () -> Unit = {},
        onSettings: () -> Unit = {}
    ) {
        composeRule.setContent {
            HomeScreen(
                state = state,
                onNewLobbyScreen = onNewLobbyScreen,
                onBrowseFancyLobbies = onBrowseFancyLobbies,
                onShowLeaderboard = onShowLeaderboard,
                onSettings = onSettings
            )
        }
    }

    // --- Button callbacks ---

    @Test
    fun clickingCreateLobby_callsOnNewLobbyScreen() {
        var called = false

        setScreen(onNewLobbyScreen = { called = true })

        composeRule
            .onNodeWithTag("home_create_lobby_button")
            .performClick()

        assertTrue(called)
    }

    @Test
    fun clickingBrowseLobbies_callsOnBrowseFancyLobbies() {
        var called = false

        setScreen(onBrowseFancyLobbies = { called = true })

        composeRule
            .onNodeWithTag("home_browse_lobbies_button")
            .performClick()

        assertTrue(called)
    }

    @Test
    fun clickingSettings_callsOnSettings() {
        var called = false

        setScreen(onSettings = { called = true })

        composeRule
            .onNodeWithTag("home_settings_list_button")
            .performClick()

        assertTrue(called)
    }

    @Test
    fun clickingLeaderboard_callsOnShowLeaderboard() {
        var called = false

        setScreen(onShowLeaderboard = { called = true })

        composeRule
            .onNodeWithTag("home_leaderboard_button")
            .performClick()

        assertTrue(called)
    }
}
