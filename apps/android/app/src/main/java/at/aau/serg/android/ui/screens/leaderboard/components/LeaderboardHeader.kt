package at.aau.serg.android.ui.screens.leaderboard.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LeaderboardHeader(
    onBack: () -> Unit
) {
    Button(onClick = onBack) {
        Text("Back")
    }
}


