package at.aau.serg.android.ui.screens.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shared.models.match.domain.JokerTile
import shared.models.match.domain.NumberedTile
import shared.models.match.domain.Tile

@Composable
fun TileItem(
    tile: Tile,
    size: Int,
    selected: Boolean,
    moveHack: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onMoveRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(size.dp)
            .height((size * 1.4f).dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (moveHack) {
                    onMoveRequest()
                } else {
                    onSelectedChange(!selected)
                }
            }
            .background(
                /*if (selected) Color(tile.color.colorInt).copy(alpha = 0.1f)
                else Color(tile.color.colorInt)*/
                color = Color(tile.color.colorInt)
            )
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Text(
                text = when (tile) {
                    is NumberedTile -> tile.number.toString()
                    is JokerTile -> "0"
                },
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
                text = when (tile) {
                    is NumberedTile -> tile.number.toString()
                    is JokerTile -> "0"
                },
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size * 0.35f).sp
            )
        }
    }
}
