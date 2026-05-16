package at.aau.serg.android.core.datastore

import android.content.Context
import at.aau.serg.android.core.datastore.user.UserProtoDataStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.datastore.proto.User
import kotlin.reflect.KClass

class DataStoreProvider private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val stores: Map<KClass<*>, ProtoStore<*>> by lazy {
        mapOf(
            User::class to UserStore(
                BaseProtoStore(appContext.UserProtoDataStore, User.getDefaultInstance())
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getStore(type: KClass<T>): ProtoStore<T> {
        return stores[type] as? ProtoStore<T>
            ?: throw IllegalArgumentException("No store for ${type.simpleName}")
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
