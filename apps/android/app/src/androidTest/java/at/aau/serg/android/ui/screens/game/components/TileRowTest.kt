package at.aau.serg.android.ui.screens.game.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor

class TileRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val tiles = listOf(
        NumberedTile("t1", TileColor.RED, 5),
        NumberedTile("t2", TileColor.BLUE, 7)
    )

    // --- borderColor branches ---

    @Test
    fun tileRow_renders_withBorderColor() {
        composeRule.setContent {
            TileRow(
                onEvent = {},
                tiles = tiles,
                tileSize = 44,
                selectedTiles = emptySet(),
                borderColor = Color.Gray
            )
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun tileRow_renders_withNoBorderColor() {
        composeRule.setContent {
            TileRow(
                onEvent = {},
                tiles = tiles,
                tileSize = 44,
                selectedTiles = emptySet(),
                borderColor = null
            )
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    // --- moveHack branches (selectedTiles non-empty, rowId matches / doesn't match) ---

    @Test
    fun tileRow_renders_selectedTile_differentRow() {
        val selected = tiles.first()
        composeRule.setContent {
            TileRow(
                onEvent = {},
                tiles = tiles,
                tileSize = 44,
                selectedTiles = setOf(selected),
                borderColor = Color.Blue,
                rowId = "row1",
                selectedRow = "row2"      // moveHack = true
            )
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun tileRow_renders_selectedTile_sameRow() {
        val selected = tiles.first()
        composeRule.setContent {
            TileRow(
                onEvent = {},
                tiles = tiles,
                tileSize = 44,
                selectedTiles = setOf(selected),
                borderColor = Color.Blue,
                rowId = "row1",
                selectedRow = "row1"      // moveHack = false
            )
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun tileRow_renders_emptyTiles_noBorderColor() {
        composeRule.setContent {
            TileRow(
                onEvent = {},
                tiles = emptyList(),
                tileSize = 44,
                selectedTiles = emptySet(),
                borderColor = null
            )
        }
        composeRule.onRoot().assertIsDisplayed()
    }
}
