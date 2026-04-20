package at.aau.serg.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.getStore
import at.aau.serg.android.datastore.proto.User

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues = PaddingValues()
) {
    val dataStoreProvider = remember {
        DataStoreProvider.getInstance(navController.context)
    }

    val userStore = remember {
        dataStoreProvider.getStore<User>()
    }

    val user by userStore.data.collectAsState(
        initial = null
    )

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination =
        if (user!!.uid.isNotBlank()) Routes.HOME else Routes.AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = "root",
        modifier = Modifier.padding(innerPadding)
    ) {
        authGraph(navController, dataStoreProvider)
        homeGraph(navController, dataStoreProvider)
    }
}
