package at.aau.serg.android.ui.screens.leaderboard.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import shared.models.LeaderboardEntry

@Composable
fun LeaderboardList(
    players: List<LeaderboardEntry>
) {
    LazyColumn {
        items(players.sortedBy { it.rank }) { entry ->
            LeaderboardRow(entry)
        }
    }
}
