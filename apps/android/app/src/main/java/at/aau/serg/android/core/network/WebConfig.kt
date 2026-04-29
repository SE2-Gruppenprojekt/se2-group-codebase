package at.aau.serg.android.core.network

object WebConfig {
    private const val HOST = "10.0.2.2:8080"
    private const val HTTP = "http://"
    private const val WS = "ws://"

    const val API_URL = "$HTTP$HOST/api/"
    const val SOCKET_URL = "$WS$HOST/ws"

    object Topics {
        fun lobby(lobbyId: String) = "/topic/lobbies/$lobbyId"
        fun match(matchId: String) = "/topic/matches/$matchId"
    }
}
