package at.aau.serg.android.core.network

object WebConfig {
    // Emulator
    //private const val HOST = "10.0.2.2:8080"
    // Lokale TestAdresse
    //private const val HOST = "192.168.178.41:8080"
    private const val HOST = "10.0.0.20:8080"
    private const val HTTP = "http://"
    private const val WS = "ws://"

    const val API_URL = "$HTTP$HOST/api/"
    const val SOCKET_URL = "$WS$HOST/ws"

    object Socket {
        // Number of retries before connection to server is assumed as lost
        const val MAX_ATTEMPTS = 10
        /*
         * Delay per attempt = attempt * delay (3 * 1000 = 3 seconds) for thrird attempt
         */
        const val ATTEMPT_DELAY = 1000L
        // Hardcoded limit so if attempt limit is at 100 it would not take to long
        const val MAX_ATTEMPT_DELAY = 10000L
        const val IDLE_TIMEOUT = 10000L
    }

    object Topics {
        fun lobby(lobbyId: String) = "/topic/lobbies/$lobbyId"
        fun match(matchId: String) = "/topic/matches/$matchId"
    }
}
