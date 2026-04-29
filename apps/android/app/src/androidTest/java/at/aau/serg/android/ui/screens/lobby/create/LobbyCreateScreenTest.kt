package at.aau.serg.android.ui.screens.lobby.create

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import at.aau.serg.android.ui.state.LoadState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LobbyCreateScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        isLoading: Boolean = false,
        onBack: () -> Unit = {},
        onSettings: () -> Unit = {},
        onCreateLobby: (Int, Boolean) -> Unit = { _, _ -> }
    ) {
        composeRule.setContent {
            LobbyCreateScreenContent(
                uiState = LobbyCreateUiState(
                    isPrivate = false,
                    maxPlayers = 6,
                    turnTimer = 60,
                    startingTiles = 100,
                    winScore = 1000,
                    quickMode = false,
                    requireInitialMeld = false,
                    loadState = if (isLoading) LoadState.Loading else LoadState.Idle
                ),
                onEvent = { event ->
                    when (event) {
                        LobbyCreateEvent.OnBack -> onBack()
                        LobbyCreateEvent.OnSettings -> onSettings()
                        LobbyCreateEvent.CreateLobby -> onCreateLobby(6, false)
                        else -> {}
                    }
                }
            )
        }
    }

    // --- Rendering ---

    @Test
    fun displaysScreen() {
        setScreen()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.SCREEN)
            .assertIsDisplayed()
    }

    @Test
    fun displaysRoomCode() {
        setScreen()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.ROOM_CODE_TEXT)
            .assertIsDisplayed()
    }

    @Test
    fun displaysAllPlayerOptions() {
        setScreen()

        listOf(2, 4, 6, 8).forEach { count ->
            composeRule
                .onNodeWithTag("${LobbyCreateTestTags.MaxPlayers.OPTION_PREFIX}_$count")
                .assertIsDisplayed()
        }
    }

    @Test
    fun displaysPrivacyOptions() {
        setScreen()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.PRIVACY_PUBLIC)
            .assertIsDisplayed()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.PRIVACY_PRIVATE)
            .assertIsDisplayed()
    }

    @Test
    fun displaysCreateButton() {
        setScreen()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON)
            .assertIsDisplayed()
    }

    // --- Top bar actions ---

    @Test
    fun clickingBack_callsOnBack() {
        var called = false

        setScreen(onBack = { called = true })

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.BACK_BUTTON)
            .performClick()

        assertTrue(called)
    }

    @Test
    fun clickingSettings_callsOnSettings() {
        var called = false

        setScreen(onSettings = { called = true })

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.SETTINGS_BUTTON)
            .performClick()

        assertTrue(called)
    }

    // --- Player selection ---

    @Test
    fun selectingMaxPlayers_updatesSelection() {
        setScreen()

        composeRule
            .onNodeWithTag("${LobbyCreateTestTags.MaxPlayers.OPTION_PREFIX}_4")
            .performClick()
    }

    // --- Privacy selection ---

    @Test
    fun selectingPrivate_setsPrivate() {
        setScreen()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.PRIVACY_PRIVATE)
            .performClick()
    }

    @Test
    fun selectingPublic_setsPublic() {
        setScreen()

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.PRIVACY_PUBLIC)
            .performClick()
    }

    // --- Create lobby ---

    @Test
    fun clickingCreate_callsOnCreateLobby() {
        var called = false

        setScreen(onCreateLobby = { _, _ -> called = true })

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON)
            .performClick()

        assertTrue(called)
    }

    // --- Loading state ---

    @Test
    fun loadingState_disablesCreateButton() {
        setScreen(isLoading = true)

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun notLoading_enablesCreateButton() {
        setScreen(isLoading = false)

        composeRule
            .onNodeWithTag(LobbyCreateTestTags.CREATE_BUTTON)
            .assertIsEnabled()
    }
}
