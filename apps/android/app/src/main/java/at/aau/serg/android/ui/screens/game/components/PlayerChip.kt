package at.aau.serg.android.ui.screens.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.theme.AccentGreen
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.appColors
import shared.models.game.domain.GamePlayer

@Composable
fun PlayerChip(
    player: GamePlayer,
    isActive: Boolean
) {
    val c = appColors()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val chipBg = if (isActive) AccentPurple.copy(alpha = 0.18f) else Color.Transparent
    val chipBorder = if (isActive) AccentPurple.copy(alpha = 0.55f) else c.game.boardBorder
    val avatarBg = if (isActive) AccentPurple.copy(alpha = 0.85f) else c.game.iconBtnBg
    val avatarTextColor = if (isActive) Color.White else c.game.iconBtnTint
    val nameColor = if (isActive) MaterialTheme.colorScheme.onSurface else c.game.iconBtnTint
    val nameFontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .background(chipBg, RoundedCornerShape(50.dp))
            .border(1.dp, chipBorder, RoundedCornerShape(50.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // avatar circle with pulsing dot overlay for active player
        Box(modifier = Modifier.size(22.dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(avatarBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.displayName.first().uppercaseChar().toString(),
                    color = avatarTextColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.BottomEnd)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .background(AccentGreen, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }

        Text(
            text = player.displayName,
            color = nameColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = nameFontWeight,
            maxLines = 1
        )
    }
}
