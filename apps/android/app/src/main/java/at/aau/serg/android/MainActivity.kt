package at.aau.serg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import at.aau.serg.android.ui.screens.HomeScreen
import at.aau.serg.android.ui.screens.LeaderboardScreen
import at.aau.serg.android.ui.theme.TempappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TempappTheme {

                var currentScreen by remember { mutableStateOf(Screen.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (currentScreen) {

                        Screen.HOME -> HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            onShowLeaderboard = {
                                currentScreen = Screen.LEADERBOARD
                            }
                        )

                        Screen.LEADERBOARD -> LeaderboardScreen(
                            modifier = Modifier.padding(innerPadding),
                            onBack = {
                                currentScreen = Screen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
