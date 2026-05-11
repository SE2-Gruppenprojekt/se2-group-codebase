package at.se2group.backend.persistence

import shared.models.game.domain.GameStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "games")
class GameEntity(
    @Id
    @Column(name = "game_id")
    var gameId: String = "",

    @Column(name = "lobby_id", nullable = false)
    var lobbyId: String = "",

    @Column(name = "current_player_user_id", nullable = false)
    var currentPlayerUserId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: GameStatus = GameStatus.WAITING,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "started_at")
    var startedAt: Instant? = null,

    @Column(name = "finished_at")
    var finishedAt: Instant? = null,

    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "player_order")
    var players: MutableList<GamePlayerEntity> = mutableListOf(),

    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "board_set_order")
    var boardSets: MutableList<BoardSetEntity> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "game_draw_pile_tiles", joinColumns = [JoinColumn(name = "game_id")])
    @OrderColumn(name = "draw_pile_order")
    var drawPile: MutableList<TileEmbeddable> = mutableListOf()
)
