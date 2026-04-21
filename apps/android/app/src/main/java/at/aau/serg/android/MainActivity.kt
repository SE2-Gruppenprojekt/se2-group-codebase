package at.aau.serg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.getStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.navigation.AppNavHost
import at.aau.serg.android.ui.theme.TempappTheme
import at.aau.serg.android.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            TempappTheme(
                darkTheme = ThemeState.isDarkMode.value
            ) {

                val navController = rememberNavController()

                val provider = remember { DataStoreProvider.getInstance(this) }
                val userStore = remember { provider.getStore<User>() }

                androidx.compose.material3.Scaffold { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        context = this@MainActivity,
                        innerPadding = innerPadding,
                        userStore = userStore,
                    )
                }
            }
        }
    }
}
