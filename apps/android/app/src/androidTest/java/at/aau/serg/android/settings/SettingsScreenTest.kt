package at.aau.serg.android.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.settings.SettingsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        user: User = User.newBuilder()
            .setUid("1")
            .setDisplayName("TestUser")
            .build(),
        onBack: () -> Unit = {},
        onLogout: () -> Unit = {},
        onChangeUsername: () -> Unit = {},
        isDarkMode: Boolean = false,
        onToggleDarkMode: (Boolean) -> Unit = {}
    ) {
        composeRule.setContent {
            SettingsScreen(
                user = user,
                onChangeUsername = onChangeUsername,
                onLogout = onLogout,
                onBack = onBack,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }
    }

    // --- Rendering ---

    @Test
    fun displaysDarkModeToggle() {
        setScreen()

        composeRule
            .onNodeWithText("Dark Mode")
            .assertIsDisplayed()
    }

    // --- Back button ---

    @Test
    fun clickingBack_callsOnBack() {
        var called = false

        setScreen(onBack = { called = true })

        composeRule
            .onNodeWithContentDescription("Back")
            .performClick()

        assertTrue(called)
    }

    // --- Change Username ---

    @Test
    fun clickingChangeUsername_callsCallback() {
        var called = false

        setScreen(onChangeUsername = { called = true })

        composeRule
            .onNodeWithText("Change Username")
            .performClick()

        assertTrue(called)
    }

    // --- Logout ---

    @Test
    fun clickingLogout_callsOnLogout() {
        var called = false

        setScreen(onLogout = { called = true })

        composeRule
            .onNodeWithText("Logout")
            .performClick()

        assertTrue(called)
    }

    // --- Dark mode toggle ---

    @Test
    fun darkModeToggle_isDisplayed() {
        setScreen()

        composeRule
            .onNodeWithTag("settings_darkmode_switch")
            .assertIsDisplayed()
    }

    @Test
    fun darkModeToggle_callsOnToggleDarkMode() {
        var toggled = false

        setScreen(onToggleDarkMode = { toggled = true })

        composeRule
            .onNodeWithTag("settings_darkmode_switch")
            .performClick()

        assertTrue(toggled)
    }
}
