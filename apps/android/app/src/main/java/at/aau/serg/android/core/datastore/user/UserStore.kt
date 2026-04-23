package at.aau.serg.android.core.datastore.user

import android.content.Context
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import shared.validation.user.DisplayNameValidator

class UserStore(
    private val store: ProtoStore<User>
) : ProtoStore<User> {

    override val data = store.data

    override suspend fun save(value: User) {
        store.save(value)
    }

    suspend fun updateDisplayName(rawName: String): Boolean {
        val result = DisplayNameValidator.validate(rawName)
        if (!result.isValid) return false

        save(
            data.first().toBuilder()
                .setDisplayName(rawName.trim())
                .build()
        )
        return true
    }

    override suspend fun wipe() {
        store.save(User.getDefaultInstance())
    }
}
