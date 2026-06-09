package at.aau.serg.android.ui.screens.game

sealed class GameEffect {
    object NavigateToSettings : GameEffect()
    object NavigateBack : GameEffect()
    object NavigateToResult : GameEffect()
}
