package at.aau.serg.android.core.datastore

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DataStoreProviderTest {

    private lateinit var context: Context
    private lateinit var provider: DataStoreProvider

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        provider = DataStoreProvider.getInstance(context)
    }

    @Test
    fun getStore_reified_returnsSameInstance() = runTest {
        val store1 = provider.getStore(User::class)
        val store2 = provider.getStore<User>()

        assertSame(store1, store2)
    }

    @Test
    fun getInstance_returnsSameSingletonInstance() = runTest {
        val instance1 = DataStoreProvider.getInstance(context)
        val instance2 = DataStoreProvider.getInstance(context)

        assertSame(instance1, instance2)
    }

    @Test
    fun userStore_wipe_resetsToDefault() = runTest {
        val store = provider.getStore<User>()

        store.save(
            User.newBuilder()
                .setUid("abc")
                .setDisplayName("test")
                .build()
        )

        store.wipe()

        val result = store.data.first()

        assertEquals(User.getDefaultInstance(), result)
    }
}
