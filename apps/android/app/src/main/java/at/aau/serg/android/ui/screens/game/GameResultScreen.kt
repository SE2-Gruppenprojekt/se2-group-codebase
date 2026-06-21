package at.aau.serg.android.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ResBackground    = Color(0xFF0D0B1E)
private val ResSurface       = Color(0xFF1A1740)
private val ResCard          = Color(0xFF1D1A38)
private val ResCardHighlight = Color(0xFF2A2560)
private val ResAccent        = Color(0xFF7B61FF)
private val ResGold          = Color(0xFFFFD700)
private val ResGreen         = Color(0xFF4CAF50)
private val ResRed           = Color(0xFFFF5252)
private val ResGray          = Color(0xFF9E9E9E)
private val ResStillPlaying  = Color(0xFF4ECDC4)

@Composable
fun GameResultScreen(
    gameResult: GameResultUiModel?,
    currentUserId: String? = null,
    onNavigateHome: () -> Unit = {},
) {
    val context = LocalContext.current
    val players = gameResult?.players.orEmpty()
    val winnerUserId = gameResult?.winnerUserId.orEmpty()
    val matchDuration = gameResult?.matchDuration ?: "0:00"
    val isGameOver = gameResult?.isGameOver != false
    val currentPlayer = players.firstOrNull { it.userId == currentUserId }
    val isWinner = currentUserId != null && currentUserId == winnerUserId
    val finishedCount = players.count { !it.isStillPlaying }

    val shareText = buildString {
        appendLine("Rummikub — Game Results")
        appendLine("Duration: $matchDuration")
        appendLine()
        players.forEach { player ->
            val medal = when (player.finishPosition) {
                1 -> "🏆"; 2 -> "🥈"; 3 -> "🥉"; else -> "   "
            }
            appendLine("$medal ${player.displayName} — ${player.score} pts")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A1060), ResBackground))
            )
            .testTag(GameTestTags.RESULT_SCREEN)
    ) {

        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Game #4821", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
            Text("Play Time: $matchDuration", color = Color.White, fontSize = 13.sp)
        }

        // --- HERO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "GAME OVER" badge
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        if (isGameOver) "GAME OVER" else "IN PROGRESS",
                        color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Trophy (winner) or medal (finished)
            Icon(
                imageVector = if (isWinner) Icons.Filled.EmojiEvents else Icons.Filled.MilitaryTech,
                contentDescription = null,
                tint = if (isWinner) ResGold else Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = when {
                    isGameOver && isWinner -> "YOU WIN!"
                    isGameOver -> "YOU FINISHED"
                    currentPlayer?.isStillPlaying == false -> "YOU FINISHED!"
                    else -> "PLAYER FINISHED"
                },
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.testTag(GameTestTags.RESULT_TITLE)
            )

            Spacer(Modifier.height(4.dp))
            Text("RESULTS", color = ResGray, fontSize = 11.sp, letterSpacing = 2.sp)

            Spacer(Modifier.height(12.dp))

            // Players finished pill
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    "Players $finishedCount/${players.size} Finished",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Stats row (current player's stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultStatBox("Duration", matchDuration, Modifier.weight(1f))
                ResultStatBox("Moves", "${currentPlayer?.turnsCompleted ?: gameResult?.totalTurns ?: 0}", Modifier.weight(1f))
                ResultStatBox("Points", "+${currentPlayer?.pointsFromTiles ?: 0}", Modifier.weight(1f))
                ResultStatBox("Melds", "${currentPlayer?.meldsCreated ?: 0}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))
        }

        // --- LEADERBOARD (scrollable) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(players) { index, player ->
                PlayerResultCard(
                    player = player,
                    rank = index + 1,
                    isCurrentUser = player.userId == currentUserId,
                    isWinner = player.userId == winnerUserId,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // --- BOTTOM PLAYER STATS (fixed above action bar) ---
        if (currentPlayer != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ResSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultStatBox(
                    label = "Tiles Played",
                    value = "+${currentPlayer.tilesPlayed}",
                    modifier = Modifier.weight(1f),
                    valueColor = ResGreen
                )
                ResultStatBox(
                    label = "Sets Created",
                    value = "${currentPlayer.meldsCreated}",
                    modifier = Modifier.weight(1f)
                )

            }
        }

        // --- BOTTOM ACTIONS (fixed) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ResSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateHome,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag(GameTestTags.RESULT_HOME_BUTTON),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResAccent)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Back to Home", fontWeight = FontWeight.Bold, color = Color.White)
            }

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Results"))
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ResultStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = valueColor)
        Text(label, fontSize = 10.sp, color = ResGray)
    }
}

@Composable
private fun PlayerResultCard(
    player: GameResultPlayerSummary,
    rank: Int,
    isCurrentUser: Boolean,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrentUser) ResCardHighlight else ResCard)
            .then(
                if (isCurrentUser) Modifier.border(1.5.dp, ResAccent, RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank badge + avatar
        Box(contentAlignment = Alignment.TopStart) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ResSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = ResGray, modifier = Modifier.size(24.dp))
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(ResAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("$rank", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Name + stats line
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isCurrentUser) "You" else player.displayName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = if (isWinner) Modifier.testTag(GameTestTags.RESULT_WINNER_NAME) else Modifier
                )
                val placeBadge = when (player.finishPosition) {
                    1 -> Pair("🏆 1st Place", ResGold)
                    2 -> Pair("🥈 2nd Place", Color(0xFFB0BEC5))
                    3 -> Pair("🥉 3rd Place", Color(0xFFCD7F32))
                    else -> if (player.isStillPlaying) Pair("Still Playing", ResStillPlaying) else null
                }
                if (placeBadge != null) {
                    Surface(shape = RoundedCornerShape(50), color = placeBadge.second.copy(alpha = 0.15f)) {
                        Text(
                            placeBadge.first,
                            fontSize = 10.sp,
                            color = placeBadge.second,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(3.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${player.remainingTiles} tiles left", fontSize = 11.sp, color = ResGray)
                Text("•", fontSize = 11.sp, color = ResGray)
                Text("${player.meldsCreated} melds", fontSize = 11.sp, color = ResGray)
                if (player.penaltyPoints > 0) {
                    Text("•", fontSize = 11.sp, color = ResGray)
                    Text("-${player.penaltyPoints} penalty", fontSize = 11.sp, color = ResRed)
                }
                if (isWinner) {
                    Text("•", fontSize = 11.sp, color = ResGray)
                    Text("Bonus +50", fontSize = 11.sp, color = ResGreen)
                }
            }
        }

        // Score
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${player.score}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.White
            )
            Text("points", fontSize = 10.sp, color = ResGray)
        }
    }
}
