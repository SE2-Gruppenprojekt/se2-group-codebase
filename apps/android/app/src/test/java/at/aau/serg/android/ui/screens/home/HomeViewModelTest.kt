package at.aau.serg.android.ui.screens.home

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

        assertEquals("Alice", state.user?.displayName)
        assertEquals("123", state.user?.uid)
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

        assertEquals("Bob", state.user?.displayName)
        assertEquals("777", state.user?.uid)
        assertTrue(state.loadState is LoadState.Success)
    }

    @Test
    fun onCreateLobby_emitsNavigateToCreate() = runTest {
        vm.onEvent(HomeEvent.OnCreateLobby)

        val effect = vm.effects.first()

        assertEquals(HomeEffect.NavigateToCreate, effect)
    }

    @Test
    fun onBrowseLobby_emitsNavigateToBrowse() = runTest {
        vm.onEvent(HomeEvent.OnBrowseLobby)

        val effect = vm.effects.first()

        assertEquals(HomeEffect.NavigateToBrowse, effect)
    }

    @Test
    fun onSettings_emitsNavigateToSettings() = runTest {
        vm.onEvent(HomeEvent.OnSettings)

        val effect = vm.effects.first()

        assertEquals(HomeEffect.NavigateToSettings, effect)
    }
}
