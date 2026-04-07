package at.aau.serg.android.network.lobby

import shared.models.lobby.response.*
import shared.models.lobby.request.*

class LobbyAPI(
    private val service: LobbyService
) {
    suspend fun getLobbies(): List<LobbyListItemResponse> {
        return service.getLobbies()
    }

    suspend fun joinLobby(lobbyId: String): LobbyResponse {
        return service.joinLobby(lobbyId)
    }

    suspend fun leaveLobby(lobbyId: String): Boolean {
        val response = service.leaveLobby(lobbyId)
        return response.isSuccessful
    }
}
