package at.aau.serg.android.core.network.game

import shared.models.game.request.DrawTileRequest
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.UpdateDraftRequest
import shared.models.game.response.GameResponse
import shared.models.game.response.TurnDraftResponse

class GameService(
    private val api: GameAPI
) {

    suspend fun loadGame(gameId: String): GameResponse {
        return api.loadGame(gameId)
    }

    suspend fun loadDraft(gameId: String): TurnDraftResponse {
        return api.getTurnDraft(gameId)
    }

    suspend fun updateDraft(
        gameId: String,
        playerId: String,
        request: UpdateDraftRequest
    ): TurnDraftResponse {
        return api.updateDraft(
            gameId,
            playerId,
            request
        )
    }

    suspend fun drawTile(
        gameId: String,
        playerId: String
    ): GameResponse {
        return api.drawTile(
            gameId,
            playerId
        )
    }

    suspend fun endTurn(
        gameId: String,
        playerId: String,
        request: EndTurnRequest
    ) {
        api.endTurn(
            gameId,
            playerId,
            request
        )
    }
}
