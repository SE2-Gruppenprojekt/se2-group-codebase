package at.aau.serg.android.core.datastore.user

import android.content.Context
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.Flow
import shared.validation.user.DisplayNameValidator

class UserStore(private val context: Context) : ProtoStore<User> {

    override val data: Flow<User>
        get() = context.UserProtoDataStore.data

    override suspend fun save(value: User) {
        context.UserProtoDataStore.updateData { value }
    }

    suspend fun updateDisplayName(rawName: String): Boolean {
        val result = DisplayNameValidator.validate(rawName)
        if (!result.isValid) return false

        context.UserProtoDataStore.updateData { current ->
            current.toBuilder()
                .setDisplayName(rawName.trim())
                .build()
        }

        return true
    }

    suspend fun wipe() {
        context.UserProtoDataStore.updateData {
            User.getDefaultInstance()
        }
    }
}
