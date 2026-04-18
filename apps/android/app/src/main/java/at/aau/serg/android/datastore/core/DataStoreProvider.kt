package at.aau.serg.android.datastore.core

import android.content.Context
import at.aau.serg.android.datastore.user.UserStore

class DataStoreProvider private constructor(context: Context) {

    val userStore = UserStore(context)

    companion object {
        @Volatile private var INSTANCE: DataStoreProvider? = null

        fun getInstance(context: Context): DataStoreProvider =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreProvider(context.applicationContext)
                    .also { INSTANCE = it }
            }
    }
}
