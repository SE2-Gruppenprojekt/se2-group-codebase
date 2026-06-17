package at.aau.serg.android.core.network

import at.aau.serg.android.core.network.auth.AccessTokenProvider
import at.aau.serg.android.core.network.auth.AuthTokenInterceptor
import at.aau.serg.android.core.network.auth.UnauthorizedSessionHandler
import at.aau.serg.android.core.network.auth.UnauthorizedSessionInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    private object NoOpAccessTokenProvider : AccessTokenProvider {
        override fun currentAccessToken(): String? = null
    }

    private object NoOpUnauthorizedSessionHandler : UnauthorizedSessionHandler {
        override fun onUnauthorized() = Unit
    }

    @Volatile
    private var retrofitInstance: Retrofit = createRetrofit(
        accessTokenProvider = NoOpAccessTokenProvider,
        unauthorizedSessionHandler = NoOpUnauthorizedSessionHandler
    )

    private fun createRetrofit(
        accessTokenProvider: AccessTokenProvider,
        unauthorizedSessionHandler: UnauthorizedSessionHandler
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(accessTokenProvider))
            .addInterceptor(UnauthorizedSessionInterceptor(unauthorizedSessionHandler))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(WebConfig.API_URL)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create(MoshiProvider.moshi)
            )
            .build()
    }

    fun initialize(
        accessTokenProvider: AccessTokenProvider,
        unauthorizedSessionHandler: UnauthorizedSessionHandler
    ) {
        retrofitInstance = createRetrofit(accessTokenProvider, unauthorizedSessionHandler)
    }

    val retrofit: Retrofit
        get() = retrofitInstance
}
