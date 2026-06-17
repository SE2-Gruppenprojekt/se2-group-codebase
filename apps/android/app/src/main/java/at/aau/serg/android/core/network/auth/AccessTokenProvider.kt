package at.aau.serg.android.core.network.auth

fun interface AccessTokenProvider {
    fun currentAccessToken(): String?
}
