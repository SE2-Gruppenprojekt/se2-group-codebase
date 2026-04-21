package at.aau.serg.android.ui.screens.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(
            User.newBuilder()
                .setUid("u1")
                .setDisplayName("Alice")
                .build()
        )

        viewModel = SettingsViewModel(store)
    }

    @Test
    fun screen_is_displayed() {
        composeRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onChangeUsername = {},
                onLogout = {},
                onBack = {},
                isDarkMode = false,
                onToggleDarkMode = {}
            )
        }

        composeRule
            .onNodeWithTag(SettingsTestTags.SCREEN)
            .assertExists()
    }

    @Test
    fun backButton_triggersCallback() {
        var clicked = false

        composeRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onChangeUsername = {},
                onLogout = {},
                onBack = { clicked = true },
                isDarkMode = false,
                onToggleDarkMode = {}
            )
        }

        composeRule
            .onNodeWithTag(SettingsTestTags.BACK_BUTTON)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun changeUsernameButton_triggersCallback() {
        var clicked = false

        composeRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onChangeUsername = { clicked = true },
                onLogout = {},
                onBack = {},
                isDarkMode = false,
                onToggleDarkMode = {}
            )
        }

        composeRule
            .onNodeWithTag(SettingsTestTags.CHANGE_USERNAME_BUTTON)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun logoutButton_triggersCallback() {
        var clicked = false

        composeRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onChangeUsername = {},
                onLogout = { clicked = true },
                onBack = {},
                isDarkMode = false,
                onToggleDarkMode = {}
            )
        }

        composeRule
            .onNodeWithTag(SettingsTestTags.LOGOUT_BUTTON)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun darkModeSwitch_triggersCallback() = runTest {
        var toggled = false

        composeRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onChangeUsername = {},
                onLogout = {},
                onBack = {},
                isDarkMode = false,
                onToggleDarkMode = { toggled = true }
            )
        }

        composeRule
            .onNodeWithTag(SettingsTestTags.DARK_MODE_SWITCH)
            .performClick()

        assertTrue(toggled)
    }
}
