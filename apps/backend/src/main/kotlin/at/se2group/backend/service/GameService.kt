package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.dto.UpdateDraftRequest

@Service
@Transactional(readOnly = true)
class GameService(
    private val gameRepository: GameRepository
) {
    fun getGame(gameId: String): ConfirmedGame {
        return gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game not found") }
            .toDomain()
    }
    @Transactional
    fun updateDraft(gameId: String, userId: String, request: UpdateDraftRequest
    ): TurnDraft {

        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game not found") }
            .toDomain()

        if (game.currentPlayerUserId != userId) {
            throw SecurityException("Not your turn")
        }

        return TurnDraft(
            gameId = gameId,
            playerUserId = userId
        )
    }
}
