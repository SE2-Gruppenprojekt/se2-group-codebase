package at.aau.serg.android.ui.screens.lobby.waiting

object LobbyWaitingTestTags {
    const val SCREEN = "lobbyWaiting_screen"
    const val ROOM_CODE = "waiting_room_code"
    const val PLAYER_LIST = "waiting_player_list"
    const val SETTINGS_SECTION = "waiting_settings_section"

    const val TURN_TIMER_VALUE = "waiting_turn_timer_value"
    const val TURN_TIMER_PLUS = "waiting_turn_timer_plus"
    const val TURN_TIMER_MINUS = "waiting_turn_timer_minus"

    const val STARTING_CARDS_VALUE = "waiting_starting_cards_value"
    const val STARTING_CARDS_PLUS = "waiting_starting_cards_plus"
    const val STARTING_CARDS_MINUS = "waiting_starting_cards_minus"

    const val STACK_SWITCH = "waiting_stack_switch"

    const val START_BUTTON = "waiting_start_game_button"

    object Players {
        const val READY_PREFIX = "player_"

        fun ready_tag(uid: String) = "$READY_PREFIX$uid"
    }
}
