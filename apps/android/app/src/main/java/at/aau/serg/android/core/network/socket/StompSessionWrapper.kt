package at.aau.serg.android.core.network.socket

import kotlinx.coroutines.flow.Flow

interface StompSessionWrapper {
    suspend fun subscribeText(destination: String): Flow<String>
    suspend fun disconnect()
}
