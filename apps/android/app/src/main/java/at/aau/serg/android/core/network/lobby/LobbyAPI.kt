package at.aau.serg.android.core.network.lobby

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import shared.models.lobby.request.*
import shared.models.lobby.response.*

interface LobbyAPI {
    @GET("lobbies")
    suspend fun getLobbies(): List<LobbyListItemResponse>

    @POST("lobbies")
    suspend fun createLobby(
        @Body request: CreateLobbyRequest
    ): AuthenticatedLobbyResponse

    @GET("lobbies/{lobbyId}")
    suspend fun getLobby(
        @Path("lobbyId") lobbyId: String
    ): LobbyResponse

    @POST("lobbies/{lobbyId}/join")
    suspend fun joinLobby(
        @Path("lobbyId") lobbyId: String,
        @Body request: JoinLobbyRequest
    ): AuthenticatedLobbyResponse

    @POST("lobbies/{lobbyId}/leave")
    suspend fun leaveLobby(
        @Path("lobbyId") lobbyId: String
    )

    @POST("lobbies/{lobbyId}/ready")
    suspend fun ready(
        @Path("lobbyId") lobbyId: String
    ): LobbyResponse

    @POST("lobbies/{lobbyId}/unready")
    suspend fun unready(
        @Path("lobbyId") lobbyId: String
    ): LobbyResponse

    @POST("lobbies/{lobbyId}/start")
    suspend fun startMatch(
        @Path("lobbyId") lobbyId: String
    ): LobbyResponse
}
