package at.aau.serg.android.ui.screens.leaderboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shared.models.LeaderboardEntry

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        LeaderboardMainRow(entry)
        LeaderboardStatsRow(entry)
    }
}



