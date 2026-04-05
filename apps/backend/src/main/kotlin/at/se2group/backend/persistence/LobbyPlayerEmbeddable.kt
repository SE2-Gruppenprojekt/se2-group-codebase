package at.se2group.backend.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class LobbyPlayerEmbeddable(
    @Column(name = "user_id")
    var userId: String = "",

    @Column(name = "display_name")
    var displayName: String = ""
)
