package at.aau.serg.android.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import at.aau.serg.android.ui.navigation.AppNavHost
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            AppNavHost(navController = navController)
        }
    }

    // TOP LEVEL
    @Test
    fun appNavHost_startsAtHome() {
        assertEquals("home", navController.currentDestination?.route)
    }

    @Test
    fun home_toCreateLobbyFancy_backToHome() {
        assertEquals("home", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("createLobbyFancy")
        }
        assertEquals("createLobbyFancy", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("home")
        }
        assertEquals("home", navController.currentDestination?.route)
    }

    @Test
    fun home_toBrowseLobbies_backToHome() {
        assertEquals("home", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("browsingLobbies")
        }
        assertEquals("browsingLobbies", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("home")
        }
        assertEquals("home", navController.currentDestination?.route)
    }

    @Test
    fun home_toWaitingRoom_backToHome() {
        assertEquals("home", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("waitingRoom/TEST123")
        }
        assertEquals("waitingRoom/{lobbyId}", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("home")
        }
        assertEquals("home", navController.currentDestination?.route)
    }

    @Test
    fun home_toSettings_backToHome() {
        assertEquals("home", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("settings")
        }
        assertEquals("settings", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("home")
        }
        assertEquals("home", navController.currentDestination?.route)
    }

    @Test
    fun home_toLeaderboard_backToHome() {
        assertEquals("home", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("leaderboard")
        }
        assertEquals("leaderboard", navController.currentDestination?.route)

        composeRule.runOnUiThread {
            navController.navigate("home")
        }
        assertEquals("home", navController.currentDestination?.route)
    }

}
