package at.aau.serg.android.leaderboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen
import shared.models.LeaderboardEntry

// UI-Component tests
class LeaderboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        players: List<LeaderboardEntry> = emptyList(),
        onBack: () -> Unit = {}
    ) {
        composeRule.setContent {
            LeaderboardScreen(
                players = players,
                onBack = onBack
            )
        }
    }

    // --- Rendering ---

    @Test
    fun displaysLeaderboardTitle() {
        setScreen()
        composeRule.onNodeWithText("Leaderboard").assertIsDisplayed()
    }

    // --- Back button ---

    @Test
    fun clickingBack_callsOnBack() {
        var called = false
        setScreen(onBack = { called = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(called)
    }
}
