package at.aau.serg.android.core.datastore

import kotlinx.coroutines.flow.Flow

class BaseProtoStore<T>(
    private val dataStore: androidx.datastore.core.DataStore<T>,
    private val defaultInstance: T
) : ProtoStore<T> {
    override val data: Flow<T> get() = dataStore.data

    override suspend fun save(value: T) { dataStore.updateData { value } }
    override suspend fun wipe() { dataStore.updateData { defaultInstance } }
}
