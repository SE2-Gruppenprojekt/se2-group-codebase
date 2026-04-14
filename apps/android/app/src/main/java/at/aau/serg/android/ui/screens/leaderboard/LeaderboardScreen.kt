package at.aau.serg.android.ui.screens.leaderboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.leaderboard.components.LeaderboardHeader
import at.aau.serg.android.ui.screens.leaderboard.components.LeaderboardList
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
            .padding(16.dp)
    ) {
        LeaderboardHeader(onBack)

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        LeaderboardList(players)
    }
}
