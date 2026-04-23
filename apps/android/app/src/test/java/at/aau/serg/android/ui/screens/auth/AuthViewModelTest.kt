package at.aau.serg.android.ui.screens.auth

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
    fun usernameChange_updatesValidation() = runTest {
        vm.onUsernameChanged("Bob")

        val state = vm.uiState.value

        assertEquals("Bob", state.username)
        assertTrue(state.validation.isValid)
        assertTrue(state.validation.violations.isEmpty())
    }

    @Test
    fun submit_savesUserAndTriggersSuccess() = runTest {
        var successCalled = false

        vm.onUsernameChanged("Charlie")
        vm.submit { successCalled = true }

        advanceUntilIdle()

        val saved = store.data.first()

        assertEquals("Charlie", saved.displayName)
        assertTrue(saved.uid.isNotBlank())
        assertTrue(successCalled)
    }

    @Test
    fun submit_doesNothingWhenValidationFails() = runTest {
        var successCalled = false

        vm.onUsernameChanged("") // invalid
        vm.submit { successCalled = true }

        val saved = store.data.first()

        assertEquals(User.getDefaultInstance(), saved)
        assertFalse(successCalled)
    }
}
