package at.aau.serg.android.ui.screens.leaderboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import shared.models.LeaderboardEntry

@Composable
fun LeaderboardMainRow(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "#${entry.rank}  ${entry.playerName}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${entry.score} pts",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
