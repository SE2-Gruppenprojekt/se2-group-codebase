package at.aau.serg.android.core.session

import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.network.auth.UnauthorizedSessionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SessionManager(
    private val userStore: UserStore
) : UnauthorizedSessionHandler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _unauthorizedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvents = _unauthorizedEvents.asSharedFlow()

    override fun onUnauthorized() {
        scope.launch {
            userStore.clearSession()
            _unauthorizedEvents.emit(Unit)
        }
    }
}
