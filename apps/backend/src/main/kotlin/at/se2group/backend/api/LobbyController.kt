package at.se2group.backend.api

import at.se2group.backend.dto.LobbyListItemResponse
import at.se2group.backend.mapper.toListItemResponse
import at.se2group.backend.service.LobbyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/lobby")
class LobbyController(
    private val lobbyService: LobbyService
) {

    @GetMapping
    fun listLobbies(): List<LobbyListItemResponse> {
        return lobbyService.listOpenLobbies()
            .map { it.toListItemResponse() }
    }
}
