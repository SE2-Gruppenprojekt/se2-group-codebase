package at.aau.serg.android.ui.screens.home

sealed class HomeEffect {
    object NavigateToCreate : HomeEffect()
    object NavigateToBrowse : HomeEffect()
    object NavigateToSettings : HomeEffect()
}
