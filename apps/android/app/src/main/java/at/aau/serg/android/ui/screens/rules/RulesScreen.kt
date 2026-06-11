package at.aau.serg.android.ui.screens.rules

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.components.BackButton
import at.aau.serg.android.ui.theme.AccentBlue
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.AccentYellow
import at.aau.serg.android.ui.theme.RulesKeyRulesGreen
import at.aau.serg.android.ui.theme.appColors
import shared.models.game.domain.TileColor

@Composable
fun RulesScreen(
    onBack: () -> Unit = {}
) {
    RulesScreenContent(onBack = onBack)
}

@Composable
fun RulesScreenContent(
    onBack: () -> Unit
) {
    val c = appColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.settings.background)
            .testTag(RulesTestTags.SCREEN)
    ) {

        // header section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = listOf(AccentPurple, AccentBlue))
                )
                .padding(16.dp)
        ) {
            BackButton(
                onBack = onBack,
                tint = Color.White,
                modifier = Modifier.testTag(RulesTestTags.BACK_BUTTON)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // master class badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MASTER CLASS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HOW TO PLAY",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "RUMMIKUB",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Master the art of sets and runs to clear your rack and become the champion.",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // rules content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag(RulesTestTags.CONTENT)
        ) {

            // The Objective section
            RulesSectionHeader(
                icon = Icons.Filled.GpsFixed,
                iconBg = AccentPurple,
                title = "The Objective"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.settings.card)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Be the first player to empty your rack by placing all your tiles on the table in \"Groups\" and \"Runs\". Accumulate the highest score over several rounds to win.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.settings.secondaryText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Valid Sets section
            RulesSectionHeader(
                icon = Icons.Filled.Layers,
                iconBg = AccentPurple,
                title = "Valid Sets"
            )

            Spacer(modifier = Modifier.height(8.dp))

            RuleSetCard(
                name = "THE GROUP",
                badge = "3-4 Different Colors",
                description = "Same number, different colors. Must have at least 3 tiles.",
                tiles = listOf(
                    RuleTileData("7", TileColor.RED),
                    RuleTileData("7", TileColor.BLUE),
                    RuleTileData("7", TileColor.ORANGE)
                ),
                showAddTile = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleSetCard(
                name = "THE RUN",
                badge = "Same Color, Sequential",
                description = "Consecutive numbers of the same color. Must have at least 3 tiles.",
                tiles = listOf(
                    RuleTileData("4", TileColor.RED),
                    RuleTileData("5", TileColor.RED),
                    RuleTileData("6", TileColor.RED)
                ),
                showAddTile = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Key Rules section
            RulesSectionHeader(
                icon = Icons.Filled.MiscellaneousServices,
                iconBg = RulesKeyRulesGreen,
                title = "Key Rules"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RuleItemCard(
                    badgeContent = {
                        Text(
                            text = "30",
                            color = RulesKeyRulesGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    badgeBg = RulesKeyRulesGreen.copy(alpha = 0.15f),
                    title = "Initial Meld",
                    description = "Your first move must total at least 30 points using only tiles from your rack. You cannot use board tiles until this is met."
                )

                RuleItemCard(
                    badgeContent = {
                        Icon(
                            imageVector = Icons.Filled.Casino,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    badgeBg = AccentYellow.copy(alpha = 0.15f),
                    title = "The Joker",
                    description = "Wildcards that can represent any number/color. If you replace a Joker on the board with the actual tile, you must use it in a set immediately."
                )

                RuleItemCard(
                    badgeContent = {
                        Icon(
                            imageVector = Icons.Filled.PanTool,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    badgeBg = AccentBlue.copy(alpha = 0.15f),
                    title = "Manipulation",
                    description = "You can rearrange any tiles already on the board to form new valid sets, as long as every set remains valid at the end of your turn."
                )
            }
        }
    }
}

private data class RuleTileData(
    val label: String,
    val color: TileColor
)

@Composable
private fun RuleSetCard(
    name: String,
    badge: String,
    description: String,
    tiles: List<RuleTileData>,
    showAddTile: Boolean
) {
    val c = appColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.settings.card)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = AccentPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = badge,
                color = c.settings.secondaryText,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = c.settings.secondaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.forEach { tile ->
                RuleTilePreview(label = tile.label, color = Color(tile.color.colorInt))
            }
            if (showAddTile) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(c.settings.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = c.settings.secondaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleTilePreview(label: String, color: Color) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Text(
                text = label,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                style = LocalTextStyle.current.copy(
                    drawStyle = Stroke(width = 4f, join = StrokeJoin.Round)
                )
            )
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun RuleItemCard(
    badgeContent: @Composable () -> Unit,
    badgeBg: Color,
    title: String,
    description: String
) {
    val c = appColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.settings.card)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            badgeContent()
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.settings.primaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = c.settings.secondaryText
            )
        }
    }
}

@Composable
private fun RulesSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String
) {
    val c = appColors()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c.settings.primaryText
        )
    }
}
