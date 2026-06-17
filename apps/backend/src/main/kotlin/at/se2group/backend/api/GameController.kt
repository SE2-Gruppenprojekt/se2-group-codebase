package at.se2group.backend.api

import shared.models.game.response.GameResponse
import at.se2group.backend.mapper.toResponse
import at.se2group.backend.service.GameService
import at.se2group.backend.service.TurnDraftService
import at.se2group.backend.service.DrawTileService
import at.se2group.backend.service.EndTurnService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PostMapping
import shared.models.game.request.UpdateDraftRequest
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import shared.models.game.response.TurnDraftResponse
import shared.models.game.request.EndTurnRequest
import jakarta.validation.Valid
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameService: GameService,
    private val turnDraftService: TurnDraftService,
    private val drawTileService: DrawTileService,
    private val endTurnService: EndTurnService
) {
    @GetMapping("/{gameId}")
    @SecurityRequirement(name = "bearerAuth")
    fun getGame(
        @PathVariable gameId: String,
        authentication: Authentication
    ): GameResponse {
        return gameService.getGameForUser(gameId, authentication.name).toResponse()
    }

    @PutMapping("/{gameId}/draft")
    @SecurityRequirement(name = "bearerAuth")
    fun updateDraft(
        @PathVariable gameId: String,
        authentication: Authentication,
        @Valid @RequestBody request: UpdateDraftRequest
    ): TurnDraftResponse {

        return turnDraftService.updateDraft(gameId, authentication.name, request).toResponse()
    }

    @PostMapping("/{gameId}/draw")
    @SecurityRequirement(name = "bearerAuth")
    fun drawTile(
        @PathVariable gameId: String,
        authentication: Authentication
    ): GameResponse {
        return drawTileService.drawTile(gameId, authentication.name).toResponse()
    }

    @PostMapping("/{gameId}/end-turn")
    @SecurityRequirement(name = "bearerAuth")
    fun endTurn(
        @PathVariable gameId: String,
        authentication: Authentication,
        @Valid @RequestBody request: EndTurnRequest
    ): GameResponse {
        return endTurnService.endTurn(gameId, authentication.name, request).toResponse()
    }

    @PostMapping("/{gameId}/reset-draft")
    @SecurityRequirement(name = "bearerAuth")
    fun resetDraft(
        @PathVariable gameId: String,
        authentication: Authentication
    ): TurnDraftResponse {
        return turnDraftService.resetDraft(gameId,authentication.name).toResponse()
    }
}
