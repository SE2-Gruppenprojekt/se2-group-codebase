package at.aau.serg.android.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var store: InMemoryProtoStore<User>

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
    }

    @Test
    fun shows_loading_while_reading_userdata() {
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            AppNavHost(
                navController = rememberNavController(),
                context = ApplicationProvider.getApplicationContext(),
                userStore = store
            )
        }

        composeRule
            .onNodeWithTag(AppNavTestTags.LOADING)
            .assertExists()
    }

    @Test
    fun navigates_to_home_with_user() {
        store = InMemoryProtoStore(
            User.newBuilder()
                .setUid("123")
                .setDisplayName("Alice")
                .build()
        )

        lateinit var navController: NavHostController

        composeRule.setContent {
            navController = rememberNavController()

            AppNavHost(
                navController = navController,
                context = ApplicationProvider.getApplicationContext(),
                userStore = store
            )
        }

        composeRule.runOnIdle {
            assert(navController.currentDestination?.route == Routes.HOME_SCREEN)
        }
    }

    @Test
    fun navigates_to_auth_without_user() {
        store = InMemoryProtoStore(
            User.newBuilder()
                .setUid("")
                .setDisplayName("")
                .build()
        )

        lateinit var navController: NavHostController

        composeRule.setContent {
            navController = rememberNavController()

            AppNavHost(
                navController = navController,
                context = ApplicationProvider.getApplicationContext(),
                userStore = store
            )
        }

        composeRule.runOnIdle {
            assert(navController.currentDestination?.route == Routes.USERNAME)
        }
    }

    @Test
    fun transitions_from_loading_to_auth() {
        composeRule.mainClock.autoAdvance = false

        lateinit var navController: NavHostController

        composeRule.setContent {
            navController = rememberNavController()

            AppNavHost(
                navController = navController,
                context = ApplicationProvider.getApplicationContext(),
                userStore = store
            )
        }

        composeRule
            .onNodeWithTag(AppNavTestTags.LOADING)
            .assertExists()

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assert(navController.currentDestination?.route == Routes.USERNAME)
        }
    }
}
