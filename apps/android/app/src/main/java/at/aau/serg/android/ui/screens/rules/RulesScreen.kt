package at.aau.serg.android.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.theme.AccentBlue
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.AccentYellow
import at.aau.serg.android.ui.theme.RulesKeyRulesGreen
import at.aau.serg.android.ui.theme.RulesScoringPink
import at.aau.serg.android.ui.theme.SettingsIconRedTint
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
            .padding(16.dp)
            .testTag(RulesTestTags.SCREEN)
    ) {

        // header section
        TopBar(
            subtitle = "Game Rules",
            onBack = onBack,
            backButtonModifier = Modifier.testTag(RulesTestTags.BACK_BUTTON)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // rules content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
                iconBg = AccentBlue,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Scoring System section
            RulesSectionHeader(
                icon = Icons.Filled.EmojiEvents,
                iconBg = RulesScoringPink,
                title = "Scoring System"
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    Column {
                        Text(
                            text = "Winner's Score",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = c.settings.primaryText
                        )
                        Text(
                            text = "Sum of all opponents' remaining tiles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.settings.secondaryText
                        )
                    }
                    Text(
                        text = "+Sum",
                        color = AccentPurple,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.settings.card)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Once you empty your rack, the game continues until everyone else finishes too. Places are awarded in the order players finish - 1st, 2nd, 3rd, and so on.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.settings.secondaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SettingsIconRedTint.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Careful! A Joker left on your rack at the end of a game counts as ")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("30 penalty points")
                            }
                            append(".")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SettingsIconRedTint
                    )
                }
            }

            // Cheat banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                SettingsIconRedTint.copy(alpha = 0.18f),
                                AccentPurple.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PhoneAndroid,
                        contentDescription = null,
                        tint = SettingsIconRedTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHEAT",
                        color = SettingsIconRedTint,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 4.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shake to reveal opponents' tiles",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.settings.secondaryText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // ready to play action
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(52.dp)
                .testTag(RulesTestTags.READY_BUTTON),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(colors = listOf(AccentPurple, AccentBlue))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "I'M READY TO PLAY",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
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
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
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
