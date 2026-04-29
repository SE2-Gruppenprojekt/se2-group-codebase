package at.aau.serg.android.ui.screens.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<SettingsEvent>()

    private fun setContent() {
        events.clear()
        composeRule.setContent {
            SettingsScreenContent(
                onEvent = { events.add(it) }
            )
        }
    }

    private fun click(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
    }

    @Test
    fun screen_is_displayed() {
        setContent()

        composeRule
            .onNodeWithTag(SettingsTestTags.SCREEN)
            .assertExists()
    }

    @Test
    fun buttons_trigger_events() {
        val events = mutableListOf<SettingsEvent>()

        composeRule.setContent {
            SettingsScreenContent(
                onEvent = { events.add(it) }
            )
        }

        click(SettingsTestTags.BACK_BUTTON)
        click(SettingsTestTags.CHANGE_USERNAME_BUTTON)
        click(SettingsTestTags.LOGOUT_BUTTON)

        assertEquals(3, events.size)

        assertTrue(events.contains(SettingsEvent.OnBack))
        assertTrue(events.contains(SettingsEvent.OnChangeUsername))
        assertTrue(events.contains(SettingsEvent.OnLogout))
    }

    @Test
    fun darkModeSwitch_emits_toggle_event() {
        val events = mutableListOf<SettingsEvent>()

        composeRule.setContent {
            SettingsScreenContent(
                onEvent = { events.add(it) }
            )
        }

        click(SettingsTestTags.DARK_MODE_SWITCH)

        assertTrue(events.any { it is SettingsEvent.SetDarkMode })
    }
}
