package at.aau.serg.android.ui.screens.lobby.browse.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseItem
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseTestTags
import at.aau.serg.android.ui.theme.appColors

@Composable
fun LobbyBrowseCard(
    lobby: LobbyBrowseItem,
    cardColor: Color,
    primaryText: Color,
    onJoinLobby: (String) -> Unit
) {
    val accentColor = lobby.accentColor
    val cardAlpha = if (lobby.isOpen) 0.14f else 0.05f
    val borderAlpha = if (lobby.isOpen) 0.95f else 0.30f
    val subtleCardColor = accentColor.copy(alpha = cardAlpha).compositeOver(cardColor)
    val disabledButtonColor = appColors().screen.disabledButton
    val buttonColor = if (lobby.isOpen) accentColor else disabledButtonColor
    val buttonText = if (lobby.isOpen) "Join" else "Full"
    Card(
        modifier = Modifier
            .testTag("${LobbyBrowseTestTags.LobbyItem.CARD_PREFIX}_${lobby.lobbyId}")
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = accentColor.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = subtleCardColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "#${lobby.lobbyId}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BadgeChip(
                        text = if (lobby.isOpen) "OPEN" else "FULL",
                        backgroundColor = accentColor,
                        textColor = Color.White
                    )
                }

                Icon(
                    imageVector = Icons.Filled.Casino,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${lobby.currentPlayers}/${lobby.maxPlayers}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier.testTag(
                        "${LobbyBrowseTestTags.LobbyItem.JOIN_BUTTON_PREFIX}_${lobby.lobbyId}"
                    ),
                    onClick = { onJoinLobby(lobby.lobbyId) },
                    enabled = lobby.isOpen,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White,
                        disabledContainerColor = disabledButtonColor,
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
