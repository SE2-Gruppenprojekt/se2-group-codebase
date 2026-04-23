package at.aau.serg.android.core.datastore

import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InMemoryProtoStoreTest {

    private lateinit var initialUser: User
    private lateinit var store: InMemoryProtoStore<User>

    @Before
    fun setup() {
        initialUser = User.newBuilder()
            .setUid("initial")
            .setDisplayName("Init")
            .build()

        store = InMemoryProtoStore(initialUser)
    }

    @Test
    fun data_emitsInitialValue() = runTest {
        val emitted = store.data.first()

        assertEquals("initial", emitted.uid)
        assertEquals("Init", emitted.displayName)
    }

    @Test
    fun save_updatesValue() = runTest {
        val updated = User.newBuilder()
            .setUid("updated")
            .setDisplayName("User")
            .build()

        store.save(updated)

        val emitted = store.data.first()

        assertEquals(updated, emitted)
    }

    @Test
    fun save_multipleUpdates_emitLatestValue() = runTest {
        val user1 = User.newBuilder().setUid("u1").setDisplayName("One").build()
        val user2 = User.newBuilder().setUid("u2").setDisplayName("Two").build()
        val user3 = User.newBuilder().setUid("u3").setDisplayName("Three").build()

        store.save(user1)
        store.save(user2)
        store.save(user3)

        val emitted = store.data.first()

        assertEquals(user3, emitted)
    }
}
