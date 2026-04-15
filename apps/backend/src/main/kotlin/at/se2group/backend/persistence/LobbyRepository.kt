package at.se2group.backend.persistence

import at.se2group.backend.domain.LobbyStatus
import org.springframework.data.jpa.repository.JpaRepository

interface LobbyRepository : JpaRepository<LobbyEntity, String> {
    fun findAllByStatus(status: LobbyStatus): List<LobbyEntity>
}
