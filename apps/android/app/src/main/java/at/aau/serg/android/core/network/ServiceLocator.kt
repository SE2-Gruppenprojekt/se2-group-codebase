package at.aau.serg.android.core.network

import at.aau.serg.android.core.network.game.GameWebSocketService
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService

object ServiceLocator {

    val lobbyWebSocketService: LobbyWebSocketService by lazy {
        LobbyWebSocketService(MoshiProvider.moshi)
    }

    val gameWebSocketService: GameWebSocketService by lazy {
        GameWebSocketService(MoshiProvider.moshi)
    }

}
