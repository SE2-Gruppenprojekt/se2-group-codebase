package at.se2group.backend.api

import at.se2group.backend.dto.GameResponse
import at.se2group.backend.mapper.toResponse
import at.se2group.backend.service.GameService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.domain.TurnDraft

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameService: GameService
) {
    @GetMapping("/{gameId}")
    fun getGame(@PathVariable gameId: String): GameResponse {
        return gameService.getGame(gameId).toResponse()
    }

    @PutMapping("/{gameId}/draft")
    fun updateDraft(
        @PathVariable gameId: String,
        @RequestBody request: UpdateDraftRequest
    ): TurnDraft {

        val userId = "mock-user"

        return gameService.updateDraft(gameId, userId, request)
    }
}
