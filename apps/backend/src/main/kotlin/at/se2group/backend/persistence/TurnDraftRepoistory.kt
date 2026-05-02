package at.se2group.backend.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TurnDraftRepository : JpaRepository<TurnDraftEntity, String> {
    fun findByGameId(gameId: String): TurnDraftEntity?
}
