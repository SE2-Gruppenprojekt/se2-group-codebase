package at.aau.serg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import at.aau.serg.android.navigation.AppNavHost
import at.aau.serg.android.ui.theme.TempappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TempappTheme {
                val navController = rememberNavController()

                androidx.compose.material3.Scaffold { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}



