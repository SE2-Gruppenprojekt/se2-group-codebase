package at.aau.serg.android.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BaseProtoStoreTest {
    private lateinit var baseProtoStore: BaseProtoStore<String>
    private val defaultInstance = "DEFAULT"
    private lateinit var fakeDataStore: FakeDataStore<String>

    @Before
    fun setUp() {
        fakeDataStore = FakeDataStore(defaultInstance)
        baseProtoStore = BaseProtoStore(fakeDataStore, defaultInstance)
    }

    @Test
    fun data_flow_emits_current_state() = runTest {
        val expectedValue = "Hello World"
        fakeDataStore.updateData { expectedValue }
        val result = baseProtoStore.data.first()

        assertEquals(expectedValue, result)
    }

    @Test
    fun save_updates_state() = runTest {
        val newValue = "UpdatedValue"
        baseProtoStore.save(newValue)

        assertEquals(newValue, baseProtoStore.data.first())
    }

    @Test
    fun wipe_restores_default_instance() = runTest {
        baseProtoStore.save("Dirty Data")
        baseProtoStore.wipe()

        assertEquals(defaultInstance, baseProtoStore.data.first())
    }

    private class FakeDataStore<T>(initialValue: T) : DataStore<T> {
        private val flow = MutableStateFlow(initialValue)
        override val data: Flow<T> = flow

        override suspend fun updateData(transform: suspend (t: T) -> T): T {
            val newValue = transform(flow.value)
            flow.value = newValue
            return newValue
        }
    }
}
