package at.aau.serg.android.core.datastore

import android.content.Context
import at.aau.serg.android.core.datastore.user.UserProtoDataStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.datastore.proto.User
import kotlin.reflect.KClass

class DataStoreProvider private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val stores: Map<KClass<*>, ProtoStore<*>> = mapOf(
        User::class to createUserStore()
    )

    private fun createUserStore(): UserStore {
        val protoStore = object : ProtoStore<User> {
            override val data = appContext.UserProtoDataStore.data

            override suspend fun save(value: User) {
                appContext.UserProtoDataStore.updateData { value }
            }

            override suspend fun wipe() {
                appContext.UserProtoDataStore.updateData {
                    User.getDefaultInstance()
                }
            }
        }

        return UserStore(protoStore)
    }

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
