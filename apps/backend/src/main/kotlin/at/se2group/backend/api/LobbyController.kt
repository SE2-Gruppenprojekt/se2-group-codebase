package at.se2group.backend.api

import at.se2group.backend.dto.AuthenticatedLobbyResponse
import at.se2group.backend.dto.LobbyListItemResponse
import at.se2group.backend.mapper.toListItemResponse
import at.se2group.backend.service.LobbyService
import at.se2group.backend.service.LobbyAuthenticationService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PatchMapping
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.LobbyResponse
import at.se2group.backend.mapper.toResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import jakarta.validation.Valid
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/api/lobbies")
class LobbyController(
    private val lobbyService: LobbyService,
    private val lobbyAuthenticationService: LobbyAuthenticationService
) {

    private fun currentGameIdFor(lobbyId: String): String? =
        lobbyService.findCurrentGameId(lobbyId)

    @GetMapping
    fun listLobbies(): List<LobbyListItemResponse> {
        return lobbyService.listOpenLobbies()
            .map { it.toListItemResponse() }
    }

    @PostMapping
    fun createLobby(
       @Valid @RequestBody request: CreateLobbyRequest
    ): AuthenticatedLobbyResponse {
        return lobbyAuthenticationService.createAuthenticatedLobby(request)
    }

    @GetMapping("/{lobbyId}")
    @SecurityRequirement(name = "bearerAuth")
    fun getLobby(
        @PathVariable lobbyId: String,
        authentication: Authentication
    ): LobbyResponse {
        return lobbyService.getLobbyForUser(lobbyId, authentication.name)
            .toResponse(currentGameId = currentGameIdFor(lobbyId))
    }
    @PostMapping("/{lobbyId}/join")
    fun joinLobby(
        @PathVariable lobbyId: String,
        @Valid @RequestBody request: JoinLobbyRequest
    ): AuthenticatedLobbyResponse {
        return lobbyAuthenticationService.joinAuthenticatedLobby(lobbyId, request)
    }

    @PatchMapping("/{lobbyId}/settings")
    @SecurityRequirement(name = "bearerAuth")
    fun updateLobbySettings(
        @PathVariable lobbyId: String,
        authentication: Authentication,
        @Valid @RequestBody request: UpdateLobbySettingsRequest
    ): LobbyResponse {
        return lobbyService.updateLobbySettings(lobbyId, authentication.name, request)
            .toResponse(currentGameId = currentGameIdFor(lobbyId))
    }

    @PostMapping("/{lobbyId}/start")
    @SecurityRequirement(name = "bearerAuth")
    fun startLobby(
        @PathVariable lobbyId: String,
        authentication: Authentication
    ): LobbyResponse {
        return lobbyService.startLobby(lobbyId, authentication.name)
            .toResponse(currentGameId = currentGameIdFor(lobbyId))
    }

    @PostMapping("/{lobbyId}/leave")
    @SecurityRequirement(name = "bearerAuth")
    fun leaveLobby(
        @PathVariable lobbyId: String,
        authentication: Authentication,
        ): ResponseEntity<Any> {
        val updatedLobby = lobbyService.leaveLobby(lobbyId, authentication.name)

        return if (updatedLobby == null) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(updatedLobby.toResponse(currentGameId = currentGameIdFor(updatedLobby.lobbyId)))
        }
    }

    @PostMapping("/{lobbyId}/ready")
    @SecurityRequirement(name = "bearerAuth")
    fun readyLobby(
        @PathVariable lobbyId: String,
        authentication: Authentication,
    ): LobbyResponse {
        return lobbyService.readyLobby(lobbyId, authentication.name)
            .toResponse(currentGameId = currentGameIdFor(lobbyId))
    }

    @PostMapping("/{lobbyId}/unready")
    @SecurityRequirement(name = "bearerAuth")
    fun unreadyLobby(
        @PathVariable lobbyId: String,
        authentication: Authentication
     ): LobbyResponse {
        return lobbyService.unreadyLobby(lobbyId, authentication.name)
            .toResponse(currentGameId = currentGameIdFor(lobbyId))
    }

    @DeleteMapping("/{lobbyId}")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteLobby(
        @PathVariable lobbyId: String,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        lobbyService.deleteLobby(lobbyId, authentication.name)
        return ResponseEntity.noContent().build()
    }
}
