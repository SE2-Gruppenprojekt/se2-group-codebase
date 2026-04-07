package at.aau.serg.android.util

import kotlinx.coroutines.CoroutineDispatcher

class TestDispatcherProvider(
    private val dispatcher: CoroutineDispatcher
) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
}
