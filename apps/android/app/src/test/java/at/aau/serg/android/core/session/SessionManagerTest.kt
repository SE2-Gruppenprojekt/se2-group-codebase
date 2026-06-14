package at.aau.serg.android.core.session

import app.cash.turbine.test
import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onUnauthorized_clearsSession_andEmitsEvent() = runTest {
        val store = UserStore(InMemoryProtoStore(User.getDefaultInstance()))
        store.save(
            User.newBuilder()
                .setUid("user-1")
                .setDisplayName("Alice")
                .setAccessToken("token")
                .setGameId("game-1")
                .build()
        )
        val manager = SessionManager(store)

        manager.unauthorizedEvents.test {
            manager.onUnauthorized()
            advanceUntilIdle()

            awaitItem()
            val stored = store.data.first()
            assertEquals("", stored.uid)
            assertEquals("Alice", stored.displayName)
            assertEquals("", stored.accessToken)
            assertEquals("", stored.gameId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
