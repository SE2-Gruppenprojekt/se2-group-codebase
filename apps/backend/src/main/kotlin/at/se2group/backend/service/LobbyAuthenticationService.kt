package at.se2group.backend.service

import at.se2group.backend.dto.AuthenticatedLobbyResponse
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.mapper.toResponse
import at.se2group.backend.security.JwtService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LobbyAuthenticationService(
    private val lobbyService: LobbyService,
    private val jwtService: JwtService
) {
    fun createAuthenticatedLobby(request: CreateLobbyRequest): AuthenticatedLobbyResponse {
        val userId = UUID.randomUUID().toString()
        val lobby = lobbyService.createLobby(userId, request)
        return AuthenticatedLobbyResponse(
            accessToken = jwtService.issueAccessToken(userId),
            lobby = lobby.toResponse()
        )
    }

    fun joinAuthenticatedLobby(lobbyId: String, request: JoinLobbyRequest): AuthenticatedLobbyResponse {
        val userId = UUID.randomUUID().toString()
        val lobby = lobbyService.joinLobby(lobbyId, userId, request)
        return AuthenticatedLobbyResponse(
            accessToken = jwtService.issueAccessToken(userId),
            lobby = lobby.toResponse()
        )
    }
}
