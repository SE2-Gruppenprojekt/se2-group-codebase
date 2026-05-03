package at.se2group.backend.game.service

import at.se2group.backend.persistence.*
import at.se2group.backend.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*
import at.se2group.backend.service.GameService

class GameServiceTest {

    private val gameRepository: GameRepository = mock()
    private val turnDraftRepository: TurnDraftRepository = mock()

    private val gameService = GameService(
        gameRepository,
        turnDraftRepository
    )

    private fun mockGame(
        players: List<String>,
        currentPlayer: String
    ): GameEntity {
        val game = GameEntity(
            gameId = "g1",
            lobbyId = "l1",
            currentPlayerUserId = currentPlayer,
            status = GameStatus.ACTIVE,
            createdAt = java.time.Instant.now(),
            startedAt = java.time.Instant.now(),
            finishedAt = null,
            players = players.mapIndexed { i, id ->
                GamePlayerEntity(
                    game = null,
                    userId = id,
                    displayName = id,
                    turnOrder = i,
                    score = 0,
                    hasCompletedInitialMeld = false,
                    joinedAt = java.time.Instant.now(),
                    rackTiles = mutableListOf()
                )
            }.toMutableList(),
            boardSets = mutableListOf(),
            drawPile = mutableListOf()
        )
        return game
    }

    private fun mockDraft(): TurnDraftEntity {
        return TurnDraftEntity(
            gameId = "g1",
            playerUserId = "u1",
            rackTiles = mutableListOf(),
            boardSets = mutableListOf()
        )
    }
}
