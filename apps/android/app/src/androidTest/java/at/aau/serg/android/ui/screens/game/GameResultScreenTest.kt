package at.aau.serg.android.ui.screens.game

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class GameResultScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeResult = GameResultUiModel(
        winnerUserId = "u1",
        players = listOf(
            GameResultPlayerSummary(
                userId = "u1",
                displayName = "Alice",
                score = 120,
                finishPosition = 1
            ),
            GameResultPlayerSummary(
                userId = "u2",
                displayName = "Bob",
                score = 80,
                finishPosition = 2
            )
        ),
        matchDuration = "3:45"
    )

    @Test
    fun gameResultScreen_renders_withNullResult() {
        composeRule.setContent {
            GameResultScreen(gameResult = null)
        }

        composeRule
            .onNodeWithTag(GameTestTags.RESULT_SCREEN)
            .assertExists()
    }

    @Test
    fun gameResultScreen_renders_withResult() {
        composeRule.setContent {
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = "u1"
            )
        }

        composeRule
            .onNodeWithTag(GameTestTags.RESULT_SCREEN)
            .assertExists()
    }

    @Test
    fun gameResultScreen_showsYouWin_forWinner() {
        composeRule.setContent {
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = "u1"
            )
        }

        composeRule
            .onNodeWithTag(GameTestTags.RESULT_TITLE)
            .assertExists()

        composeRule
            .onNodeWithText("YOU WIN!")
            .assertExists()
    }

    @Test
    fun gameResultScreen_showsYouFinished_forNonWinner() {
        composeRule.setContent {
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = "u2"
            )
        }

        composeRule
            .onNodeWithText("YOU FINISHED")
            .assertExists()
    }

    @Test
    fun gameResultScreen_showsAllPlayers() {
        composeRule.setContent {
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = "u2"
            )
        }

        composeRule.onNodeWithText("Alice").assertExists()
        composeRule.onNodeWithText("Bob").assertExists()
    }

    @Test
    fun gameResultScreen_homeButton_callsOnNavigateHome() {
        val onNavigateHome = mockk<() -> Unit>(relaxed = true)

        composeRule.setContent {
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = "u1",
                onNavigateHome = onNavigateHome
            )
        }

        composeRule
            .onNodeWithTag(GameTestTags.RESULT_HOME_BUTTON)
            .performClick()

        verify(exactly = 1) { onNavigateHome.invoke() }
    }

    @Test
    fun gameResultScreen_winnerName_hasCorrectTestTag() {
        composeRule.setContent {
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = "u2"
            )
        }

        composeRule
            .onNodeWithTag(GameTestTags.RESULT_WINNER_NAME)
            .assertExists()
    }
}
