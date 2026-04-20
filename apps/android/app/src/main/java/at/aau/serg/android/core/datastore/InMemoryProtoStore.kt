package at.aau.serg.android.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryProtoStore<T>(
    initial: T
) : ProtoStore<T> {

    private val state = MutableStateFlow(initial)

    override val data: Flow<T> = state

    override suspend fun save(value: T) {
        state.value = value
    }
}
