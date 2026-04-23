package at.aau.serg.android.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import at.aau.serg.android.ui.components.BackButton
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.theme.ThemeState

// hardcoded tile colors by number (placeholder)
private fun tileColor(number: Int): Brush {
    return when (number % 7) {
        0 -> Brush.verticalGradient(listOf(Color(0xFFEC4899), Color(0xFFA855F7)))
        1 -> Brush.verticalGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A)))
        2 -> Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
        3 -> Brush.verticalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7)))
        4 -> Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
        5 -> Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFEAB308)))
        else -> Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
    }
}

@Composable
fun GameScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val darkMode = ThemeState.isDarkMode.value

    // colors
    val bgColor = if (darkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val headerBg = if (darkMode) Color(0xFF1E293B) else Color.White
    val headerBorder = if (darkMode) Color.Transparent else Color(0xFFE2E8F0)
    val primaryText = if (darkMode) Color.White else Color(0xFF1E293B)
    val secondaryText = if (darkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (darkMode) Color(0xFF1E293B) else Color.White
    val cardBorder = if (darkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val boardBg = if (darkMode) Color(0xFF0F2D27) else Color(0xFFDCFCE7)
    val boardBorder = if (darkMode) Color.White.copy(alpha = 0.2f) else Color(0xFF86EFAC)
    val handBg = if (darkMode) Color(0xFF1E293B) else Color.White
    val iconBtnBg = if (darkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val iconBtnTint = if (darkMode) Color.White else Color(0xFF475569)
    val timerColor = if (darkMode) Color(0xFFF97316) else Color(0xFFEA580C)

    // placeholder data
    val boardSets = listOf(
        listOf(7, 8, 9, 10, 11),
        listOf(5, 5, 5)
    )
    val myTiles = listOf(3, 11, 7, 9, 12, 4, 10)
    val players = listOf(
        Triple("Player2", "14 tiles", 127),
        Triple("Player3", "18 tiles", 89)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .border(width = 1.dp, color = headerBorder)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BackButton(
                onBack = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
                tint = primaryText
            )

            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(text = "Game #4821",
                    color = primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp)
                Text(text = "Round 2 of 3",
                    color = secondaryText,
                    fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3:45",
                    color = primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = primaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // PLAYER CARDS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            players.forEach { (name, tiles, score) ->
                PlayerCard(
                    name = name,
                    tiles = tiles,
                    score = score,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // BOARD
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(boardBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                boardSets.forEach { set ->
                    TileRow(tiles = set, borderColor = boardBorder)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // PLAYER HAND
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(handBg)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${myTiles.size}  Your Tiles",
                    color = primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp)
                Text(
                    text = "Turn ends in:  0:45",
                    color = timerColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(myTiles) { number ->
                    TileItem(number = number, size = 44)
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

@Composable
private fun PlayerCard(
    name: String,
    tiles: String,
    score: Int,
    cardBg: Color,
    cardBorder: Color,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBorder),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 16.sp)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(name, color = primaryText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(tiles, color = secondaryText, fontSize = 11.sp)
            }
        }
        Text(
            text = score.toString(),
            color = primaryText,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun TileRow(tiles: List<Int>, borderColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tiles.forEach { number ->
            TileItem(number = number, size = 60)
        }
    }
}

@Composable
private fun TileItem(number: Int, size: Int) {
    Box(
        modifier = Modifier
            .width(size.dp)
            .height((size * 1.4f).dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tileColor(number)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size * 0.35f).sp
        )
    }
}
