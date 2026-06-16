package at.aau.serg.android.ui.screens.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.screens.game.util.resolveDisplayedJokerLabel
import shared.models.game.domain.BoardSet
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile

@Composable
fun TileItem(
    tile: Tile,
    size: Int,
    selected: Boolean,
    moveHack: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onMoveRequest: () -> Unit,
    modifier: Modifier = Modifier,
    boardSet: BoardSet? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    val isJoker = tile is JokerTile
    val baseColor = Color(tile.color.colorInt)
    val glowColor = lerp(baseColor, Color.White, 0.6f)

    val borderWidth = when {
        selected -> 3.dp
        isJoker -> 2.dp
        else -> 0.dp
    }
    val borderColor = when {
        selected -> Color.White
        isJoker -> glowColor
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .width(size.dp)
            .height((size * 1.4f).dp)
            .then(
                if (isJoker) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = shape,
                        ambientColor = glowColor,
                        spotColor = glowColor
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .clickable {
                if (moveHack) {
                    onMoveRequest()
                } else {
                    onSelectedChange(!selected)
                }
            }
            .background(
                brush = if (isJoker) {
                    Brush.verticalGradient(listOf(glowColor, baseColor))
                } else {
                    Brush.verticalGradient(listOf(baseColor, baseColor))
                }
            )
            .border(width = borderWidth, color = borderColor, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        if (isJoker) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Joker",
                tint = Color.White,
                modifier = Modifier.size((size * 0.55f).dp)
            )

            val effectiveLabel = resolveDisplayedJokerLabel(boardSet, tile)
            if (effectiveLabel != "J") {
                Text(
                    text = effectiveLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size * 0.22f).sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 2.dp)
                )
            }
        } else {
            val label = (tile as NumberedTile).number.toString()

            Box {
                Text(
                    text = label,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = (size * 0.35f).sp,
                    style = LocalTextStyle.current.copy(
                        drawStyle = Stroke(
                            width = 4f,
                            join = StrokeJoin.Round
                        )
                    )
                )

                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size * 0.35f).sp
                )
            }
        }
    }
}
