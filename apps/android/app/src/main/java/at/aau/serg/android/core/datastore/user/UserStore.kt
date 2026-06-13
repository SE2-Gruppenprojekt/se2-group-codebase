package at.aau.serg.android.core.datastore.user

import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.auth.AccessTokenProvider
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import shared.validation.user.DisplayNameValidator

class UserStore(
    private val store: ProtoStore<User>
) : ProtoStore<User> by store, AccessTokenProvider {

    @Volatile
    private var cachedUser: User = User.getDefaultInstance()

    override val data = store.data.onEach { user -> cachedUser = user }

    override suspend fun save(value: User) {
        cachedUser = value
        store.save(value)
    }

    override suspend fun wipe() {
        cachedUser = User.getDefaultInstance()
        store.wipe()
    }

    override fun currentAccessToken(): String? =
        cachedUser.accessToken.takeIf { it.isNotBlank() }

    suspend fun saveSession(userId: String, displayName: String, accessToken: String) {
        val current = data.first()
        save(
            current.toBuilder()
                .setUid(userId)
                .setDisplayName(displayName.trim())
                .setAccessToken(accessToken)
                .build()
        )
    }

    suspend fun updateGameId(gameId: String) {
        val current = data.first()
        save(
            current.toBuilder()
                .setGameId(gameId)
                .build()
        )
    }

    suspend fun clearSession() {
        wipe()
    }

    suspend fun updateDisplayName(rawName: String): Boolean {
        val result = DisplayNameValidator.validate(rawName)
        if (!result.isValid) return false

        val current = data.first()
        save(current.toBuilder().setDisplayName(rawName.trim()).build())
        return true
    }
}
