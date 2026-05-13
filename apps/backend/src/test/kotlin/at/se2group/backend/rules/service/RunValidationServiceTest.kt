package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.domain.Tile
import shared.models.game.domain.JokerTile


class RunValidationServiceTest {
    private val service = RunValidationService()

    private fun set(vararg tiles: Tile): BoardSet =
        BoardSet(
            boardSetId = "set-1",
            tiles = tiles.toList()
        )

    private fun tile(id: String, color: TileColor, number: Int): NumberedTile =
        NumberedTile(tileId = id, color = color, number = number)

    private fun joker(id: String, color: TileColor): JokerTile =
        JokerTile(tileId = id, color = color)


    @Test
    fun `validate 3 tile run with consecutive numbers and same color`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 3),
                tile("tile-2", TileColor.RED, 4),
                tile("tile-3", TileColor.RED, 5)

            )
        )
        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `validate 4 tile run with consecutive numbers and same color`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.BLUE, 10),
                tile("tile-2", TileColor.BLUE, 11),
                tile("tile-3", TileColor.BLUE, 12),
                tile("tile-4", TileColor.BLUE, 13)
            )
        )
        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `reject run if tiles are less than 3`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 7),
                tile("tile-2", TileColor.RED, 8)
            )
        )
        assertFalse(result.isValid)
        assertEquals("RUN_MIN_SIZE", result.violations.single().code)
        assertEquals("Run must contain at least 3 tiles", result.violations.single().message)
    }

    @Test
    fun `rejects runs if tiles colors are mixed`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 3),
                tile("tile-2", TileColor.BLUE, 4),
                tile("tile-3", TileColor.RED, 5)
            )
        )
        assertFalse(result.isValid)
        assertEquals("RUN_COLOR_MISMATCH", result.violations.single().code)
        assertEquals("All run tiles must have the same color", result.violations.single().message)
    }

    @Test
    fun`rejects run containing duplicate numbers`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 7),
                tile("tile-2", TileColor.RED, 7),
                tile("tile-3", TileColor.RED, 8)
            )
        )
        assertFalse(result.isValid)
        assertEquals("RUN_DUPLICATE_NUMBER", result.violations.single().code)
        assertEquals("Run tiles must not have duplicate numbers", result.violations.single().message)
    }

    @Test
    fun `rejects run with a gap in the sequence`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 3),
                tile("tile-2", TileColor.RED, 5),
                tile("tile-3", TileColor.RED, 6)
            )
        )
        assertFalse(result.isValid)
        assertEquals("RUN_NOT_CONSECUTIVE", result.violations.single().code)
        assertEquals("Run tiles must create a consecutive ascending sequence", result.violations.single().message)
    }


    @Test
    fun `accept run if tiles are submitted with mixed order numbers`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 4),
                tile("tile-2", TileColor.RED, 6),
                tile("tile-3", TileColor.RED, 5)
            )
        )
        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `rejects run if consecutive sequence number fails after sorting`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.BLUE, 4),
                tile("tile-2", TileColor.BLUE, 7),
                tile("tile-3", TileColor.BLUE, 8)
            )
        )
        assertFalse(result.isValid)
        assertEquals("RUN_NOT_CONSECUTIVE", result.violations.single().code)
    }

    @Test
    fun `rejects run containing joker`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 3),
                tile("tile-2", TileColor.RED, 4),
                joker("tile-3", TileColor.BLACK),
            )
        )

        assertFalse(result.isValid)
        assertEquals("RUN_JOKER_NOT_SUPPORTED", result.violations.single().code)
        assertEquals("Joker support is not implemented yet", result.violations.single().message)
    }

    @Test
    fun `reject duplicate number violation before non-consecutive validation`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 3),
                tile("tile-2", TileColor.RED, 3),
                tile("tile-3", TileColor.RED, 5)
            )
        )
        assertFalse(result.isValid)
        assertEquals("RUN_DUPLICATE_NUMBER", result.violations.single().code)
    }

}
