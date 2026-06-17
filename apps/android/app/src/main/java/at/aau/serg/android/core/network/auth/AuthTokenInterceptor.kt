package at.aau.serg.android.core.network.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(
    private val tokenProvider: AccessTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.currentAccessToken()

        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val updated = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(updated)
    }
}
