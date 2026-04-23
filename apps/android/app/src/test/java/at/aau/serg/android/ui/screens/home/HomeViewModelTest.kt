package at.aau.serg.android.ui.screens.home

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var vm: HomeViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
        vm = HomeViewModel(store)
    }

    @Test
    fun initialState_loadsFromStore() = runTest {
        val initialUser = User.newBuilder()
            .setUid("123")
            .setDisplayName("Alice")
            .build()

        store.save(initialUser)
        advanceUntilIdle()

        val state = vm.uiState.value

        assertEquals("Alice", state.username)
        assertEquals("123", state.uid)
        assertTrue(state.loadState is LoadState.Success)
    }

    @Test
    fun blankUsername_becomesGuest() = runTest {
        val initialUser = User.newBuilder()
            .setUid("999")
            .setDisplayName("")
            .build()

        store.save(initialUser)
        advanceUntilIdle()

        val state = vm.uiState.value

        assertEquals("Guest", state.username)
        assertEquals("999", state.uid)
        assertTrue(state.loadState is LoadState.Success)
    }

    @Test
    fun storeSave_updatesUiState() = runTest {
        val updatedUser = User.newBuilder()
            .setUid("777")
            .setDisplayName("Bob")
            .build()

        store.save(updatedUser)
        advanceUntilIdle()

        val state = vm.uiState.value

        assertEquals("Bob", state.username)
        assertEquals("777", state.uid)
        assertTrue(state.loadState is LoadState.Success)
    }
}
