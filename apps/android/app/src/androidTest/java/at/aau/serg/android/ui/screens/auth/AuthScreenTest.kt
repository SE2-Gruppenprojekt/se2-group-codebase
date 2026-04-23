package at.aau.serg.android.ui.screens.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
        viewModel = AuthViewModel(store)
    }

    @Test
    fun screen_renders_all_elements() = runTest {
        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onContinue = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .assertExists()

        composeRule
            .onNodeWithText("Create Your Profile")
            .assertExists()
    }

    @Test
    fun typing_updates_username() = runTest {
        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onContinue = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .performTextInput("Alice")

        assertEquals("Alice", viewModel.uiState.value.username)
    }

    @Test
    fun invalid_username_shows_error() = runTest {
        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onContinue = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .performTextInput("!!")

        composeRule
            .onNodeWithTag(AuthTestTags.ERROR_TEXT)
            .assertExists()
    }

    @Test
    fun continue_button_calls_submit() = runTest {
        var called = false

        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onContinue = { called = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .performTextInput("Alice")

        composeRule
            .onNodeWithTag(AuthTestTags.CONTINUE_BUTTON)
            .performClick()

        assertTrue(called)
    }

    @Test
    fun suggestion_chip_updates_username() {
        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onContinue = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithText("Player7429")
            .performClick()

        assertEquals("Player7429", viewModel.uiState.value.username)
    }
}
