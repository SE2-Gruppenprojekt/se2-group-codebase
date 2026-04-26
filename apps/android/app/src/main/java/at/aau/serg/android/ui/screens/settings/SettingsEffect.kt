package at.aau.serg.android.ui.screens.settings

sealed class SettingsEffect {
    object NavigateChangeUsername : SettingsEffect()
    object NavigateBack : SettingsEffect()
    object Logout : SettingsEffect()
}
