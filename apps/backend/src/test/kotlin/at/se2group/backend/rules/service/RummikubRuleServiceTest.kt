package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GameStatus
import shared.models.game.domain.TurnDraft

class RummikubRuleServiceTest {

    private val ruleService = RummikubRuleService()

    @Test
    fun `validateSubmittedDraft returns valid empty result for foundation skeleton`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0
        )
        val confirmedGame = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(player),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE
        )
        val submittedDraft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        val result = ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }
}
