package at.aau.serg.android.datastore.core

import kotlinx.coroutines.flow.Flow

interface ProtoStore<T> {
    val data: Flow<T>
    suspend fun save(value: T)
}
