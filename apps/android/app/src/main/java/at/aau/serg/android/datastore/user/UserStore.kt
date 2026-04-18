package at.aau.serg.android.datastore.user

import android.content.Context
import at.aau.serg.android.datastore.core.ProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.datastore.user.userDataStore
import kotlinx.coroutines.flow.Flow
import shared.validation.user.DisplayNameValidator

class UserStore(private val context: Context) : ProtoStore<User> {

    override val data: Flow<User>
        get() = context.userDataStore.data

    override suspend fun save(value: User) {
        context.userDataStore.updateData { value }
    }

    suspend fun updateDisplayName(rawName: String): Boolean {
        val result = DisplayNameValidator.validate(rawName)
        if (!result.isValid) return false

        context.userDataStore.updateData { current ->
            current.toBuilder()
                .setDisplayName(rawName.trim())
                .build()
        }

        return true
    }

    suspend fun wipe() {
        context.userDataStore.updateData {
            User.getDefaultInstance()
        }
    }
}
