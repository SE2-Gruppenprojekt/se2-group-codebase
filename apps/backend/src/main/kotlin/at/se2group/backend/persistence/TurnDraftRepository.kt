package at.se2group.backend.persistence

import at.se2group.backend.domain.TurnDraft
import org.springframework.data.jpa.repository.JpaRepository

interface TurnDraftRepository : JpaRepository<TurnDraft, String> {
    fun findByGameId(gameId: String): TurnDraft?
}
