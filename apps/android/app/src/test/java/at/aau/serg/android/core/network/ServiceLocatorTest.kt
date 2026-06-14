package at.aau.serg.android.core.network

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.core.session.SessionManager
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ServiceLocatorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun resetServiceLocator() {
        val field = ServiceLocator::class.java.getDeclaredField("_sessionManager")
        field.isAccessible = true
        field.set(ServiceLocator, null)
    }

    @Test
    fun initialize_setsSessionManager_andRetrofit() {
        val store = UserStore(InMemoryProtoStore(User.getDefaultInstance()))

        ServiceLocator.initialize(store)

        assertNotNull(ServiceLocator.sessionManager)
        assertNotNull(RetrofitProvider.retrofit)
    }

    @Test
    fun initialize_keepsExistingSessionManager_whenCalledTwice() {
        val first = UserStore(InMemoryProtoStore(User.getDefaultInstance()))
        val second = UserStore(InMemoryProtoStore(User.getDefaultInstance()))

        ServiceLocator.initialize(first)
        val initialManager: SessionManager = ServiceLocator.sessionManager
        ServiceLocator.initialize(second)

        assertSame(initialManager, ServiceLocator.sessionManager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sessionManager_throwsWhenNotInitialized() {
        ServiceLocator.sessionManager
    }
}
