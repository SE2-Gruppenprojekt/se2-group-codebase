package at.se2group.backend.persistence

import at.se2group.backend.domain.LobbyStatus
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "lobbies")
class LobbyEntity(
    @Id
    @Column(name = "lobby_id")
    var lobbyId: String = "",

    @Column(name = "host_user_id", nullable = false)
    var hostUserId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: LobbyStatus = LobbyStatus.OPEN,

    @Column(name = "max_players", nullable = false)
    var maxPlayers: Int = 4,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column(name = "allow_guests", nullable = false)
    var allowGuests: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @ElementCollection
    @CollectionTable(name = "lobby_players", joinColumns = [JoinColumn(name = "lobby_id")])
    var players: MutableList<LobbyPlayerEmbeddable> = mutableListOf()
)
