package at.aau.serg.android.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenericViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {

    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        val viewModel = creator()

        if (modelClass.isAssignableFrom(viewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewModel as VM
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
