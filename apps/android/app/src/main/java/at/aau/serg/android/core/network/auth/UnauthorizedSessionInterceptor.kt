package at.aau.serg.android.core.network.auth

import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedSessionInterceptor(
    private val unauthorizedSessionHandler: UnauthorizedSessionHandler
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            unauthorizedSessionHandler.onUnauthorized()
        }
        return response
    }
}
