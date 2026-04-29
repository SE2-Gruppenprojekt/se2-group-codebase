package at.se2group.backend.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface GameRepository : JpaRepository<GameEntity, String> {
}
