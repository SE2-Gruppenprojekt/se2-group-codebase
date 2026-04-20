package at.aau.serg.android.core.datastore

import android.content.Context
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.datastore.proto.User
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DataStoreProviderTest {

    private lateinit var context: Context
    private lateinit var provider: DataStoreProvider

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        provider = DataStoreProvider.getInstance(context)
    }

    @Test
    fun getStore_returnsUserStore_forUserClass() {
        val store = provider.getStore(User::class)

        assertEquals(UserStore::class, store::class)
    }

    @Test
    fun getStore_reified_returnsSameInstance() {
        val store1 = provider.getStore(User::class)
        val store2 = provider.getStore<User>()

        assertSame(store1, store2)
    }

    @Test
    fun getInstance_returnsSameSingletonInstance() {
        val instance1 = DataStoreProvider.getInstance(context)
        val instance2 = DataStoreProvider.getInstance(context)

        assertSame(instance1, instance2)
    }
}
