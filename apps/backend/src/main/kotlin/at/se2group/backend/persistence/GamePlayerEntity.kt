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

/**
 * JPA persistence model for a player participating in a game.
 *
 * Stores both the mutable game state of the player (rack, score, meld status)
 * and the aggregated gameplay metrics used for post-game statistics.
 */
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

    /**
     * Number of completed turns taken by this player.
     */
    @Column(name = "turns_completed", nullable = false)
    var turnsCompleted: Int = 0,

    /**
     * Number of tiles played from the rack onto the board.
     */
    @Column(name = "tiles_played", nullable = false)
    var tilesPlayed: Int = 0,

    /**
     * Number of newly created board sets.
     *
     * Modifying or extending existing board sets does not increase this metric.
     */
    @Column(name = "melds_created", nullable = false)
    var meldsCreated: Int = 0,

    /**
     * Sum of tile values played from the rack.
     *
     * Joker handling follows the metric scoring rules rather than end-game penalty rules.
     */
    @Column(name = "points_played", nullable = false)
    var pointsPlayed: Int = 0,

    /**
     * Number of tiles remaining in the player's rack when the game ended.
     */
    @Column(name = "tiles_remaining_at_end")
    var tilesRemainingAtEnd: Int? = null,

    /**
     * End-game penalty score derived from the remaining rack tiles.
     */
    @Column(name = "penalty_points_at_end")
    var penaltyPointsAtEnd: Int? = null,

    @Column(name = "winner", nullable = false)
    var winner: Boolean = false,

    /**
     * Final ranking position after game completion.
     *
     * A value of 1 indicates the winner.
     */
    @Column(name = "finish_position")
    var finishPosition: Int? = null,

    /**
     * Tiles currently held by the player.
     *
     * The persisted order matches the rack order shown in the UI.
     */
    @ElementCollection
    @CollectionTable(name = "game_player_rack_tiles", joinColumns = [JoinColumn(name = "game_player_id")])
    @OrderColumn(name = "rack_tile_order")
    var rackTiles: MutableList<TileEmbeddable> = mutableListOf()
)
