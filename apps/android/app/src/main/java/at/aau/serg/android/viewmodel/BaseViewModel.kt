package at.aau.serg.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.errors.ErrorMapper
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.util.DispatcherProvider
import at.aau.serg.android.util.DefaultDispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel(
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : ViewModel() {

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState

    protected fun <T> launchRequest(
        request: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch(dispatchers.main) {
            _loadState.value = LoadState.Loading

            try {
                val result = withContext(dispatchers.io) { request() }
                _loadState.value = LoadState.Success
                onSuccess(result)

            } catch (e: Exception) {
                _loadState.value = LoadState.Error(ErrorMapper.map(e))
                onError()
            }
        }
    }
}
