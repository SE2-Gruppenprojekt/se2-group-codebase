package at.aau.serg.android.core.network.game

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import shared.models.game.request.DrawTileRequest
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.UpdateDraftRequest
import shared.models.game.response.GameResponse
import shared.models.game.response.TurnDraftResponse

class GameServiceTest {

    private val api = mockk<GameAPI>()
    private val service = GameService(api)

    @Test
    fun getGame_returnsFromApi() = runBlocking {
        val expected = mockk<GameResponse>()

        coEvery { api.getGame("game123") } returns expected

        val result = service.getGame("game123")

        assertEquals(expected, result)
    }

    @Test
    fun loadDraft_returnsFromApi() = runBlocking {
        val expected = mockk<TurnDraftResponse>()

        coEvery { api.getTurnDraft("game123") } returns expected

        val result = service.loadDraft("game123")

        assertEquals(expected, result)
    }

    @Test
    fun updateDraft_returnsFromApi() = runBlocking {
        val request = mockk<UpdateDraftRequest>()
        val expected = mockk<TurnDraftResponse>()

        coEvery { api.updateDraft("game123", request) } returns expected

        val result = service.updateDraft("game123", request)

        assertEquals(expected, result)
    }

    @Test
    fun drawTile_returnsFromApi() = runBlocking {
        val expected = mockk<GameResponse>()

        coEvery {
            api.drawTile("game123", DrawTileRequest("player1"))
        } returns expected

        val result = service.drawTile("game123", "player1")

        assertEquals(expected, result)
    }

    @Test
    fun endTurn_returnsFromApi() = runBlocking {
        val expected = mockk<GameResponse>()

        coEvery {
            api.endTurn("game123", EndTurnRequest("player1"))
        } returns expected

        val result = service.endTurn("game123", "player1")

        assertEquals(expected, result)
    }
}
