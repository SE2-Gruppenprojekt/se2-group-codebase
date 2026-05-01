package at.aau.serg.android.ui.screens.game

import android.graphics.Color.green
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.BackButton
import at.aau.serg.android.ui.screens.game.components.TileItem
import at.aau.serg.android.ui.screens.game.components.TileRow
import at.aau.serg.android.ui.screens.game.components.TileRowPlaceholder
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val dark = ThemeState.isDarkMode.value

    val boardBorder = if (dark) Color.White.copy(alpha = 0.2f) else Color(0xFF86EFAC)

    val iconBtnBg = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val iconBtnTint = if (dark) Color.White else Color(0xFF475569)

    val uiState by viewModel.uiState.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
    ) {
        Column(Modifier.fillMaxSize()) {

            // HEADER
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(if (dark) Color(0xFF1E293B) else Color.White)
                    .padding(12.dp)
            ) {
                BackButton(onBack = onBack)

                Column(Modifier.align(Alignment.Center)) {
                    Text("Game #4821", fontWeight = FontWeight.Bold)
                    Text("Round 2 of 3", fontSize = 12.sp)
                }

                Row(Modifier.align(Alignment.CenterEnd)) {
                    Text("3:45")
                    IconButton(onClick = onSettings) {
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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.boardSets) { boardSet ->
                            TileRow(
                                tiles = boardSet.tiles,
                                tileSize = 60,
                                borderColor = boardBorder,
                                selectedTiles = uiState.selectedTiles,
                                selectedRow = uiState.activeSelectionRow,
                                rowId = boardSet.boardSetId,
                                onSelectionChange = { tile, selected, rowId ->
                                    viewModel.onTileSelected(tile, selected, rowId)
                                },
                                onRowClick = { clickedRowId ->
                                    viewModel.moveTiles(boardSetId = clickedRowId)
                                }
                            )
                        }

                        if (uiState.selectedTiles.isNotEmpty()) {
                            item {
                                TileRowPlaceholder(
                                    tileSize = 60,
                                    onClick = { viewModel.addRow() }
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
                    ) {
                        Column {

                            Text("${uiState.rackTiles.size} Your Tiles")

                            Spacer(Modifier.height(12.dp))

                            val scrollState = rememberScrollState()

                            val shape = RoundedCornerShape(12.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .clip(shape)
                                    .border(2.dp, Color.Gray, shape)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),

                            ) {

                                val tiles = uiState.rackTiles

                                tiles.forEachIndexed { index, tile ->

                                    var isDragging by remember { mutableStateOf(false) }
                                    var offsetX by remember { mutableStateOf(0f) }

                                    val density = LocalDensity.current
                                    val tileWidthPx = with(density) { 60.dp.toPx() }

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX.toInt(), 0) }
                                            .zIndex(if (isDragging) 1f else 0f)
                                            .pointerInput(tiles){

                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        isDragging = true
                                                    },

                                                    onDragEnd = {

                                                        isDragging = false

                                                        val targetIndex =
                                                            (index + (offsetX / tileWidthPx).toInt())
                                                                .coerceIn(0, tiles.lastIndex)

                                                        viewModel.moveTileInRack(index, targetIndex)

                                                        offsetX = 0f
                                                    },

                                                    onDragCancel = {
                                                        isDragging = false
                                                        offsetX = 0f
                                                    },

                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        offsetX += dragAmount.x
                                                    }
                                                )
                                            }
                                    ) {
                                        TileItem(
                                            tile = tile,
                                            size = 44,
                                            selected = tile in uiState.selectedTiles,
                                            moveHack = false,
                                            onSelectedChange = { selected ->
                                                viewModel.onTileSelected(tile, selected)
                                            },
                                            onMoveRequest = {}
                                        )
                                    }
                                }

                            }

                            Spacer(Modifier.height(12.dp))


                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D3CFF))
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text("End Turn", fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(iconBtnBg)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = iconBtnTint)
                                }

                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(iconBtnBg)
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
