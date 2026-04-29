package at.aau.serg.android.ui.screens.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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

    private fun click(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
    }

    @Test
    fun screen_renders_all_elements() = runTest {
        composeRule.setContent {
            AuthScreen(viewModel = viewModel)
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
            AuthScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .performTextInput("Alice")

        assertEquals("Alice", viewModel.uiState.value.username)
    }

    @Test
    fun invalid_username_shows_error() = runTest {
        composeRule.setContent {
            AuthScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .performTextInput("!!")

        composeRule
            .onNodeWithTag(AuthTestTags.ERROR_TEXT)
            .assertExists()
    }

    @Test
    fun continue_button_saves_user() = runTest {
        composeRule.setContent {
            AuthScreen(viewModel = viewModel)
        }

        composeRule.onNodeWithTag(AuthTestTags.INPUT)
            .performTextInput("Alice")

        click(AuthTestTags.CONTINUE_BUTTON)

        advanceUntilIdle()

        val saved = store.data.first()

        assertEquals("Alice", saved.displayName)
    }

    @Test
    fun suggestion_chip_updates_username() {
        composeRule.setContent {
            AuthScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(AuthTestTags.INPUT)
            .performTextClearance()

        click("${AuthTestTags.SuggestedNames.OPTION_PREFIX}0")
        val updated = viewModel.uiState.value.username

        assertTrue(updated.isNotBlank())
    }


    @Test
    fun backButton_is_not_shown_in_create_mode() {
        viewModel.setMode(AuthMode.CreateUser)
        composeRule.setContent {
            AuthScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(AuthTestTags.BACK_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun backButton_is_shown_in_change_username_mode() {
        viewModel.setMode(AuthMode.ChangeUsername)

        composeRule.setContent {
            AuthScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(AuthTestTags.BACK_BUTTON)
            .assertExists()
    }

    @Test
    fun back_button_triggers_event() = runTest {
        val events = mutableListOf<AuthEvent>()

        composeRule.setContent {
            AuthScreenContent(
                uiState = AuthUiState(mode = AuthMode.ChangeUsername),
                onEvent = { events.add(it) }
            )
        }

        click(AuthTestTags.BACK_BUTTON)

        assertTrue(events.any { it is AuthEvent.OnBack })
    }
}
