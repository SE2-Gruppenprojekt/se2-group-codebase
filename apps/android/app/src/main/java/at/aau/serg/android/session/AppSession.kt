package at.aau.serg.android.session

import java.util.UUID

object AppSession {
    val userId: String by lazy {
        UUID.randomUUID().toString()
    }

    val displayName: String by lazy {
        "Player${userId.takeLast(4).uppercase()}"
    }
}
