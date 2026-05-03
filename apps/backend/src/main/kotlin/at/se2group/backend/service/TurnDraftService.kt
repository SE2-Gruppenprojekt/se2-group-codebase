package at.se2group.backend.service

import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toDomain as toDraftDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TurnDraftService(
    private val gameRepository: GameRepository,
    private val turnDraftRepository: TurnDraftRepository
) {

    private companion object {
        const val GAME_NOT_FOUND = "Game not found"
        const val DRAFT_NOT_FOUND = "Draft not found"
        const val NOT_ACTIVE_PLAYER = "Not active player"
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
            ?: throw NoSuchElementException(DRAFT_NOT_FOUND)

        check(draftEntity.playerUserId == userId) { NOT_ACTIVE_PLAYER }

        return turnDraftRepository.save(
            request.toDraftDomain(gameId, userId).toEntity(draftEntity)
        ).toDomain()
    }
}
