package at.aau.serg.android.ui.screens.game

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import at.aau.serg.android.ui.theme.ThemeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor

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
                    NumberedTile("x1", TileColor.RED, 1),
                    NumberedTile("x2", TileColor.BLUE, 2)
                ),
                boardSets = listOf(
                    BoardSet(
                        boardSetId = "row1",
                        tiles = listOf(
                            NumberedTile("x3", TileColor.BLACK, 10),
                            NumberedTile("x4", TileColor.RED, 10)
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

        val selectedTile = NumberedTile("x1", TileColor.RED, 5)

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
                NumberedTile("x1", TileColor.RED, 1),
                NumberedTile("x2", TileColor.RED, 2),
                NumberedTile("x3", TileColor.RED, 3)
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
