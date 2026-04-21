package at.aau.serg.android.core.datastore.user

import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserStoreTest {

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var userStore: UserStore

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
        userStore = UserStore(store)
    }

    @Test
    fun save_persistsUser() = runTest {
        val user = User.newBuilder()
            .setUid("123")
            .setDisplayName("Alice")
            .build()

        userStore.save(user)

        val stored = userStore.data.first()

        assertEquals("123", stored.uid)
        assertEquals("Alice", stored.displayName)
    }

    @Test
    fun updateDisplayName_updatesWhenValid() = runTest {
        val initial = User.newBuilder()
            .setDisplayName("Old")
            .build()

        userStore.save(initial)

        val result = userStore.updateDisplayName("  NewName  ")

        val updated = userStore.data.first()

        assertTrue(result)
        assertEquals("NewName", updated.displayName)
    }

    @Test
    fun updateDisplayName_returnsFalseWhenInvalid() = runTest {
        val initial = User.newBuilder()
            .setDisplayName("Old")
            .build()

        userStore.save(initial)

        val result = userStore.updateDisplayName("")

        val stored = userStore.data.first()

        assertFalse(result)
        assertEquals("Old", stored.displayName)
    }

    @Test
    fun wipe_resetsToDefault() = runTest {
        val user = User.newBuilder()
            .setUid("wipe")
            .setDisplayName("Me")
            .build()

        userStore.save(user)

        userStore.wipe()

        val stored = userStore.data.first()

        assertEquals(User.getDefaultInstance(), stored)
    }

    @Test
    fun data_emitsLatestValue() = runTest {
        val user1 = User.newBuilder()
            .setUid("1")
            .setDisplayName("A")
            .build()

        val user2 = User.newBuilder()
            .setUid("2")
            .setDisplayName("B")
            .build()

        userStore.save(user1)
        assertEquals("A", userStore.data.first().displayName)

        userStore.save(user2)
        assertEquals("B", userStore.data.first().displayName)
    }
}
