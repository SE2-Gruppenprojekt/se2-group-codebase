package at.aau.serg.android.ui.screens.settings

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.theme.ThemeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        val user = User.newBuilder()
            .setUid("")
            .setDisplayName("")
            .build()

        store = InMemoryProtoStore(user)
        viewModel = SettingsViewModel(store)
    }

    @Test
    fun user_state_emits_initial_value() = runTest {
        val user = viewModel.user.value

        assertEquals("", user.uid)
        assertEquals("", user.displayName)
    }

    @Test
    fun user_state_updates_when_store_changes() = runTest {
        val job = launch { viewModel.user.collect { } }

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
    fun onBack_emits_navigation_effect() = runTest {
        val job = launch {
            val effect = viewModel.effects.first()
            assertTrue(effect is SettingsEffect.NavigateBack)
        }

        viewModel.onEvent(SettingsEvent.OnBack)
        advanceUntilIdle()
        job.cancel()
    }

    @Test
    fun onChangeUsername_emits_navigation_effect() = runTest {
        val job = launch {
            val effect = viewModel.effects.first()
            assertTrue(effect is SettingsEffect.NavigateChangeUsername)
        }

        viewModel.onEvent(SettingsEvent.OnChangeUsername)
        advanceUntilIdle()
        job.cancel()
    }

    @Test
    fun logout_wipes_store_and_emits_effect() = runTest {
        val job = launch {
            val effect = viewModel.effects.first()
            assertTrue(effect is SettingsEffect.Logout)
        }

        viewModel.onEvent(SettingsEvent.OnLogout)
        advanceUntilIdle()

        val stored = store.data.first()
        assertEquals(User.getDefaultInstance(), stored)

        job.cancel()
    }

    @Test
    fun setDarkMode_updates_theme_state() = runTest {
        ThemeState.isDarkMode.value = false

        viewModel.onEvent(SettingsEvent.SetDarkMode(true))

        assertTrue(ThemeState.isDarkMode.value)
    }
}
