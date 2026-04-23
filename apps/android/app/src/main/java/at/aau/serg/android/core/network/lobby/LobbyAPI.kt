package at.aau.serg.android.core.network.lobby

import shared.models.lobby.response.*
import shared.models.lobby.request.*

class LobbyAPI(
    private val service: LobbyService
) {
    suspend fun getLobbies(): List<LobbyListItemResponse> {
        return service.getLobbies()
    }

    suspend fun getLobby(lobbyId: String) : LobbyResponse {
        return service.getLobby(lobbyId)
    }

    suspend fun createLobby(userId: String, lobbyRequest: CreateLobbyRequest) : LobbyResponse {
        return service.createLobby(userId, lobbyRequest)
    }

    suspend fun joinLobby(lobbyId: String, request: JoinLobbyRequest): LobbyResponse {
        return service.joinLobby(lobbyId, request)
    }

    suspend fun leaveLobby(userId: String, lobbyId: String): Boolean {
        val response = service.leaveLobby(userId, lobbyId)
        return response.isSuccessful
    }

    suspend fun startMatch(userId: String, lobbyId: String): Boolean {
        val response = service.startMatch(userId, lobbyId)
        return response.isSuccessful
    }

}
