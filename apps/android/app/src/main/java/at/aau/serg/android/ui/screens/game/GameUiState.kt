package at.aau.serg.android.ui.screens.game

import at.aau.serg.android.core.errors.ApiRuleViolation
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import shared.models.game.domain.BoardSet
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.Tile

data class RuleValidationUiState(
    val violationsByBoardSetId: Map<String, List<ApiRuleViolation>> = emptyMap(),
    val globalViolations: List<ApiRuleViolation> = emptyList(),
    val summaryMessage: String? = null
)

data class GameResultPlayerSummary(
    val userId: String,
    val displayName: String,
    val score: Int,
    val finishPosition: Int = 0,
    val tilesPlayed: Int = 0,
    val meldsCreated: Int = 0,
    val turnsCompleted: Int = 0,
    val pointsFromTiles: Int = 0,
    val remainingTiles: Int = 0,
    val penaltyPoints: Int = 0,
    val isStillPlaying: Boolean = false
)

data class GameResultUiModel(
    val winnerUserId: String,
    val players: List<GameResultPlayerSummary>,
    val matchDuration: String = "0:00",
    val totalTurns: Int = 0,
    val finishedTimestamp: String? = null
)

data class GameUiState(
    val loadState: LoadState = LoadState.Success,
    val user: User? = null,
    val rackTiles: List<Tile> = emptyList(),
    val boardSets: List<BoardSet> = emptyList(),
    val selectedTiles: Set<Tile> = emptySet(),
    val activeSelectionRow: String? = null,
    val gameState: ConfirmedGame? = null,
    val isActivePlayer: Boolean = false,
    val ruleValidation: RuleValidationUiState = RuleValidationUiState(),
    val gameResult: GameResultUiModel? = null
)
