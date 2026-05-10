package at.se2group.backend.game.service
import at.se2group.backend.service.DrawTileService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows


class DrawTileServiceTest {
    private val drawTileService = DrawTileService()

    @Test
    fun `drawTile throws now implemented exception`() {
        val exception = assertThrows(UnsupportedOperationException::class.java) {
            drawTileService.drawTile("game-1", "user-1")
        }
        assertEquals("Draw tile is not implemented yet.", exception.message)
    }
}
