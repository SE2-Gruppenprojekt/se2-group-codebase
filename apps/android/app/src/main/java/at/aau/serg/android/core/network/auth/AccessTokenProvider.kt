package at.aau.serg.android.core.network.auth

interface AccessTokenProvider {
    fun currentAccessToken(): String?
}
