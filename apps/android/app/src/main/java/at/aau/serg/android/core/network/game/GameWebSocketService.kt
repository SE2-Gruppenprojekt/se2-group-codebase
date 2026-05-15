package at.aau.serg.android.core.network.game

import at.aau.serg.android.core.network.WebConfig
import at.aau.serg.android.core.network.WebSocketManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import shared.models.game.event.GameDraftUpdatedEvent
import shared.models.game.event.GameEndedEvent
import shared.models.game.event.GameEvent
import shared.models.game.event.GameUpdatedEvent
import shared.models.game.event.TurnChangedEvent
import shared.models.game.event.TurnTimedOutEvent

class GameWebSocketService(
    private val moshi: Moshi,
    private val ws: WebSocketManager = WebSocketManager()
) {
    private val messageTypeAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Any::class.java
        )
    )

    suspend fun subscribe(gameId: String): Flow<GameEvent> {
        return ws
            .subscribe(WebConfig.Topics.match(gameId))
            .mapNotNull { parseGameEvent(it) }
    }

    private fun parseGameEvent(message: String): GameEvent? {
        val type = messageTypeAdapter.fromJson(message)?.get("type") as? String

        return when (type) {
            GameDraftUpdatedEvent.TYPE ->
                moshi.adapter(GameDraftUpdatedEvent::class.java)
                    .fromJson(message)
                    ?.let { GameEvent.DraftUpdated(it) }

            GameEndedEvent.TYPE ->
                moshi.adapter(GameEndedEvent::class.java)
                    .fromJson(message)
                    ?.let { GameEvent.Ended(it) }

            GameUpdatedEvent.TYPE ->
                moshi.adapter(GameUpdatedEvent::class.java)
                    .fromJson(message)
                    ?.let { GameEvent.Updated(it) }

            TurnChangedEvent.TYPE ->
                moshi.adapter(TurnChangedEvent::class.java)
                    .fromJson(message)
                    ?.let { GameEvent.TurnChanged(it) }

            TurnTimedOutEvent.TYPE ->
                moshi.adapter(TurnTimedOutEvent::class.java)
                    .fromJson(message)
                    ?.let { GameEvent.TurnTimedOut(it) }

            else -> null
        }
    }

    internal fun dispatch(message: String): GameEvent? {
        return parseGameEvent(message)
    }
}
