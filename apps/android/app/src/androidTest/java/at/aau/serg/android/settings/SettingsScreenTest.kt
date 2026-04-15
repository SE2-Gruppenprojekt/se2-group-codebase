package at.aau.serg.android.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.android.ui.screens.settings.SettingsScreen
import at.aau.serg.android.ui.theme.ThemeState
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

// UI-Component tests
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        onBack: () -> Unit = {}
    ) {
        composeRule.setContent {
            SettingsScreen(onBack = onBack)
        }
    }

    // --- Rendering ---

    @Test
    fun displaysDarkModeToggle() {
        setScreen()
        composeRule.onNodeWithText("Dark Mode").assertIsDisplayed()
    }

    // --- Back button ---

    @Test
    fun clickingBack_callsOnBack() {
        var called = false
        setScreen(onBack = { called = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(called)
    }

    // --- Dark mode toggle ---

    @Test
    fun darkModeToggle_reflectsInitialState() {
        ThemeState.isDarkMode.value = true
        setScreen()
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag("settings_darkmode_switch")
            .assertIsOn()
    }

    @Test
    fun darkModeToggle_changesThemeState() {
        ThemeState.isDarkMode.value = false
        setScreen()
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag("settings_darkmode_switch")
            .performClick()

        assertTrue(ThemeState.isDarkMode.value)
    }

    @Test
    fun darkModeToggle_canTurnOffDarkMode() {
        ThemeState.isDarkMode.value = true
        setScreen()
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag("settings_darkmode_switch")
            .performClick()

        assertFalse(ThemeState.isDarkMode.value)
    }

    @After
    fun tearDown() {
        ThemeState.isDarkMode.value = false
    }

}
