package at.aau.serg.android.ui.navigation

object Routes {
    // graphs
    const val AUTH = "auth_graph"
    const val HOME = "home_graph"

    // Start of AUTH_GRAPH
    // auth / profile
    const val USERNAME = "username"
    const val CHANGE_USERNAME = "changeUsername"
    // End of AUTH_GRAPH


    // Start of HOME_GRAPH
    // home
    const val HOME_SCREEN = "home"
    const val SETTINGS = "settings"
    const val LEADERBOARD = "leaderboard"
    const val RULES = "rules"

    // lobby
    const val LOBBY = "lobby"
    const val CREATE_LOBBY = "createLobby"
    const val CREATE_LOBBY_FANCY = "createLobbyFancy"
    const val BROWSING_LOBBIES = "browsingLobbies"
    const val WAITING_ROOM = "waitingRoom"

    // game
    const val GAME_FLOW = "game_flow"
    const val GAME = "game"
    const val GAME_RESULT = "game_result"
    // End of HOME_GRAPH
}
