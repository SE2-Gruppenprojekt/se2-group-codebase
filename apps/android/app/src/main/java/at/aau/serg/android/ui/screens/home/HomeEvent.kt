package at.aau.serg.android.ui.screens.home

sealed class HomeEvent {
    object OnCreateLobby : HomeEvent()
    object OnBrowseLobby : HomeEvent()
    object OnSettings : HomeEvent()
}
