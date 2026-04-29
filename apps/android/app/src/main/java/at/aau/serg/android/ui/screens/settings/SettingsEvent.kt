package at.aau.serg.android.ui.screens.settings


sealed class SettingsEvent {
    object OnChangeUsername : SettingsEvent()
    object OnBack : SettingsEvent()
    object OnLogout : SettingsEvent()
    data class SetDarkMode(val value: Boolean) : SettingsEvent()
}
