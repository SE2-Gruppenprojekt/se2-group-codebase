package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
}
