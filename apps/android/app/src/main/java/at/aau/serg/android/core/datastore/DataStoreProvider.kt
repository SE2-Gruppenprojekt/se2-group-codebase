package at.aau.serg.android.core.datastore

import android.content.Context
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.core.datastore.user.UserStore
import kotlin.reflect.KClass

class DataStoreProvider private constructor(context: Context) {

    private val stores: Map<KClass<*>, ProtoStore<*>> = mapOf(
        User::class to UserStore(context)
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getStore(type: KClass<T>): ProtoStore<T> {
        return stores[type] as ProtoStore<T>
    }

    companion object {
        @Volatile private var INSTANCE: DataStoreProvider? = null

        fun getInstance(context: Context): DataStoreProvider =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreProvider(context.applicationContext)
                    .also { INSTANCE = it }
            }
    }
}

inline fun <reified T : Any> DataStoreProvider.getStore(): ProtoStore<T> =
    getStore(T::class)
