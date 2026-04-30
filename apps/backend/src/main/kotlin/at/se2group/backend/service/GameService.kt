package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.persistence.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toDomain as toDraftDomain
import at.se2group.backend.mapper.toEntity

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
        val draftDomain = request.toDraftDomain(gameId, userId)

        val updatedEntity = draftDomain.toEntity(draftEntity)

        val saved = turnDraftRepository.save(updatedEntity)

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

        draft.playerUserId = nextPlayer.userId

        draft.boardTiles = game.boardSets
            .flatMap { it.tiles }
            .toMutableList()

        draft.rackTiles = nextPlayer.rackTiles
            .toMutableList()

        turnDraftRepository.save(draft)

        return savedGame.toDomain()
    }

    @Transactional
    fun resetDraft(gameId: String, userId: String): TurnDraft {

        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game not found") }

        val draft = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException("Draft not found")

        // nur aktiver Spieler darf resetten
        if (game.currentPlayerUserId != userId) {
            throw IllegalStateException("Not active player")
        }

        val player = game.players.first { it.userId == userId }

        val draftDomain = TurnDraft(
            gameId = gameId,
            playerUserId = userId,
            boardSets = game.boardSets.map { it.toDomain() },
            rackTiles = player.rackTiles.map { it.toDomain() }
        )

        val updated = draftDomain.toEntity(draft)

        val saved = turnDraftRepository.save(updated)

        return saved.toDomain()
    }
}
