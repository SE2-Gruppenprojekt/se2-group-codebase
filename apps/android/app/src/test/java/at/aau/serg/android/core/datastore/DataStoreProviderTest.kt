package at.aau.serg.android.core.datastore

import android.content.Context
import at.aau.serg.android.datastore.proto.User
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DataStoreProviderTest {
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext

        val field = DataStoreProvider::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun getInstance_returns_sameInstance() {
        val instance1 = DataStoreProvider.getInstance(mockContext)
        val instance2 = DataStoreProvider.getInstance(mockContext)

        assertSame(instance1, instance2)
    }

    @Test
    fun getStore_returns_UserStore() {
        val provider = DataStoreProvider.getInstance(mockContext)
        val store = provider.getStore<User>()

        assertNotNull(store)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getStore_throws_IllegalArgumentException() {
        val provider = DataStoreProvider.getInstance(mockContext)
        provider.getStore(String::class)
    }
}
