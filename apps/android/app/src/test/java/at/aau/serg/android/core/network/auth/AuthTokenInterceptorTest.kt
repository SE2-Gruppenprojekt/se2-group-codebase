package at.aau.serg.android.core.network.auth

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthTokenInterceptorTest {

    @Test
    fun intercept_addsBearerHeader_whenTokenExists() {
        val interceptor = AuthTokenInterceptor(
            object : AccessTokenProvider {
                override fun currentAccessToken(): String? = "jwt-token"
            }
        )
        val chain = CapturingChain(request = Request.Builder().url("https://example.com").build())

        interceptor.intercept(chain)

        assertEquals("Bearer jwt-token", chain.capturedRequest.header("Authorization"))
    }

    @Test
    fun intercept_doesNotAddHeader_whenTokenIsBlank() {
        val interceptor = AuthTokenInterceptor(
            object : AccessTokenProvider {
                override fun currentAccessToken(): String? = "  "
            }
        )
        val chain = CapturingChain(request = Request.Builder().url("https://example.com").build())

        interceptor.intercept(chain)

        assertNull(chain.capturedRequest.header("Authorization"))
    }

    private class CapturingChain(
        private val request: Request
    ) : Interceptor.Chain {
        lateinit var capturedRequest: Request

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            capturedRequest = request
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("ok".toResponseBody())
                .build()
        }

        override fun connection() = null
        override fun call() = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    }
}
