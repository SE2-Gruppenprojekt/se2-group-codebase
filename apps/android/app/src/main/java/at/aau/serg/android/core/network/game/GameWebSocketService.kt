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

    private val draftUpdatedAdapter = moshi.adapter(GameDraftUpdatedEvent::class.java)
    private val gameEndedAdapter = moshi.adapter(GameEndedEvent::class.java)
    private val gameUpdatedAdapter = moshi.adapter(GameUpdatedEvent::class.java)
    private val turnChangedAdapter = moshi.adapter(TurnChangedEvent::class.java)
    private val turnTimedOutAdapter = moshi.adapter(TurnTimedOutEvent::class.java)

    suspend fun subscribe(gameId: String): Flow<GameEvent> {
        return ws
            .subscribe(WebConfig.Topics.match(gameId))
            .mapNotNull { parseGameEvent(it) }
    }

    private fun parseGameEvent(message: String): GameEvent? {
        return try {
            val type = messageTypeAdapter.fromJson(message)?.get("type") as? String

            when (type) {
                GameDraftUpdatedEvent.TYPE ->
                    draftUpdatedAdapter.fromJson(message)?.let { GameEvent.DraftUpdated(it) }

                GameEndedEvent.TYPE ->
                    gameEndedAdapter.fromJson(message)?.let { GameEvent.Ended(it) }

                GameUpdatedEvent.TYPE ->
                    gameUpdatedAdapter.fromJson(message)?.let { GameEvent.Updated(it) }

                TurnChangedEvent.TYPE ->
                    turnChangedAdapter.fromJson(message)?.let { GameEvent.TurnChanged(it) }

                TurnTimedOutEvent.TYPE ->
                    turnTimedOutAdapter.fromJson(message)?.let { GameEvent.TurnTimedOut(it) }

                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    internal fun dispatch(message: String): GameEvent? {
        return parseGameEvent(message)
    }
}
