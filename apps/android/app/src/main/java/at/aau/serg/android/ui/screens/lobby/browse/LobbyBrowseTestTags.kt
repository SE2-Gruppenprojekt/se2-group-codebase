package at.aau.serg.android.ui.screens.lobby.browse

object LobbyBrowseTestTags {
    const val SCREEN = "lobbyBrowse_screen"

    // Top Bar
    const val BACK_BUTTON = "lobbyBrowse_backButton"
    const val SETTINGS_BUTTON = "lobbyBrowse_settingsButton"
    const val TITLE = "lobbyBrowse_title"
    const val SUBTITLE = "lobbyBrowse_subtitle"

    // Direct Join Section
    const val LOBBY_ID_INPUT = "lobbyBrowse_lobbyIdInput"
    const val JOIN_BUTTON = "lobbyBrowse_joinButton"

    // Lobby List
    const val LOBBY_LIST = "lobbyBrowse_lobbyList"

    // per-item tags (use with lobbyId suffix)
    object LobbyItem {
        const val CARD_PREFIX = "lobbyBrowse_card"
        const val JOIN_BUTTON_PREFIX = "lobbyBrowse_join"
        const val ID_TEXT_PREFIX = "lobbyBrowse_id"
    }

    const val CREATE_BUTTON = "lobbyBrowse_createButton"
}
