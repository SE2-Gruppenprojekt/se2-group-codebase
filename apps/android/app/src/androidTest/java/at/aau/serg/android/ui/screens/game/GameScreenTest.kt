package at.aau.serg.android.ui.screens.game

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.match.domain.BoardSet
import shared.models.match.domain.NumberedTile
import shared.models.match.domain.TileColor
import at.aau.serg.android.ui.theme.ThemeState

class GameScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: GameViewModel

    private lateinit var stateFlow: MutableStateFlow<GameUiState>

    @Before
    fun setup() {

        viewModel = mockk(relaxed = true)

        stateFlow = MutableStateFlow(
            GameUiState(
                rackTiles = listOf(
                    NumberedTile(TileColor.RED, 1),
                    NumberedTile(TileColor.BLUE, 2)
                ),
                boardSets = listOf(
                    BoardSet(
                        boardSetId = "row1",
                        tiles = listOf(
                            NumberedTile(TileColor.BLACK, 10),
                            NumberedTile(TileColor.RED, 10)
                        )
                    )
                )
            )
        )

        every { viewModel.uiState } returns stateFlow
    }

    @After
    fun teardown() {
        ThemeState.isDarkMode.value = false
    }

    @Test
    fun gameScreen_displaysMainComponents() {

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(GameTestTags.SCREEN)
            .assertExists()

        composeRule
            .onNodeWithTag(GameTestTags.HEADER)
            .assertExists()

        composeRule
            .onNodeWithTag(GameTestTags.BOARD)
            .assertExists()

        composeRule
            .onNodeWithTag(GameTestTags.RACK)
            .assertExists()
    }

    @Test
    fun gameScreen_displaysStaticTexts() {

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithText("Game #4821")
            .assertExists()

        composeRule
            .onNodeWithText("Round 2 of 3")
            .assertExists()

        composeRule
            .onNodeWithText("3:45")
            .assertExists()

        composeRule
            .onNodeWithText("2 Your Tiles")
            .assertExists()

        composeRule
            .onNodeWithText("End Turn")
            .assertExists()
    }

    @Test
    fun gameScreen_displaysActionButtons() {

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(GameTestTags.ACTION_END_TURN)
            .assertExists()
            .assertHasClickAction()

        composeRule
            .onNodeWithTag(GameTestTags.ACTION_ADD)
            .assertExists()
            .assertHasClickAction()

        composeRule
            .onNodeWithTag(GameTestTags.ACTION_RESET)
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun gameScreen_callsSettingsCallback() {

        val onSettings = mockk<() -> Unit>(relaxed = true)

        composeRule.setContent {
            GameScreen(
                viewModel = viewModel,
                onSettings = onSettings
            )
        }

        composeRule
            .onNode(hasClickAction() and hasAnyAncestor(hasTestTag(GameTestTags.HEADER)))
            .onChildren()
            .filter(hasClickAction())
            .onLast()
            .performClick()

        verify(exactly = 1) {
            onSettings.invoke()
        }
    }

    @Test
    fun gameScreen_callsBackCallback() {

        val onBack = mockk<() -> Unit>(relaxed = true)

        composeRule.setContent {
            GameScreen(
                viewModel = viewModel,
                onBack = onBack
            )
        }

        composeRule
            .onAllNodes(hasClickAction())
            .onFirst()
            .performClick()

        verify {
            onBack.invoke()
        }
    }

    @Test
    fun gameScreen_showsPlaceholder_whenTilesSelected() {

        val selectedTile = NumberedTile(TileColor.RED, 5)

        stateFlow.value = stateFlow.value.copy(
            selectedTiles = setOf(selectedTile)
        )

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(GameTestTags.BOARD)
            .assertExists()

        composeRule
            .onAllNodes(hasClickAction())

    }

    @Test
    fun gameScreen_rendersDarkMode() {

        ThemeState.isDarkMode.value = true

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(GameTestTags.SCREEN)
            .assertExists()

        composeRule
            .onNodeWithTag(GameTestTags.HEADER)
            .assertExists()

        composeRule
            .onNodeWithTag(GameTestTags.RACK)
            .assertExists()
    }

    @Test
    fun gameScreen_actionButtons_canBeClicked() {

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithTag(GameTestTags.ACTION_END_TURN)
            .performClick()

        composeRule
            .onNodeWithTag(GameTestTags.ACTION_ADD)
            .performClick()

        composeRule
            .onNodeWithTag(GameTestTags.ACTION_RESET)
            .performClick()
    }

    @Test
    fun gameScreen_updatesRackCount() {

        stateFlow.value = stateFlow.value.copy(
            rackTiles = listOf(
                NumberedTile(TileColor.RED, 1),
                NumberedTile(TileColor.RED, 2),
                NumberedTile(TileColor.RED, 3)
            )
        )

        composeRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        composeRule
            .onNodeWithText("3 Your Tiles")
            .assertExists()
    }
}
