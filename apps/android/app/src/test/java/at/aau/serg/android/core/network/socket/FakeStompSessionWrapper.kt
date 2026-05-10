package at.aau.serg.android.core.network.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

open class FakeStompSessionWrapper : StompSessionWrapper {

    private val topicFlows = mutableMapOf<String, MutableSharedFlow<String>>()
    private val failure = mutableMapOf<String, Throwable>()
    var isDisconnected = false
        private set

    override suspend fun subscribeText(destination: String): Flow<String> {
        failure[destination]?.let { error ->
            return flow { throw error }
        }

        return topicFlows.getOrPut(destination) {
            MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
        }
    }

    suspend fun emit(destination: String, message: String) {
        val flow = topicFlows.getOrPut(destination) {
            MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
        }
        flow.emit(message)
    }

    fun failSubscription(destination: String, error: Throwable) {
        failure[destination] = error
    }

    fun closeSession() {
        isDisconnected = true
    }

    override suspend fun disconnect() {
        isDisconnected = true
    }
}
