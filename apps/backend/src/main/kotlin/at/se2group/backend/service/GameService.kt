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

    private companion object {
        const val GAME_NOT_FOUND = "Game not found"
        const val DRAFT_NOT_FOUND = "Draft not found"
        const val NOT_ACTIVE_PLAYER = "Not active player"
    }

    fun getGame(gameId: String): ConfirmedGame {
        return gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }
            .toDomain()
    }

    @Transactional
    fun updateDraft(
        gameId: String,
        userId: String,
        request: UpdateDraftRequest
    ): TurnDraft {

        gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }

        val draftEntity = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException (DRAFT_NOT_FOUND)

        if (draftEntity.playerUserId != userId) {
            throw IllegalStateException(NOT_ACTIVE_PLAYER)
        }
        val draftDomain = request.toDraftDomain(gameId, userId)

        val updatedEntity = draftDomain.toEntity(draftEntity)

        val saved = turnDraftRepository.save(updatedEntity)

        return saved.toDomain()

    }

}
