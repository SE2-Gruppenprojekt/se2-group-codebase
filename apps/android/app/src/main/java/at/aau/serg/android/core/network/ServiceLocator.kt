package at.aau.serg.android.core.network

import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.network.game.GameWebSocketService
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import at.aau.serg.android.core.session.SessionManager

object ServiceLocator {

    @Volatile
    private var _sessionManager: SessionManager? = null

    fun initialize(userStore: UserStore) {
        if (_sessionManager == null) {
            _sessionManager = SessionManager(userStore)
        }
        RetrofitProvider.initialize(
            accessTokenProvider = userStore,
            unauthorizedSessionHandler = sessionManager
        )
    }

    val sessionManager: SessionManager
        get() = requireNotNull(_sessionManager) {
            "ServiceLocator.initialize(userStore) must be called before accessing sessionManager"
        }

    val lobbyWebSocketService: LobbyWebSocketService by lazy {
        LobbyWebSocketService(MoshiProvider.moshi)
    }

    val gameWebSocketService: GameWebSocketService by lazy {
        GameWebSocketService(MoshiProvider.moshi)
    }

}
