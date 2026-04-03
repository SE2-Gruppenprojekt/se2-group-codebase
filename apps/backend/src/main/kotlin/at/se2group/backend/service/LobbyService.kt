package at.se2group.backend.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.LobbyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LobbyService(
    private val lobbyRepository: LobbyRepository
) {

    fun listOpenLobbies(): List<Lobby> {
        return lobbyRepository.findAllByStatus(LobbyStatus.OPEN)
            .map { it.toDomain() }
    }
}
