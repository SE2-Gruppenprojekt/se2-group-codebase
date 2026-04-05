package at.se2group.backend.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.Instant

@Embeddable
data class LobbyPlayerEmbeddable(
    @Column(name = "user_id")
    var userId: String = "",

    @Column(name = "display_name")
    var displayName: String = "",

    @Column(name = "is_ready")
    var isReady: Boolean = false,

    @Column(name = "joined_at")
    var joinedAt: Instant = Instant.now()
)
