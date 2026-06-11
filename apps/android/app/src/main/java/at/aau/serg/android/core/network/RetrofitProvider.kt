package at.aau.serg.android.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    /*
     60s timeouts to accommodate Render.com free-tier cold starts (backend can take
     up to ~30-60s to spin up after inactivity). Should be revisited if the backend
     moves to a paid tier or if only specific endpoints need the extra margin. */
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(WebConfig.API_URL)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create(MoshiProvider.moshi)
            )
            .build()
    }
}
