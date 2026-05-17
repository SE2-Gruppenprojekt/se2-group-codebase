package at.aau.serg.android.core.datastore.user

import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.first
import shared.validation.user.DisplayNameValidator

class UserStore(
    private val store: ProtoStore<User>
) : ProtoStore<User> by store {

    suspend fun updateDisplayName(rawName: String): Boolean {
        val result = DisplayNameValidator.validate(rawName)
        if (!result.isValid) return false

        val current = data.first()
        save(current.toBuilder().setDisplayName(rawName.trim()).build())
        return true
    }
}
