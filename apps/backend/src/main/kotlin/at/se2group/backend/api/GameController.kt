package at.se2group.backend.api

import shared.models.game.response.GameResponse
import at.se2group.backend.mapper.toResponse
import at.se2group.backend.service.GameService
import at.se2group.backend.service.TurnDraftService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import shared.models.game.request.UpdateDraftRequest
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import shared.models.game.response.TurnDraftResponse
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameService: GameService,
    private val turnDraftService: TurnDraftService
) {
    @GetMapping("/{gameId}")
    fun getGame(@PathVariable gameId: String): GameResponse {
        return gameService.getGame(gameId).toResponse()
    }

    @PutMapping("/{gameId}/draft")
    fun updateDraft(
        @PathVariable gameId: String,
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: UpdateDraftRequest
    ): TurnDraftResponse {

        return turnDraftService.updateDraft(gameId, userId, request).toResponse()
    }
}
