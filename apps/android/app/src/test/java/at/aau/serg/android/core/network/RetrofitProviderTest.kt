package at.aau.serg.android.core.network

import at.aau.serg.android.core.network.auth.AccessTokenProvider
import at.aau.serg.android.core.network.auth.AuthTokenInterceptor
import at.aau.serg.android.core.network.auth.UnauthorizedSessionHandler
import at.aau.serg.android.core.network.auth.UnauthorizedSessionInterceptor
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class RetrofitProviderTest {

    @Test
    fun retrofit_returnsDefaultInstance_beforeInitialize() {
        assertNotNull(RetrofitProvider.retrofit)
    }

    @Test
    fun defaultProviders_areNoOp() {
        val tokenProviderClass = Class.forName(
            "at.aau.serg.android.core.network.RetrofitProvider\$NoOpAccessTokenProvider"
        )
        val tokenProviderInstance = tokenProviderClass.getDeclaredField("INSTANCE").apply {
            isAccessible = true
        }.get(null)
        val currentAccessToken = tokenProviderClass.getDeclaredMethod("currentAccessToken").apply {
            isAccessible = true
        }.invoke(tokenProviderInstance)

        val unauthorizedHandlerClass = Class.forName(
            "at.aau.serg.android.core.network.RetrofitProvider\$NoOpUnauthorizedSessionHandler"
        )
        val unauthorizedHandlerInstance = unauthorizedHandlerClass.getDeclaredField("INSTANCE").apply {
            isAccessible = true
        }.get(null)
        unauthorizedHandlerClass.getDeclaredMethod("onUnauthorized").apply {
            isAccessible = true
        }.invoke(unauthorizedHandlerInstance)

        assertNull(currentAccessToken)
    }

    @Test
    fun initialize_replacesRetrofit_andConfiguresAuthInterceptors() {
        val before = RetrofitProvider.retrofit

        RetrofitProvider.initialize(
            accessTokenProvider = object : AccessTokenProvider {
                override fun currentAccessToken(): String? = "token"
            },
            unauthorizedSessionHandler = object : UnauthorizedSessionHandler {
                override fun onUnauthorized() = Unit
            }
        )

        val after: Retrofit = RetrofitProvider.retrofit
        val client = after.callFactory() as OkHttpClient

        assertNotSame(before, after)
        assertEquals(2, client.interceptors.size)
        assertTrue(client.interceptors[0] is AuthTokenInterceptor)
        assertTrue(client.interceptors[1] is UnauthorizedSessionInterceptor)
    }
}
