package at.se2group.backend.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name= "game_players")
class GamePlayerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    var game: GameEntity? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String = "",

    @Column(name = "display_name", nullable = false)
    var displayName: String = "",

    @Column(name = "turn_order", nullable = false)
    var turnOrder: Int = 0,

    @Column(name = "has_completed_initial_meld", nullable = false)
    var hasCompletedInitialMeld: Boolean = false,

    @Column(name = "score", nullable = false)
    var score: Int = 0,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant = Instant.now(),

    @Column(name = "turns_completed", nullable = false)
    var turnsCompleted: Int = 0,

    @Column(name = "tiles_played", nullable = false)
    var tilesPlayed: Int = 0,

    @Column(name = "melds_created", nullable = false)
    var meldsCreated: Int = 0,

    @Column(name = "points_played", nullable = false)
    var pointsPlayed: Int = 0,

    @Column(name = "tiles_remaining_at_end")
    var tilesRemainingAtEnd: Int? = null,

    @Column(name = "penalty_points_at_end")
    var penaltyPointsAtEnd: Int? = null,

    @Column(name = "winner", nullable = false)
    var winner: Boolean = false,

    @Column(name = "finish_position")
    var finishPosition: Int? = null,

    @ElementCollection
    @CollectionTable(name = "game_player_rack_tiles", joinColumns = [JoinColumn(name = "game_player_id")])
    @OrderColumn(name = "rack_tile_order")
    var rackTiles: MutableList<TileEmbeddable> = mutableListOf()
)
