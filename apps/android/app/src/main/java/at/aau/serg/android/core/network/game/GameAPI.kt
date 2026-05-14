package at.aau.serg.android.core.network.game

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import shared.models.game.request.DrawTileRequest
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.UpdateDraftRequest
import shared.models.game.response.GameResponse
import shared.models.game.response.TurnDraftResponse

interface GameAPI {

    @GET("games/{gameId}")
    suspend fun loadGame(
        @Path("gameId") gameId: String
    ): GameResponse

    @GET("games/{gameId}/draft")
    suspend fun getTurnDraft(
        @Path("gameId") gameId: String
    ): TurnDraftResponse

    @POST("games/{gameId}/draft")
    suspend fun updateDraft(
        @Path("gameId") gameId: String,
        @Body request: UpdateDraftRequest
    ): TurnDraftResponse

    @POST("games/{gameId}/draw")
    suspend fun drawTile(
        @Path("gameId") gameId: String,
        @Body request: DrawTileRequest
    ): GameResponse

    @POST("games/{gameId}/end-turn")
    suspend fun endTurn(
        @Path("gameId") gameId: String,
        @Body request: EndTurnRequest
    ): GameResponse
}
