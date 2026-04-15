package at.aau.serg.android.network.lobby

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import shared.models.lobby.request.*
import shared.models.lobby.response.*

interface LobbyService {
    @GET("lobbies")
    suspend fun getLobbies(): List<LobbyListItemResponse>

    @POST("lobbies")
    suspend fun createLobby(
        @Header("X-User-Id") userId: String,
        @Body request: CreateLobbyRequest
    ): LobbyResponse

    @GET("lobbies/{lobbyId}")
    suspend fun getLobby(
        @Path("lobbyId") lobbyId: String
    ): LobbyResponse

    @POST("lobbies/{lobbyId}/join")
    suspend fun joinLobby(
        @Path("lobbyId") lobbyId: String,
        @Body request: JoinLobbyRequest
    ): LobbyResponse

    @POST("lobbies/{lobbyId}/leave")
    suspend fun leaveLobby(
        @Header("X-User-Id") userId: String,
        @Path("lobbyId") lobbyId: String
    ): Response<Unit>

    @POST("lobbies/{lobbyId}/start")
    suspend fun startMatch(
        @Header("X-User-Id") userId: String,
        @Path("lobbyId") lobbyId: String
    ): Response<Unit>

}
