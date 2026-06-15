package at.aau.serg.android.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.BackButton
import at.aau.serg.android.ui.screens.game.components.BoardSetValidationMessage
import at.aau.serg.android.ui.screens.game.components.GlobalValidationBanner
import at.aau.serg.android.ui.screens.game.components.PlayerChip
import at.aau.serg.android.ui.screens.game.components.TileRow
import at.aau.serg.android.ui.screens.game.components.TileRowPlaceholder
import at.aau.serg.android.ui.theme.NotReadyRed
import at.aau.serg.android.ui.theme.appColors

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBack: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    GameScreenContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                GameUIEvent.OnBack -> onBack?.invoke() ?: viewModel.onUIEvent(event)
                GameUIEvent.OnSettings -> onSettings?.invoke() ?: viewModel.onUIEvent(event)
                else -> viewModel.onUIEvent(event)
            }
        }
    )
}

@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onEvent: (GameUIEvent) -> Unit
) {
    val c = appColors()

    Box(
        Modifier
            .fillMaxSize()
            .background(c.game.background)
            .testTag(GameTestTags.SCREEN)
    ) {
        Column(Modifier.fillMaxSize()) {

            // HEADER
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(c.game.surface)
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
                    .testTag(GameTestTags.HEADER)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    BackButton(onBack = { onEvent(GameUIEvent.OnBack) })

                    Column(Modifier.align(Alignment.Center)) {
                        Text("Game #4821", fontWeight = FontWeight.Bold)
                        Text("Round 2 of 3", fontSize = 12.sp)
                    }

                    Row(Modifier.align(Alignment.CenterEnd)) {
                        Text("3:45")
                        IconButton(onClick = { onEvent(GameUIEvent.OnSettings) }) {
                            Icon(Icons.Default.Settings, null)
                        }
                    }
                }

                // player bar — sorted by turn order, active player highlighted
                val players = uiState.gameState?.players.orEmpty()
                    .sortedBy { it.turnOrder }
                val currentPlayerId = uiState.gameState?.currentPlayerUserId

                if (players.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag(GameTestTags.PLAYER_BAR),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(players) { player ->
                            PlayerChip(
                                player = player,
                                isActive = player.userId == currentPlayerId
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                }
            }

            // GLOBAL VALIDATION BANNER
            if (uiState.ruleValidation.globalViolations.isNotEmpty()) {
                GlobalValidationBanner(
                    summaryMessage = uiState.ruleValidation.summaryMessage,
                    violations = uiState.ruleValidation.globalViolations
                )
            }

            // CONTENT
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(c.game.background)
            ) {
                Column(Modifier.fillMaxSize()) {
                    // BOARD
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(c.game.surface)
                            .padding(12.dp)
                            .testTag(GameTestTags.BOARD),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.boardSets) { boardSet ->
                            val rowViolations = uiState.ruleValidation
                                .violationsByBoardSetId[boardSet.boardSetId]
                                .orEmpty()
                            val isInvalid = rowViolations.isNotEmpty()

                            Column {
                                TileRow(
                                    onEvent,
                                    tiles = boardSet.tiles,
                                    tileSize = 60,
                                    borderColor = if (isInvalid) NotReadyRed else c.game.boardBorder,
                                    selectedTiles = uiState.selectedTiles,
                                    selectedRow = uiState.activeSelectionRow,
                                    rowId = boardSet.boardSetId,
                                    boardSet = boardSet,
                                )

                                if (isInvalid) {
                                    BoardSetValidationMessage(violations = rowViolations)
                                }
                            }
                        }

                        if (uiState.selectedTiles.isNotEmpty()) {
                            item {
                                TileRowPlaceholder(
                                    tileSize = 60,
                                    onClick = { onEvent(GameUIEvent.AddRow) }
                                )
                            }
                        }
                    }

                    // HAND
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(c.game.surface)
                            .padding(16.dp)
                            .testTag(GameTestTags.RACK)
                    ) {
                        Column {

                            Text("${uiState.rackTiles.size} Your Tiles")

                            Spacer(Modifier.height(12.dp))

                            TileRow(
                                onEvent,
                                tiles = uiState.rackTiles,
                                tileSize = 44,
                                borderColor = c.game.boardBorder,
                                selectedTiles = uiState.selectedTiles,
                                selectedRow = uiState.activeSelectionRow,
                                rowId = null
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Button(
                                    onClick = {
                                        onEvent(GameUIEvent.EndTurn)
                                    },
                                    enabled = uiState.isActivePlayer,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag(GameTestTags.ACTION_END_TURN),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = c.game.endTurnButton)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text("End Turn", fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                IconButton(
                                    onClick = {
                                        onEvent(GameUIEvent.DrawTile)
                                    },
                                    enabled = uiState.isActivePlayer,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(c.game.iconBtnBg)
                                        .testTag(GameTestTags.ACTION_ADD)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = c.game.iconBtnTint)
                                }

                                IconButton(
                                    onClick = {
                                        onEvent(GameUIEvent.ResetSelection)
                                    },
                                    enabled = uiState.isActivePlayer,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(c.game.iconBtnBg)
                                        .testTag(GameTestTags.ACTION_RESET)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = c.game.iconBtnTint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
