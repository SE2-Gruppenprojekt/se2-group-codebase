package at.aau.serg.android.ui.screens.createlobby

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewLobbyScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        isLoading: Boolean = false,
        onBack: () -> Unit = {},
        onSettings: () -> Unit = {},
        onCreateLobby: (Int, Boolean) -> Unit = { _, _ -> }
    ) {
        composeRule.setContent {
            NewLobbyScreen(
                onBack = onBack,
                onSettings = onSettings,
                isLoading = isLoading,
                onCreateLobby = onCreateLobby
            )
        }
    }

    // --- Rendering ---

    @Test
    fun displaysTitle() {
        setScreen()
        composeRule.onNodeWithText("Create New Lobby").assertIsDisplayed()
    }

    @Test
    fun displaysMaxPlayersSection() {
        setScreen()
        composeRule.onNodeWithText("Maximum Players").assertIsDisplayed()
    }

    @Test
    fun displaysAllPlayerCountOptions() {
        setScreen()
        listOf("2", "4", "6", "8").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun displaysPrivacyOptions() {
        setScreen()
        composeRule.onNodeWithText("Public").assertIsDisplayed()
        composeRule.onNodeWithText("Private").assertIsDisplayed()
    }

    @Test
    fun displaysGameSettingsSection() {
        setScreen()
        composeRule.onNodeWithText("Game Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Turn Timer").assertIsDisplayed()
        composeRule.onNodeWithText("Starting Tiles").assertIsDisplayed()
        composeRule.onNodeWithText("Win Score").assertIsDisplayed()
        composeRule.onNodeWithText("Quick Mode").assertIsDisplayed()
        composeRule.onNodeWithText("Require Initial Meld").assertIsDisplayed()
    }

    @Test
    fun displaysCreateButton() {
        setScreen()
        composeRule.onNodeWithText("Create Lobby").assertIsDisplayed()
    }

    // --- Top bar actions ---

    @Test
    fun clickingBackCallsOnBack() {
        var called = false
        setScreen(onBack = { called = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(called)
    }

    @Test
    fun clickingSettingsCallsOnSettings() {
        var called = false
        setScreen(onSettings = { called = true })

        composeRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue(called)
    }

    // --- Default submission values ---

    @Test
    fun clickingCreateLobby_withoutChanges_passesDefaultMaxPlayers() {
        var capturedMaxPlayers = 0
        setScreen(onCreateLobby = { maxPlayers, _ ->
            capturedMaxPlayers = maxPlayers
        })

        composeRule.onNodeWithText("Create Lobby").performClick()

        assertEquals(6, capturedMaxPlayers)
    }

    @Test
    fun clickingCreateLobby_withoutChanges_passesPublicByDefault() {
        var capturedIsPrivate = true
        setScreen(onCreateLobby = { _, isPrivate ->
            capturedIsPrivate = isPrivate
        })

        composeRule.onNodeWithText("Create Lobby").performClick()

        assertFalse(capturedIsPrivate)
    }

    // --- Selection affects submission ---

    @Test
    fun clickingCreateLobby_passesCorrectMaxPlayers() {
        var capturedMaxPlayers = 0
        setScreen(onCreateLobby = { maxPlayers, _ ->
            capturedMaxPlayers = maxPlayers
        })

        composeRule.onNodeWithText("4").performClick()
        composeRule.onNodeWithText("Create Lobby").performClick()

        assertEquals(4, capturedMaxPlayers)
    }

    @Test
    fun clickingCreateLobby_passesPrivateWhenSelected() {
        var capturedIsPrivate = false
        setScreen(onCreateLobby = { _, isPrivate ->
            capturedIsPrivate = isPrivate
        })

        composeRule.onNodeWithText("Private").performClick()
        composeRule.onNodeWithText("Create Lobby").performClick()

        assertTrue(capturedIsPrivate)
    }

    @Test
    fun clickingCreateLobby_passesPublicWhenPublicSelected() {
        var capturedIsPrivate = true
        setScreen(onCreateLobby = { _, isPrivate ->
            capturedIsPrivate = isPrivate
        })

        composeRule.onNodeWithText("Public").performClick()
        composeRule.onNodeWithText("Create Lobby").performClick()

        assertFalse(capturedIsPrivate)
    }

    // --- Loading state ---

    @Test
    fun loadingState_showsLoadingText() {
        setScreen(isLoading = true)
        composeRule.onNodeWithText("Loading").assertIsDisplayed()
    }

    @Test
    fun loadingState_buttonIsDisabled() {
        setScreen(isLoading = true)
        composeRule.onNodeWithText("Loading").assertIsNotEnabled()
    }

    @Test
    fun notLoading_buttonIsEnabled() {
        setScreen(isLoading = false)
        composeRule.onNodeWithText("Create Lobby").assertIsEnabled()
    }

    @Test
    fun loadingState_clickDoesNotCallOnCreateLobby() {
        var called = false
        setScreen(
            isLoading = true,
            onCreateLobby = { _, _ -> called = true }
        )

        composeRule.onNodeWithText("Loading").assertIsNotEnabled()

        assertFalse(called)
    }

    // --- Submission callback ---

    @Test
    fun clickingCreateLobby_callsOnCreateLobby() {
        var called = false
        setScreen(onCreateLobby = { _, _ -> called = true })

        composeRule.onNodeWithText("Create Lobby").performClick()

        assertTrue(called)
    }
}
