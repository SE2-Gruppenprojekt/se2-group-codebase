package at.aau.serg.android.ui.screens.auth

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var vm: AuthViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
        vm = AuthViewModel(store)
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
        assertTrue(state.validation.isValid)
        assertTrue(state.validation.violations.isEmpty())
        assertEquals("123", state.uid)
    }

    @Test
    fun initialState_handlesEmptyStoreGracefully() = runTest {
        store.save(User.getDefaultInstance())
        advanceUntilIdle()

        val state = vm.uiState.value

        assertEquals("", state.username)
        assertFalse(state.validation.isValid)
    }

    @Test
    fun usernameChange_updatesValidation() = runTest {
        vm.onEvent(AuthEvent.OnUsernameChanged("Bob"))

        val state = vm.uiState.value

        assertEquals("Bob", state.username)
        assertTrue(state.validation.isValid)
        assertTrue(state.validation.violations.isEmpty())
    }

    @Test
    fun submit_overwritesExistingUser() = runTest {
        store.save(
            User.newBuilder()
                .setUid("old")
                .setDisplayName("OldName")
                .build()
        )

        advanceUntilIdle()

        vm.onEvent(AuthEvent.OnUsernameChanged("NewName"))
        vm.onEvent(AuthEvent.OnSubmit)
        advanceUntilIdle()

        val saved = store.data.first()

        assertEquals("NewName", saved.displayName)
    }

    @Test
    fun submit_emitsNavigateContinueEffect() = runTest {
        vm.onEvent(AuthEvent.OnUsernameChanged("Charlie"))

        var emitted = false

        val job = launch {
            vm.effects.collect {
                if (it is AuthEffect.NavigateContinue) {
                    emitted = true
                }
            }
        }

        vm.onEvent(AuthEvent.OnSubmit)
        advanceUntilIdle()

        assertTrue(emitted)

        job.cancel()
    }

    @Test
    fun submit_doesNothingWhenValidationFails() = runTest {
        vm.onEvent(AuthEvent.OnUsernameChanged(""))
        vm.onEvent(AuthEvent.OnSubmit)

        val saved = store.data.first()

        assertEquals(User.getDefaultInstance(), saved)
    }

    @Test
    fun back_emitsNavigateBackEffect() = runTest {
        var emitted = false

        val job = launch {
            vm.effects.collect {
                if (it is AuthEffect.NavigateBack) {
                    emitted = true
                }
            }
        }

        vm.onEvent(AuthEvent.OnBack)
        advanceUntilIdle()

        assertTrue(emitted)

        job.cancel()
    }

    @Test
    fun submit_generatesUid_whenBlank() = runTest {
        vm.onEvent(AuthEvent.OnUsernameChanged("Charlie"))
        vm.onEvent(AuthEvent.OnSubmit)
        advanceUntilIdle()

        val saved = store.data.first()

        assertTrue(saved.uid.isNotBlank())
    }

    @Test
    fun setMode_updatesUiState() = runTest {
        vm.setMode(AuthMode.CreateUser)

        val state = vm.uiState.value

        assertEquals(AuthMode.CreateUser, state.mode)
    }

    @Test
    fun setMode_updatesUiState_toChangeUsername() = runTest {
        vm.setMode(AuthMode.ChangeUsername)

        val state = vm.uiState.value

        assertEquals(AuthMode.ChangeUsername, state.mode)
    }

    @Test
    fun canGoBack_isTrue_whenModeIsChangeUsername() = runTest {
        vm.setMode(AuthMode.ChangeUsername)

        assertTrue(vm.uiState.value.canGoBack)
    }
}
