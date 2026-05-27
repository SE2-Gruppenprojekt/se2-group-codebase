package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import shared.models.game.domain.TileColor

class GroupValidationServiceTest {

    private val service = GroupValidationService()

    private fun set(vararg tiles: Tile): BoardSet =
        BoardSet(
            boardSetId = "set-1",
            tiles = tiles.toList()
        )

    private fun tile(
        id: String,
        color: TileColor,
        number: Int
    ): NumberedTile =
        NumberedTile(
            tileId = id,
            color = color,
            number = number
        )

    private fun joker(
        id: String,
        color: TileColor
    ): JokerTile =
        JokerTile(
            tileId = id,
            color = color
        )

    @Test
    fun `validates 3 tile group with same number and unique colors`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 7),
                tile("tile-2", TileColor.BLUE, 7),
                tile("tile-3", TileColor.BLACK, 7)
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `validates 4 tile group with same number and unique colors`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 10),
                tile("tile-2", TileColor.BLUE, 10),
                tile("tile-3", TileColor.BLACK, 10),
                tile("tile-4", TileColor.ORANGE, 10)
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `rejects group with fewer than 3 tiles`() {
        val set = set(
            tile("tile-1", TileColor.RED, 7),
            tile("tile-2", TileColor.BLUE, 7)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_MIN_SIZE", violation.code)
        assertEquals("Group must contain at least 3 tiles", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.map { it.tileId }, violation.tileIds)
    }

    @Test
    fun `rejects group with more than 4 tiles`() {
        val set = set(
            tile("tile-1", TileColor.RED, 7),
            tile("tile-2", TileColor.BLUE, 7),
            tile("tile-3", TileColor.BLACK, 7),
            tile("tile-4", TileColor.ORANGE, 7),
            tile("tile-5", TileColor.RED, 7)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_MAX_SIZE", violation.code)
        assertEquals("Group must contain at most 4 tiles", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.map { it.tileId }, violation.tileIds)
    }

    @Test
    fun `rejects group with mixed tile numbers`() {
        val set = set(
            tile("tile-1", TileColor.RED, 7),
            tile("tile-2", TileColor.BLUE, 7),
            tile("tile-3", TileColor.BLACK, 8)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_NUMBER_MISMATCH", violation.code)
        assertEquals("All group tiles must have the same number", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.map { it.tileId }, violation.tileIds)
    }

    @Test
    fun `rejects group with duplicate colors`() {
        val set = set(
            tile("tile-1", TileColor.RED, 7),
            tile("tile-2", TileColor.RED, 7),
            tile("tile-3", TileColor.BLUE, 7)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_DUPLICATE_COLOR", violation.code)
        assertEquals("Group tiles must have unique colors", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.map { it.tileId }, violation.tileIds)
    }

    @Test
    fun `validates group with joker filling missing color`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 7),
                tile("tile-2", TileColor.BLUE, 7),
                joker("tile-3", TileColor.BLACK)
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `validates group with two jokers and one numbered anchor tile`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 10),
                joker("tile-2", TileColor.BLACK),
                joker("tile-3", TileColor.RED)
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `validates size four group with joker`() {
        val result = service.validate(
            set(
                tile("tile-1", TileColor.RED, 5),
                tile("tile-2", TileColor.BLUE, 5),
                tile("tile-3", TileColor.BLACK, 5),
                joker("tile-4", TileColor.RED)
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `rejects all joker group`() {
        val set = set(
            joker("tile-1", TileColor.RED),
            joker("tile-2", TileColor.BLACK),
            joker("tile-3", TileColor.RED)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_ALL_JOKERS_NOT_ALLOWED", violation.code)
        assertEquals("All-joker groups are not allowed", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.map { it.tileId }, violation.tileIds)
    }

    @Test
    fun `rejects joker group with duplicate non joker colors`() {
        val set = set(
            tile("tile-1", TileColor.RED, 7),
            tile("tile-2", TileColor.RED, 7),
            joker("tile-3", TileColor.BLACK)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_DUPLICATE_COLOR", violation.code)
        assertEquals("Group tiles must have unique colors", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.filterIsInstance<NumberedTile>().map { it.tileId }, violation.tileIds)
    }

    @Test
    fun `rejects joker group with mismatching non joker numbers`() {
        val set = set(
            tile("tile-1", TileColor.RED, 7),
            tile("tile-2", TileColor.BLUE, 8),
            joker("tile-3", TileColor.BLACK)
        )

        val result = service.validate(set)

        val violation = result.violations.single()
        assertFalse(result.isValid)
        assertEquals("GROUP_NUMBER_MISMATCH", violation.code)
        assertEquals("All group tiles must have the same number", violation.message)
        assertEquals("set-1", violation.boardSetId)
        assertEquals(set.tiles.filterIsInstance<NumberedTile>().map { it.tileId }, violation.tileIds)
    }
}
