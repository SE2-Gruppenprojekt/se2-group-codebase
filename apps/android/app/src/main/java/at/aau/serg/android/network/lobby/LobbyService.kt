package at.aau.serg.android.network.lobby

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import shared.models.lobby.request.*
import shared.models.lobby.response.*

interface LobbyService {
    @GET("lobbies")
    suspend fun getLobbies(): List<LobbyListItemResponse>

    @POST("lobbies/{lobbyId}/join")
    suspend fun joinLobby(
        @Path("lobbyId") lobbyId: String
    ): LobbyResponse

    @POST("lobbies/{lobbyId}/leave")
    suspend fun leaveLobby(
        @Path("lobbyId")lobbyId: String
    ): Response<Unit>
}
