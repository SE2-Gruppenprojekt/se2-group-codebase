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
        isDarkMode: Boolean = false,
        onBack: () -> Unit = {},
        onLogout: () -> Unit = {},
        onUsernameChange: (String) -> Unit = {},
        onToggleDarkMode: () -> Unit = {}
    ) {
        composeRule.setContent {
            SettingsScreen(
                user = user,
                onUsernameChange = onUsernameChange,
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

    // --- Username change ---

    @Test
    fun usernameChange_triggersCallback() {
        var changedValue = ""

        setScreen(onUsernameChange = {
            changedValue = it
        })

        composeRule
            .onNodeWithText("TestUser")
            .performTextReplacement("NewName")

        assertTrue(changedValue == "NewName")
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

    // --- Dark mode toggle callback ---

    @Test
    fun darkModeToggle_callsCallback() {
        var toggled = false

        setScreen(onToggleDarkMode = { toggled = true })

        composeRule
            .onNodeWithText("Dark Mode")
            .performClick()

        assertTrue(toggled)
    }
}
