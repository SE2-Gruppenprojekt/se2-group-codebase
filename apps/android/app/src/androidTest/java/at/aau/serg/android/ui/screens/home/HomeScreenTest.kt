package at.aau.serg.android.ui.screens.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var fakeUser: User = User.newBuilder()
        .setUid("1")
        .setDisplayName("Alice")
        .build()

    private fun setScreen(
        user: User = fakeUser,
        loadState: LoadState = LoadState.Success,
        onEvent: (HomeEvent) -> Unit = {}
    ) {
        composeRule.setContent {
            HomeScreenContent(
                uiState = HomeUiState(
                    user = user,
                    loadState = loadState
                ),
                onEvent = onEvent
            )
        }
    }

    @Before
    fun setup() {
    }


    @Test
    fun screen_is_displayed() {
        setScreen()
        composeRule
            .onNodeWithTag(HomeTestTags.SCREEN)
            .assertExists()
    }

    @Test
    fun username_isDisplayed() = runTest {
        setScreen()

        composeRule
            .onNodeWithText("Alice")
            .assertExists()
    }

    @Test
    fun username_is_displayed_with_tag() {
        setScreen()

        composeRule
            .onNodeWithTag(HomeTestTags.USERNAME_TEXT)
            .assertExists()
    }

    @Test
    fun buttons_trigger_events() {
        val events = mutableListOf<HomeEvent>()

        setScreen(onEvent = { events.add(it) })

        composeRule
            .onNodeWithTag(HomeTestTags.ACTION_CREATE_LOBBY)
            .performClick()

        composeRule
            .onNodeWithTag(HomeTestTags.ACTION_BROWSE_LOBBY)
            .performClick()

        composeRule
            .onNodeWithTag(HomeTestTags.ACTION_SETTINGS)
            .performClick()

        composeRule
            .onNodeWithTag(HomeTestTags.TOPBAR_SETTINGS_BUTTON)
            .performClick()


        assertEquals(
            listOf(
                HomeEvent.OnCreateLobby,
                HomeEvent.OnBrowseLobby,
                HomeEvent.OnSettings,
                HomeEvent.OnSettings
            ),
            events
        )
    }
}
