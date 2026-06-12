package at.aau.serg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.network.ServiceLocator
import at.aau.serg.android.ui.navigation.AppNavHost
import at.aau.serg.android.ui.theme.TempappTheme
import at.aau.serg.android.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val provider = DataStoreProvider.getInstance(this)
        val userStore = provider.getStore(at.aau.serg.android.datastore.proto.User::class) as UserStore
        ServiceLocator.initialize(userStore)

        setContent {

            TempappTheme(
                darkTheme = ThemeState.isDarkMode.value
            ) {

                val navController = rememberNavController()

                val rememberedProvider = remember { provider }
                val rememberedUserStore = remember { userStore }

                androidx.compose.material3.Scaffold { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        context = this@MainActivity,
                        innerPadding = innerPadding,
                        userStore = rememberedUserStore,
                    )
                }
            }
        }
    }
}
