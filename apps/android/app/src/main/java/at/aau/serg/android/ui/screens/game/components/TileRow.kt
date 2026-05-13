package at.aau.serg.android.ui.screens.game.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.game.GameViewModel
import shared.models.game.domain.Tile
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.*


@Composable
fun TileRow(
    viewModel: GameViewModel,
    tiles: List<Tile>,
    tileSize: Int,
    selectedTiles: Set<Tile>,
    selectedRow: String? = null,
    borderColor: Color? = null,
    rowId: String? = null,
    onSelectionChange: (Tile, Boolean, String?) -> Unit,
    onRowClick: (String?) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.moveInSameRow(rowId, from.index, to.index)
    }
    val shape = RoundedCornerShape(12.dp)

    val baseModifier = Modifier
        .then(
            if (borderColor != null) {
                Modifier
                    .clip(shape)
                    .border(2.dp, borderColor, shape)
            } else {
                Modifier
            }
        )
        .clickable { onRowClick(rowId) }
        .padding(8.dp)

    LazyRow(
        modifier = baseModifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        state = lazyListState
    ) {
        items(
            items = tiles,
            key = { it.tileId }) {
            ReorderableItem(
                state = reorderableLazyListState,
                key = it.tileId
            ) { isDragging ->

                TileItem(
                    tile = it,
                    size = tileSize,
                    selected = it in selectedTiles,
                    moveHack = selectedTiles.isNotEmpty() && rowId != selectedRow,
                    onSelectedChange = { selected ->
                        onSelectionChange(it, selected, rowId)
                    },
                    onMoveRequest = {
                        onRowClick(rowId)
                    },
                    modifier = Modifier
                        .longPressDraggableHandle()
                        .graphicsLayer {
                            alpha = if (isDragging) 0.6f else 1f
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                        }
                )
            }
        }
    }
}

