package at.se2group.backend.persistence

import org.springframework.data.jpa.repository.JpaRepository
import shared.models.lobby.domain.LobbyStatus

interface LobbyRepository : JpaRepository<LobbyEntity, String> {
    fun findAllByStatus(status: LobbyStatus): List<LobbyEntity>
}
