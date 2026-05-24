package at.aau.serg.android.ui.screens.game.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import at.aau.serg.android.ui.theme.ThemeState
import org.junit.After
import org.junit.Rule
import org.junit.Test
import shared.models.game.domain.GamePlayer

class PlayerChipTest {

    @get:Rule
    val composeRule = createComposeRule()

    @After
    fun teardown() {
        ThemeState.isDarkMode.value = false
    }

    private val player = GamePlayer(
        userId = "user-1",
        displayName = "Alice",
        turnOrder = 0
    )

    // --- inactive player ---

    @Test
    fun playerChip_displaysName_inactive() {
        composeRule.setContent {
            PlayerChip(player = player, isActive = false)
        }
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    @Test
    fun playerChip_displaysInitial_inactive() {
        composeRule.setContent {
            PlayerChip(player = player, isActive = false)
        }
        composeRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun playerChip_rendersDarkMode_inactive() {
        ThemeState.isDarkMode.value = true
        composeRule.setContent {
            PlayerChip(player = player, isActive = false)
        }
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    // --- active player ---

    @Test
    fun playerChip_displaysName_active() {
        composeRule.setContent {
            PlayerChip(player = player, isActive = true)
        }
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    @Test
    fun playerChip_displaysInitial_active() {
        composeRule.setContent {
            PlayerChip(player = player, isActive = true)
        }
        composeRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun playerChip_rendersDarkMode_active() {
        ThemeState.isDarkMode.value = true
        composeRule.setContent {
            PlayerChip(player = player, isActive = true)
        }
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
    }

    // --- multiple players side-by-side ---

    @Test
    fun playerChip_twoChips_bothVisible() {
        val p1 = GamePlayer(userId = "u1", displayName = "Bob", turnOrder = 0)
        val p2 = GamePlayer(userId = "u2", displayName = "Carol", turnOrder = 1)

        composeRule.setContent {
            PlayerChip(player = p1, isActive = true)
            PlayerChip(player = p2, isActive = false)
        }

        composeRule.onNodeWithText("Bob").assertIsDisplayed()
        composeRule.onNodeWithText("Carol").assertIsDisplayed()
    }
}
