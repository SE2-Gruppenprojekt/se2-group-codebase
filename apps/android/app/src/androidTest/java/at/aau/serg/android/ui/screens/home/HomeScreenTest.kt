package at.aau.serg.android.ui.screens.home

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import at.aau.serg.android.navigation.AppNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            AppNavHost(navController = navController)
        }
    }

    @Test
    fun homeScreen_navigatesToLeaderboard() {
        composeRule.onNodeWithText("Leaderboard").performClick()
        assert(navController.currentDestination?.route == "leaderboard")
    }
}
