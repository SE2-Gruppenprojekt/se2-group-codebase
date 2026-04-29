package at.aau.serg.android.core.network

import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object ServiceLocator {

    val lobbyWebSocketService: LobbyWebSocketService by lazy {
        LobbyWebSocketService(MoshiProvider.moshi)
    }
}
