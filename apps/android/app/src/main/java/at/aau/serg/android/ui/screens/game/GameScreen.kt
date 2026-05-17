package at.aau.serg.android.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import at.aau.serg.android.ui.screens.game.components.TileRow
import at.aau.serg.android.ui.screens.game.components.TileRowPlaceholder
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GameScreenContent(
        uiState = uiState,
        onEvent = viewModel::onUIEvent
    )
}

@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onEvent: (GameUIEvent) -> Unit
) {
    val dark = ThemeState.isDarkMode.value

    val boardBorder = if (dark) Color.White.copy(alpha = 0.2f) else Color(0xFF86EFAC)
    val iconBtnBg = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val iconBtnTint = if (dark) Color.White else Color(0xFF475569)

    Box(
        Modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
            .testTag(GameTestTags.SCREEN)
    ) {
        Column(Modifier.fillMaxSize()) {

            // HEADER
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(if (dark) Color(0xFF1E293B) else Color.White)
                    .padding(12.dp)
                    .testTag(GameTestTags.HEADER)
            ) {
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

            // CONTENT
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                Column(Modifier.fillMaxSize()) {
                    // BOARD
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (dark) Color(0xFF1E293B) else Color.White)
                            .padding(12.dp)
                            .testTag(GameTestTags.BOARD),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.boardSets) { boardSet ->
                            TileRow(
                                onEvent,
                                tiles = boardSet.tiles,
                                tileSize = 60,
                                borderColor = boardBorder,
                                selectedTiles = uiState.selectedTiles,
                                selectedRow = uiState.activeSelectionRow,
                                rowId = boardSet.boardSetId,
                            )
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
                            .background(if (dark) Color(0xFF1E293B) else Color.White)
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
                                borderColor = boardBorder,
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D3CFF))
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
                                        .background(iconBtnBg)
                                        .testTag(GameTestTags.ACTION_ADD)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = iconBtnTint)
                                }

                                IconButton(
                                    onClick = {
                                        onEvent(GameUIEvent.ResetSelection)
                                    },
                                    enabled = uiState.isActivePlayer,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(iconBtnBg)
                                        .testTag(GameTestTags.ACTION_RESET)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = iconBtnTint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
