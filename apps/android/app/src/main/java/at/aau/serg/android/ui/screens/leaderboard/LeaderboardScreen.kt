package at.aau.serg.android.ui.screens.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shared.models.LeaderboardEntry

@Composable
fun LeaderboardScreen(
    players: List<LeaderboardEntry>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)   // safe now because Scaffold padding is applied above
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(players.sortedBy { it.rank }) { entry ->
                LeaderboardRow(entry)
            }
        }
    }
}


@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#${entry.rank}  ${entry.playerName}", style = MaterialTheme.typography.bodyLarge)
            Text("${entry.score} pts", style = MaterialTheme.typography.bodyLarge)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Wins: ${entry.wins}", style = MaterialTheme.typography.bodyMedium)
            Text("Games: ${entry.gamesPlayed}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
