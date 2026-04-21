package at.aau.serg.android.ui.screens.setting

import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        val user = User.newBuilder()
            .setUid("u1")
            .setDisplayName("Alice")
            .build()

        store = InMemoryProtoStore(user)
        viewModel = SettingsViewModel(store)
    }

    @Test
    fun user_state_emits_initial_value() = runTest {
        val user = viewModel.user.value

        assertEquals("u1", user.uid)
        assertEquals("Alice", user.displayName)
    }

    @Test
    fun user_state_updates_when_store_changes() = runTest {
        // trigger collection
        val job = launch {
            viewModel.user.collect { }
        }

        val updated = User.newBuilder()
            .setUid("u2")
            .setDisplayName("Bob")
            .build()

        store.save(updated)
        advanceUntilIdle()

        val user = viewModel.user.value

        assertEquals("u2", user.uid)
        assertEquals("Bob", user.displayName)

        job.cancel()
    }

    @Test
    fun logout_wipes_store_and_calls_callback() = runTest {
        var doneCalled = false

        viewModel.logout {
            doneCalled = true
        }

        advanceUntilIdle()

        val stored = store.data.first()

        assertEquals(
            User.newBuilder()
                .setUid("u1")
                .setDisplayName("Alice")
                .build(),
            stored
        )

        assertTrue(doneCalled)
    }
}
