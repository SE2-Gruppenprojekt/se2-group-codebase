package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.persistence.TurnDraftRepository

@Service
@Transactional(readOnly = true)
class GameService(
    private val gameRepository: GameRepository,
    private val turnDraftRepository: TurnDraftRepository
) {

    fun getGame(gameId: String): ConfirmedGame {
        return gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game not found") }
            .toDomain()
    }

    @Transactional
    fun updateDraft(
        gameId: String,
        userId: String,
        request: UpdateDraftRequest
    ): TurnDraft {

        gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game not found") }

        val draftEntity = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException("Draft not found")

        if (draftEntity.playerUserId != userId) {
            throw IllegalStateException("Not active player")
        }

        val saved = turnDraftRepository.save(draftEntity)

        return TurnDraft(
            gameId = saved.gameId,
            playerUserId = saved.playerUserId
        )
    }

    @Transactional
    fun endTurn(gameId: String, userId: String): ConfirmedGame {

        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game not found") }

        val draft = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException("Draft not found")

        if (game.currentPlayerUserId != userId) {
            throw IllegalStateException("Not active player")
        }

        // NEXT PLAYER bestimmen
        val players = game.players
        val currentIndex = players.indexOfFirst { it.userId == userId }
        val nextPlayer = players[(currentIndex + 1) % players.size]

        game.currentPlayerUserId = nextPlayer.userId

        val savedGame = gameRepository.save(game)

        // neuen Draft erstellen (minimal)
        turnDraftRepository.save(draft)

        return savedGame.toDomain()
    }
}
