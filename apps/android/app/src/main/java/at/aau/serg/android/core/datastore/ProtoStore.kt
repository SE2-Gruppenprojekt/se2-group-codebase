package at.aau.serg.android.core.datastore

import kotlinx.coroutines.flow.Flow

interface ProtoStore<T> {
    val data: Flow<T>
    suspend fun save(value: T)
}
