package at.aau.serg.android.network.lobby

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

    suspend fun joinLobby(lobbyId: String): LobbyResponse {
        return service.joinLobby(lobbyId)
    }

    suspend fun leaveLobby(userId: String, lobbyId: String): Boolean {
        val response = service.leaveLobby(userId, lobbyId)
        return response.isSuccessful
    }
}
