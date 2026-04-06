package at.se2group.backend.api

import at.se2group.backend.dto.LobbyListItemResponse
import at.se2group.backend.mapper.toListItemResponse
import at.se2group.backend.service.LobbyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PatchMapping
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.LobbyResponse
import at.se2group.backend.mapper.toResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.PathVariable
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping

@RestController
@RequestMapping("/api/lobbies")
class LobbyController(
    private val lobbyService: LobbyService
) {

    @GetMapping
    fun listLobbies(): List<LobbyListItemResponse> {
        return lobbyService.listOpenLobbies()
            .map { it.toListItemResponse() }
    }

    @PostMapping
    fun createLobby(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: CreateLobbyRequest
    ): LobbyResponse {
        return lobbyService.createLobby(userId, request)
            .toResponse()
    }

    @GetMapping("/{lobbyId}")
    fun getLobby(@PathVariable lobbyId: String): LobbyResponse {
        return lobbyService.getLobby(lobbyId).toResponse()
    }
    @PostMapping("/{lobbyId}/join")
    fun joinLobby(
        @PathVariable lobbyId: String,
        @RequestBody request: JoinLobbyRequest
    ): LobbyResponse {
        return lobbyService.joinLobby(lobbyId, request).toResponse()
    }

    @PatchMapping("/{lobbyId}/settings")
    fun updateLobbySettings(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: UpdateLobbySettingsRequest
    ): LobbyResponse {
        return lobbyService.updateLobbySettings(lobbyId, userId, request).toResponse()
    }

    @PostMapping("/{lobbyId}/start")
    fun startLobby(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): LobbyResponse {
        return lobbyService.startLobby(lobbyId, userId).toResponse()
    }

    @DeleteMapping("/{lobbyId}")
    fun deleteLobby(
        @PathVariable lobbyId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        lobbyService.deleteLobby(lobbyId, userId)
        return ResponseEntity.noContent().build()
    }
}
